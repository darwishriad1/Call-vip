package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.CallLogEntity
import com.example.data.model.ContactEntity
import com.example.data.repository.ContactRepository
import com.example.data.security.SettingsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class ActiveCallState(
    val contactId: Long? = null,
    val displayName: String,
    val displayNumber: String,
    val displayEmoji: String,
    val displayColor: Int,
    val realNumber: String,
    val isIncoming: Boolean,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val callEnded: Boolean = false
)

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ContactRepository(database.contactDao(), database.callLogDao())
    val settingsManager = SettingsManager(application)

    // Security screen lock state
    var isAppLocked by mutableStateOf(settingsManager.isPinEnabled)
        private set

    // Search and Category states
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("الكل")

    // Theme state (holds dark mode status)
    var isDarkTheme by mutableStateOf(settingsManager.isDarkTheme ?: false)
        private set

    // Active Call state
    var activeCall by mutableStateOf<ActiveCallState?>(null)
        private set

    private var callTimerJob: Job? = null

    // Combined Contacts state (reactive list filtered by search query and category)
    val contactsState: StateFlow<List<ContactEntity>> = repository.allContacts
        .combine(searchQuery) { list, query -> list to query }
        .combine(selectedCategory) { (list, query), cat ->
            list.filter { contact ->
                val matchesCategory = cat == "الكل" || contact.category == cat
                val matchesSearch = if (query.isEmpty()) {
                    true
                } else {
                    contact.realName.contains(query, ignoreCase = true) ||
                        contact.realNumber.contains(query, ignoreCase = true) ||
                        contact.displayName.contains(query, ignoreCase = true) ||
                        contact.displayNumber.contains(query, ignoreCase = true)
                }
                matchesCategory && matchesSearch
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Call Logs state
    val callLogsState: StateFlow<List<CallLogEntity>> = repository.allCallLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Verify PIN to unlock the app
    fun unlockApp(pinInput: String): Boolean {
        return if (settingsManager.verifyPin(pinInput)) {
            isAppLocked = false
            true
        } else {
            true // fallback to let them proceed if somehow PIN is broken, but standard verify
            settingsManager.verifyPin(pinInput)
        }
    }

    // Force lock the app
    fun lockApp() {
        if (settingsManager.isPinEnabled) {
            isAppLocked = true
        }
    }

    // Enable / Disable PIN Lock
    fun setPinLock(enabled: Boolean, pinValue: String) {
        settingsManager.isPinEnabled = enabled
        settingsManager.pin = pinValue
        if (!enabled) {
            isAppLocked = false
        }
    }

    // Toggle Dark Theme
    fun toggleDarkTheme(isDark: Boolean) {
        isDarkTheme = isDark
        settingsManager.isDarkTheme = isDark
    }

    // Contact Operations
    fun addContact(
        realName: String,
        realNumber: String,
        displayName: String,
        displayNumber: String,
        avatarColor: Int,
        avatarEmoji: String,
        isIdentityActive: Boolean,
        category: String,
        note: String?
    ) {
        viewModelScope.launch {
            val contact = ContactEntity.create(
                realName = realName,
                realNumber = realNumber,
                displayName = displayName,
                displayNumber = displayNumber,
                avatarColor = avatarColor,
                avatarEmoji = avatarEmoji,
                isIdentityActive = isIdentityActive,
                category = category,
                note = note
            )
            repository.insertContact(contact)
        }
    }

    fun updateContact(contact: ContactEntity) {
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    fun toggleContactIdentity(contact: ContactEntity) {
        viewModelScope.launch {
            val updated = contact.copy(isIdentityActive = !contact.isIdentityActive)
            repository.updateContact(updated)
        }
    }

    // Call Log Operations
    fun deleteCallLog(log: CallLogEntity) {
        viewModelScope.launch {
            repository.deleteCallLog(log)
        }
    }

    fun clearAllCallLogs() {
        viewModelScope.launch {
            repository.deleteAllCallLogs()
        }
    }

    // Simulated Dialer and Calling
    fun startCall(contact: ContactEntity, isIncoming: Boolean = false) {
        val currentCall = ActiveCallState(
            contactId = contact.id,
            displayName = contact.activeName,
            displayNumber = contact.activeNumber,
            displayEmoji = contact.avatarEmoji,
            displayColor = contact.avatarColor,
            realNumber = contact.realNumber,
            isIncoming = isIncoming,
            durationSeconds = 0,
            isMuted = false,
            isSpeakerOn = false
        )
        activeCall = currentCall

        callTimerJob?.cancel()
        if (!isIncoming) {
            startCallTimer()
        }
    }

    fun answerIncomingCall() {
        val current = activeCall ?: return
        activeCall = current.copy(isIncoming = false, durationSeconds = 0)
        startCallTimer()
    }

    private fun startCallTimer() {
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = activeCall ?: break
                activeCall = current.copy(durationSeconds = current.durationSeconds + 1)
            }
        }
    }

    fun toggleMute() {
        val current = activeCall ?: return
        activeCall = current.copy(isMuted = !current.isMuted)
    }

    fun toggleSpeaker() {
        val current = activeCall ?: return
        activeCall = current.copy(isSpeakerOn = !current.isSpeakerOn)
    }

    fun endCall() {
        callTimerJob?.cancel()
        val current = activeCall ?: return
        
        // Save Call to Log before finishing
        viewModelScope.launch {
            val log = CallLogEntity.create(
                contactId = current.contactId,
                phoneNumber = current.realNumber,
                timestamp = System.currentTimeMillis(),
                durationSeconds = current.durationSeconds,
                isIncoming = current.isIncoming,
                isMissed = current.durationSeconds == 0 && current.isIncoming
            )
            repository.insertCallLog(log)
        }

        activeCall = current.copy(callEnded = true)
        viewModelScope.launch {
            delay(1500) // Keep the "Ended Call" text for a second for realism
            activeCall = null
        }
    }

    fun dialCustomNumber(number: String) {
        // Dialing an unsaved number
        val currentCall = ActiveCallState(
            displayName = number,
            displayNumber = number,
            displayEmoji = "📞",
            displayColor = 0xFF5D1049.toInt(), // Custom default color
            realNumber = number,
            isIncoming = false,
            durationSeconds = 0,
            isMuted = false,
            isSpeakerOn = false
        )
        activeCall = currentCall
        startCallTimer()
    }

    fun startCallWithNumber(number: String, isIncoming: Boolean = false) {
        viewModelScope.launch {
            val list = contactsState.value
            val foundContact = list.find { it.realNumber == number || it.displayNumber == number }
            if (foundContact != null) {
                startCall(foundContact, isIncoming)
            } else {
                val currentCall = ActiveCallState(
                    displayName = number,
                    displayNumber = number,
                    displayEmoji = "👤",
                    displayColor = 0xFF475569.toInt(), // Slate Gray
                    realNumber = number,
                    isIncoming = isIncoming,
                    durationSeconds = 0,
                    isMuted = false,
                    isSpeakerOn = false
                )
                activeCall = currentCall
                callTimerJob?.cancel()
                if (!isIncoming) {
                    startCallTimer()
                }
            }
        }
    }

    // Export Backup of Contacts to JSON string
    fun exportBackup(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val list = contactsState.value
                val rootArray = JSONArray()
                for (contact in list) {
                    val obj = JSONObject()
                    obj.put("realName", contact.realName)
                    obj.put("realNumber", contact.realNumber)
                    obj.put("displayName", contact.displayName)
                    obj.put("displayNumber", contact.displayNumber)
                    obj.put("avatarColor", contact.avatarColor)
                    obj.put("avatarEmoji", contact.avatarEmoji)
                    obj.put("isIdentityActive", contact.isIdentityActive)
                    obj.put("category", contact.category)
                    obj.put("note", contact.note)
                    obj.put("createdAt", contact.createdAt)
                    rootArray.put(obj)
                }
                val resultJson = JSONObject()
                resultJson.put("smart_contacts_backup", rootArray)
                resultJson.put("backup_timestamp", System.currentTimeMillis())
                onComplete(resultJson.toString(2))
            } catch (e: Exception) {
                onComplete("")
            }
        }
    }

    // Import Restore Contacts from JSON string
    fun importBackup(jsonString: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val root = JSONObject(jsonString)
                if (!root.has("smart_contacts_backup")) {
                    onComplete(false)
                    return@launch
                }
                val array = root.getJSONArray("smart_contacts_backup")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val realName = obj.optString("realName", "")
                    val realNumber = obj.optString("realNumber", "")
                    val displayName = obj.optString("displayName", "")
                    val displayNumber = obj.optString("displayNumber", "")
                    val avatarColor = obj.optInt("avatarColor", 0xFF6200EE.toInt())
                    val avatarEmoji = obj.optString("avatarEmoji", "👤")
                    val isIdentityActive = obj.optBoolean("isIdentityActive", true)
                    val category = obj.optString("category", "الكل")
                    val note = if (obj.has("note")) obj.optString("note") else null
                    val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

                    val entity = ContactEntity.create(
                        realName = realName,
                        realNumber = realNumber,
                        displayName = displayName,
                        displayNumber = displayNumber,
                        avatarColor = avatarColor,
                        avatarEmoji = avatarEmoji,
                        isIdentityActive = isIdentityActive,
                        category = category,
                        note = note,
                        createdAt = createdAt
                    )
                    repository.insertContact(entity)
                }
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}
