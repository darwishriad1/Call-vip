package com.example.ui.screens

import android.widget.Toast
import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import android.app.role.RoleManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallLogEntity
import com.example.data.model.ContactEntity
import com.example.ui.viewmodel.ContactsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(
    viewModel: ContactsViewModel,
    onAddContact: () -> Unit,
    onEditContact: (Long) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val contacts by viewModel.contactsState.collectAsState()
    val callLogs by viewModel.callLogsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "جهات الاتصال الخاصة"
                            1 -> "سجل المكالمات السري"
                            2 -> "لوحة الاتصال البديلة"
                            else -> "إعدادات الخصوصية"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Contacts, "جهات الاتصال") },
                    label = { Text("الجهات") },
                    modifier = Modifier.testTag("nav_contacts")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.History, "سجل المكالمات") },
                    label = { Text("السجل") },
                    modifier = Modifier.testTag("nav_history")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Dialpad, "لوحة الاتصال") },
                    label = { Text("الاتصال") },
                    modifier = Modifier.testTag("nav_dialer")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, "الإعدادات") },
                    label = { Text("الإعدادات") },
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onAddContact,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_contact_fab")
                ) {
                    Icon(Icons.Default.Add, "إضافة جهة اتصال")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ContactsTabContent(
                    viewModel = viewModel,
                    contacts = contacts,
                    onEditContact = onEditContact
                )
                1 -> CallLogsTabContent(
                    viewModel = viewModel,
                    callLogs = callLogs,
                    contacts = contacts
                )
                2 -> DialerTabContent(
                    viewModel = viewModel
                )
                3 -> SettingsTabContent(
                    viewModel = viewModel,
                    contacts = contacts
                )
            }
        }
    }
}

