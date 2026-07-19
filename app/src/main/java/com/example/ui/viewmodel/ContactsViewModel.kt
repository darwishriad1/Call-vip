package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.CallLogEntity
import com.example.data.model.ContactEntity
import com.example.data.repository.ContactRepository
import com.example.data.security.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    var currentSystemCall: android.telecom.Call? = null

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

    // Real and Simulated Call Operations
    fun makeRealPhoneCall(number: String) {
        if (number.isEmpty()) return
        val context = getApplication<Application>()
        val uri = Uri.fromParts("tel", number, null)
        
        val hasCallPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context, 
            android.Manifest.permission.CALL_PHONE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            } catch (ex: Exception) {
                // Ignore fallback failures
            }
        }
    }

    fun startCall(contact: ContactEntity, isIncoming: Boolean = false, isSimulation: Boolean = false) {
        if (!isSimulation && !isIncoming) {
            makeRealPhoneCall(contact.realNumber)
            return
        }

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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            currentSystemCall?.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
        }
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
        val nextMuted = !current.isMuted
        activeCall = current.copy(isMuted = nextMuted)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            com.example.service.SmartInCallService.toggleSystemMute(nextMuted)
        }
    }

    fun toggleSpeaker() {
        val current = activeCall ?: return
        val nextSpeaker = !current.isSpeakerOn
        activeCall = current.copy(isSpeakerOn = nextSpeaker)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            com.example.service.SmartInCallService.toggleSystemSpeaker(nextSpeaker)
        }
    }

    fun endCall() {
        callTimerJob?.cancel()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            currentSystemCall?.disconnect()
        }
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
            currentSystemCall = null
        }
    }

    fun dialCustomNumber(number: String, isSimulation: Boolean = false) {
        if (!isSimulation) {
            makeRealPhoneCall(number)
            return
        }

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

    fun setSystemCall(call: android.telecom.Call) {
        currentSystemCall = call
        val handleUri = call.details?.handle
        val number = handleUri?.schemeSpecificPart ?: "مجهول"
        val isIncoming = call.state == android.telecom.Call.STATE_RINGING

        viewModelScope.launch {
            val list = contactsState.value
            val foundContact = list.find { it.realNumber == number || it.displayNumber == number }
            if (foundContact != null) {
                val currentCall = ActiveCallState(
                    contactId = foundContact.id,
                    displayName = foundContact.activeName,
                    displayNumber = foundContact.activeNumber,
                    displayEmoji = foundContact.avatarEmoji,
                    displayColor = foundContact.avatarColor,
                    realNumber = foundContact.realNumber,
                    isIncoming = isIncoming,
                    durationSeconds = 0,
                    isMuted = false,
                    isSpeakerOn = false
                )
                activeCall = currentCall
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
            }
            callTimerJob?.cancel()
            if (!isIncoming) {
                startCallTimer()
            }
        }
    }

    fun startCallWithNumber(number: String, isIncoming: Boolean = false) {
        viewModelScope.launch {
            val list = contactsState.value
            val foundContact = list.find { it.realNumber == number || it.displayNumber == number }
            if (foundContact != null) {
                startCall(foundContact, isIncoming, isSimulation = true)
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

    // Device Contacts Integration
    var deviceContacts = mutableStateListOf<DeviceContact>()
        private set

    var hasContactsPermission by mutableStateOf(false)
        private set

    fun checkContactsPermission() {
        val context = getApplication<Application>()
        hasContactsPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun fetchDeviceContacts() {
        checkContactsPermission()
        if (!hasContactsPermission) return
        
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val list = mutableListOf<DeviceContact>()
            val resolver = context.contentResolver
            
            var cursor: android.database.Cursor? = null
            try {
                cursor = resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                
                cursor?.let { c ->
                    val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    
                    while (c.moveToNext()) {
                        val name = if (nameIndex >= 0) c.getString(nameIndex) ?: "" else ""
                        val number = if (numberIndex >= 0) c.getString(numberIndex) ?: "" else ""
                        if (name.isNotEmpty() && number.isNotEmpty()) {
                            val cleanNum = number.replace("\\s".toRegex(), "")
                            val isAlreadyImported = contactsState.value.any { 
                                it.realNumber.replace("\\s".toRegex(), "") == cleanNum || 
                                it.displayNumber.replace("\\s".toRegex(), "") == cleanNum 
                            }
                            list.add(DeviceContact(name = name, phoneNumber = number, isImported = isAlreadyImported))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            
            val uniqueList = list.distinctBy { it.phoneNumber.replace("\\s".toRegex(), "").replace("-", "") }
            
            withContext(Dispatchers.Main) {
                deviceContacts.clear()
                deviceContacts.addAll(uniqueList)
            }
        }
    }

    fun importDeviceContact(deviceContact: DeviceContact, alternativeName: String, alternativeNumber: String, category: String = "خاص") {
        viewModelScope.launch {
            val emojis = listOf("👤", "🔑", "🛡️", "🕵️", "👥", "💼", "🏠", "🌟", "🔥", "⚡")
            val colors = listOf(
                0xFF6200EE.toInt(), 0xFF03DAC6.toInt(), 0xFF3700B3.toInt(),
                0xFF4CAF50.toInt(), 0xFFFF5722.toInt(), 0xFFE91E63.toInt(),
                0xFF9C27B0.toInt(), 0xFF00BCD4.toInt(), 0xFFFF9800.toInt()
            )
            val randomEmoji = emojis.random()
            val randomColor = colors.random()
            
            val contact = ContactEntity.create(
                realName = deviceContact.name,
                realNumber = deviceContact.phoneNumber,
                displayName = alternativeName,
                displayNumber = alternativeNumber,
                avatarColor = randomColor,
                avatarEmoji = randomEmoji,
                isIdentityActive = true,
                category = category,
                note = "تم استيراده من جهات اتصال الهاتف"
            )
            repository.insertContact(contact)
            fetchDeviceContacts()
        }
    }
}

data class DeviceContact(
    val name: String,
    val phoneNumber: String,
    val isImported: Boolean = false
)
