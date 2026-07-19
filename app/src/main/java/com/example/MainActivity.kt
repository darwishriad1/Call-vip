package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.ActiveCallScreen
import com.example.ui.screens.AddEditContactScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.MainTabsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ContactsViewModel

sealed class Screen {
    object Lock : Screen()
    object MainTabs : Screen()
    object AddContact : Screen()
    data class EditContact(val id: Long) : Screen()
}

class MainActivity : ComponentActivity() {
    private val viewModel: ContactsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = viewModel.isDarkTheme) {
                // Local state-based navigation
                var currentScreen by remember {
                    mutableStateOf<Screen>(
                        if (viewModel.isAppLocked) Screen.Lock else Screen.MainTabs
                    )
                }

                // If app locks from outside or ViewModel, force lock screen
                if (viewModel.isAppLocked && currentScreen != Screen.Lock) {
                    currentScreen = Screen.Lock
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(targetState = currentScreen, label = "ScreenTransition") { screen ->
                        when (screen) {
                            is Screen.Lock -> {
                                LockScreen(
                                    viewModel = viewModel,
                                    onUnlocked = { currentScreen = Screen.MainTabs }
                                )
                            }
                            is Screen.MainTabs -> {
                                MainTabsScreen(
                                    viewModel = viewModel,
                                    onAddContact = { currentScreen = Screen.AddContact },
                                    onEditContact = { id -> currentScreen = Screen.EditContact(id) }
                                )
                            }
                            is Screen.AddContact -> {
                                AddEditContactScreen(
                                    viewModel = viewModel,
                                    contactId = null,
                                    onNavigateBack = { currentScreen = Screen.MainTabs }
                                )
                            }
                            is Screen.EditContact -> {
                                AddEditContactScreen(
                                    viewModel = viewModel,
                                    contactId = screen.id,
                                    onNavigateBack = { currentScreen = Screen.MainTabs }
                                )
                            }
                        }
                    }

                    // Simulated Active Call Screen Overlay (highest visual hierarchy)
                    val activeCall = viewModel.activeCall
                    if (activeCall != null) {
                        ActiveCallScreen(viewModel = viewModel, callState = activeCall)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            com.example.service.SmartInCallService.setListener(object : com.example.service.SmartInCallService.SystemCallListener {
                override fun onSystemCallAdded(call: android.telecom.Call) {
                    val handleUri = call.details?.handle
                    val number = handleUri?.schemeSpecificPart ?: "مجهول"
                    viewModel.startCallWithNumber(number, isIncoming = true)
                }

                override fun onSystemCallRemoved(call: android.telecom.Call) {
                    viewModel.endCall()
                }

                override fun onSystemCallStateChanged(call: android.telecom.Call, state: Int) {
                    if (state == android.telecom.Call.STATE_DISCONNECTED) {
                        viewModel.endCall()
                    }
                }
            })
        }
    }

    override fun onStop() {
        super.onStop()
        // Auto-lock the application when minimized/backgrounded for ultimate privacy protection!
        viewModel.lockApp()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            com.example.service.SmartInCallService.setListener(null)
        }
    }
}