@Composable
fun ContactsTabContent(
    viewModel: ContactsViewModel,
    contacts: List<ContactEntity>,
    onEditContact: (Long) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories = listOf("الكل", "عائلة", "أصدقاء", "عمل", "خاص")

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .testTag("contact_search_input"),
            placeholder = { Text("ابحث بالاسم الحقيقي أو اسم العرض...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        // Categories Tab
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
            edgePadding = 16.dp,
            divider = {},
            containerColor = Color.Transparent
        ) {
            categories.forEach { cat ->
                Tab(
                    selected = selectedCategory == cat,
                    onClick = { viewModel.selectedCategory.value = cat },
                    text = {
                        Text(
                            text = cat,
                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (contacts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Contacts,
                    contentDescription = "لا توجد جهات اتصال",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (searchQuery.isNotEmpty()) "لم يتم العثور على نتائج" else "قائمة جهات الاتصال الخاصة فارغة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (searchQuery.isNotEmpty()) "جرب البحث بكلمات أخرى" else "اضغط على زر (+) في الأسفل لإضافة هوية بديلة جديدة مجانًا بخصوصية تامة.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(contacts, key = { it.id }) { contact ->
                    ContactItemCard(
                        contact = contact,
                        onToggleIdentity = { viewModel.toggleContactIdentity(contact) },
                        onCall = { viewModel.startCall(contact, isIncoming = false) },
                        onReceiveSimulatedCall = { viewModel.startCall(contact, isIncoming = true) },
                        onEdit = { onEditContact(contact.id) },
                        onDelete = { viewModel.deleteContact(contact) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // extra padding for fab
            }
        }
    }
}

@Composable
fun ContactItemCard(
    contact: ContactEntity,
    onToggleIdentity: () -> Unit,
    onCall: () -> Unit,
    onReceiveSimulatedCall: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف جهة الاتصال") },
            text = { Text("هل أنت متأكد من رغبتك في حذف جهة الاتصال '${contact.realName}' نهائيًا؟ لن يتم التراجع عن هذا الإجراء.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contact_card_${contact.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar (Color Background + Emoji)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(contact.avatarColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.avatarEmoji,
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Identity info details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.activeName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Alternate identity active badge
                        if (contact.isIdentityActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "بديل نشط",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = contact.activeNumber,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Small category tag
                    Text(
                        text = "التصنيف: " + contact.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Call buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Outgoing Call Simulator
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "اتصال صادر",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Incoming Call Simulator (Receiving)
                    IconButton(
                        onClick = onReceiveSimulatedCall,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "محاكاة مكالمة واردة",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Real identity details summary shown expandable or sub-view
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show real identity details in small fonts for admin verification
                Column {
                    Text(
                        text = "البيانات الحقيقية:",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${contact.realName} • ${contact.realNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Edit, Delete, Toggle alternative identity actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Toggle alternate identity setting
                    IconButton(onClick = onToggleIdentity) {
                        Icon(
                            imageVector = if (contact.isIdentityActive) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "تبديل الهوية",
                            tint = if (contact.isIdentityActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (!contact.note.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "ملاحظة: " + contact.note,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CallLogsTabContent(
    viewModel: ContactsViewModel,
    callLogs: List<CallLogEntity>,
    contacts: List<ContactEntity>
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("مسح سجل المكالمات") },
            text = { Text("هل أنت متأكد من رغبتك في مسح كل سجل المكالمات الخاص؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllCallLogs()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("مسح الكل")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (callLogs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المكالمات الأخيرة (${callLogs.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "مسح سجل المكالمات",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { showClearConfirm = true }
                        .padding(4.dp)
                )
            }
        }

        if (callLogs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "لا مكالمات",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "سجل المكالمات الخاصة فارغ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "عند محاكاة مكالمة مع أي جهة اتصال، ستظهر المكالمة هنا مشفرة وفقًا للهوية البديلة النشطة.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(callLogs) { log ->
                    // Find if there is a matching contact in the DB
                    val matchingContact = contacts.find { it.id == log.contactId || it.realNumber == log.phoneNumber }
                    
                    CallLogItemRow(
                        log = log,
                        contact = matchingContact,
                        onDialBack = {
                            if (matchingContact != null) {
                                viewModel.startCall(matchingContact)
                            } else {
                                viewModel.dialCustomNumber(log.phoneNumber)
                            }
                        },
                        onDelete = { viewModel.deleteCallLog(log) }
                    )
                }
            }
        }
    }
}

@Composable
fun CallLogItemRow(
    log: CallLogEntity,
    contact: ContactEntity?,
    onDialBack: () -> Unit,
    onDelete: () -> Unit
) {
    // Resolve display name and display number according to alternate identity status
    val displayName = contact?.activeName ?: log.phoneNumber
    val displayNumber = contact?.activeNumber ?: log.phoneNumber
    val isIdentityApplied = contact?.isIdentityActive ?: false

    val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(log.timestamp))

    val minutes = log.durationSeconds / 60
    val seconds = log.durationSeconds % 60
    val durationText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Call type icon (Incoming, Outgoing, Missed)
            val iconColor = when {
                log.isMissed -> MaterialTheme.colorScheme.error
                log.isIncoming -> MaterialTheme.colorScheme.primary
                else -> Color(0xFF4CAF50)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        log.isMissed -> Icons.Default.CallEnd
                        log.isIncoming -> Icons.AutoMirrored.Default.CallReceived
                        else -> Icons.AutoMirrored.Default.CallMade
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (log.isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    if (isIdentityApplied) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "بديل",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (log.isMissed) "مكالمة فائتة • $formattedDate" else "مدة المكالمة: $durationText • $formattedDate",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDialBack) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "اتصال مجدد",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف المكالمة من السجل",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun DialerTabContent(
    viewModel: ContactsViewModel
) {
    var dialInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Dial Input Display Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dialInput,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (dialInput.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "اضغط على زر الاتصال بالأسفل لبدء المحاكاة الآمنة",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Dialer numpad keys grid
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("*", "0", "#")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { digit ->
                        KeypadButton(digit) {
                            if (dialInput.length < 20) {
                                dialInput += digit
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dial action and backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear all input
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clickable { dialInput = "" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "إلغاء",
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Call Action Green Key
                IconButton(
                    onClick = {
                        if (dialInput.isNotEmpty()) {
                            viewModel.dialCustomNumber(dialInput)
                        }
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "اتصال",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Backspace
                IconButton(
                    onClick = {
                        if (dialInput.isNotEmpty()) {
                            dialInput = dialInput.dropLast(1)
                        }
                    },
                    modifier = Modifier.size(68.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "تراجع",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsTabContent(
    viewModel: ContactsViewModel,
    contacts: List<ContactEntity>
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val telecomManager = remember { context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager }
    var isDefaultDialer by remember {
        mutableStateOf(telecomManager?.defaultDialerPackage == context.packageName)
    }

    val requestDefaultDialerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultDialer = telecomManager?.defaultDialerPackage == context.packageName
    }

    var pinEnabled by remember { mutableStateOf(viewModel.settingsManager.isPinEnabled) }
    var pinValue by remember { mutableStateOf(viewModel.settingsManager.pin) }

    var backupText by remember { mutableStateOf("") }
    var restoreText by remember { mutableStateOf("") }

    var showClearDataDialog by remember { mutableStateOf(false) }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("حذف جميع البيانات") },
            text = { Text("هل أنت متأكد من رغبتك في مسح جميع جهات الاتصال الخاصة وسجل المكالمات نهائيًا؟ لا يمكن استعادة البيانات بعد ذلك.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllCallLogs()
                        viewModel.contactsState.value.forEach { viewModel.deleteContact(it) }
                        Toast.makeText(context, "تم مسح جميع البيانات بنجاح", Toast.LENGTH_SHORT).show()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("مسح البيانات")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDataDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Default Phone App & Activation Guide Section
        item {
            Text(
                text = "تهيئة وتفعيل تطبيق الاتصال",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDefaultDialer) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    else 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDefaultDialer) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "حالة الاتصال الأساسي",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isDefaultDialer) Color(0xFF4CAF50) else Color(0xFFFF9800))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isDefaultDialer) "نشط كتطبيق الهاتف الافتراضي" else "غير نشط كتطبيق افتراضي حاليًا",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDefaultDialer) Color(0xFF2E7D32) else Color(0xFFD84315)
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isDefaultDialer) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "قفل",
                            tint = if (isDefaultDialer) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "طريقة تفعيل التطبيق كتطبيق أساسي للاتصال:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val steps = listOf(
                        "1. انقر على زر 'التعيين كتطبيق افتراضي' أدناه.",
                        "2. ستظهر لك نافذة نظام Android تطلب تأكيد الاختيار.",
                        "3. اختر تطبيقنا من القائمة ثم اضغط 'تعيين كافتراضي' (Set as Default).",
                        "4. عند استقبال أو إجراء مكالمة، ستتداخل هوية الحماية تلقائيًا لتشفير هويتك الحقيقية."
                    )

                    steps.forEach { step ->
                        Text(
                            text = step,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Button(
                        onClick = {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                                    if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                                        requestDefaultDialerLauncher.launch(intent)
                                    } else {
                                        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                                            putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                                        }
                                        requestDefaultDialerLauncher.launch(intent)
                                    }
                                } else {
                                    val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                                        putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                                    }
                                    requestDefaultDialerLauncher.launch(intent)
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "لم نتمكن من فتح إعدادات النظام، يمكنك تفعيلها يدويًا من إعدادات تطبيقات الهاتف الافتراضية.", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDefaultDialer) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "تفعيل الهاتف")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isDefaultDialer) "تعديل تطبيق الاتصال الافتراضي" else "التعيين كتطبيق افتراضي للاتصال")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Simulation controls for easy testing!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (contacts.isNotEmpty()) {
                                    viewModel.startCall(contacts.first(), isIncoming = true)
                                    Toast.makeText(context, "جاري محاكاة مكالمة واردة آمنة...", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.startCallWithNumber("0791234567", isIncoming = true)
                                    Toast.makeText(context, "جاري محاكاة مكالمة واردة مجهولة...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("محاكاة مكالمة واردة", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                if (contacts.isNotEmpty()) {
                                    viewModel.startCall(contacts.first(), isIncoming = false)
                                    Toast.makeText(context, "جاري محاكاة اتصال صادر آمن...", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.startCallWithNumber("0791234567", isIncoming = false)
                                    Toast.makeText(context, "جاري محاكاة اتصال صادر مجهول...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("محاكاة اتصال صادر", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // App Theme / Dark Mode Section
        item {
            Text(
                text = "المظهر العام",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الوضع الداكن (الليل)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تعديل ألوان واجهة التطبيق لتناسب الاستخدام الليلي.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = viewModel.isDarkTheme,
                        onCheckedChange = { viewModel.toggleDarkTheme(it) },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // Passcode Protection Section
        item {
            Text(
                text = "حماية الخصوصية والأمان",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "حماية التطبيق برمز حماية PIN",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "طلب رمز سري مكون من 4 أرقام عند فتح التطبيق.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = pinEnabled,
                            onCheckedChange = {
                                pinEnabled = it
                                if (!it) {
                                    viewModel.setPinLock(false, "")
                                    pinValue = ""
                                }
                            },
                            modifier = Modifier.testTag("pin_lock_switch")
                        )
                    }

                    if (pinEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = pinValue,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    pinValue = it
                                    if (it.length == 4) {
                                        viewModel.setPinLock(true, it)
                                        Toast.makeText(context, "تم تفعيل رمز PIN بنجاح", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            label = { Text("رمز الحماية PIN الجديد (4 أرقام)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pin_value_input"),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Backup and Restore Section
        item {
            Text(
                text = "النسخ الاحتياطي واستعادة البيانات",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إنشاء نسخة احتياطية مشفرة محليًا",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "انسخ كود النسخة الاحتياطية لتخزينه بأمان خارج التطبيق.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.exportBackup { backup ->
                                backupText = backup
                                clipboardManager.setText(AnnotatedString(backup))
                                Toast.makeText(context, "تم نسخ كود الاحتياط إلى الحافظة!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, "مشاركة الكود")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إنشاء ونسخ كود الاحتياط")
                    }

                    if (backupText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = backupText,
                                fontSize = 10.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    Text(
                        text = "استعادة البيانات من كود احتياطي",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreText,
                        onValueChange = { restoreText = it },
                        label = { Text("أدخل كود النسخة الاحتياطية هنا") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("restore_value_input"),
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (restoreText.isBlank()) {
                                Toast.makeText(context, "الرجاء إدخال كود صالح", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.importBackup(restoreText) { success ->
                                    if (success) {
                                        Toast.makeText(context, "تمت استعادة البيانات بنجاح!", Toast.LENGTH_LONG).show()
                                        restoreText = ""
                                    } else {
                                        Toast.makeText(context, "فشلت الاستعادة، كود غير صالح", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("استعادة البيانات الآن")
                    }
                }
            }
        }

        // Clear Database Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "منطقة الخطر",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "حذف كافة جهات الاتصال المسجلة وسجل المكالمات دفعة واحدة.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showClearDataDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "تحذير",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
