package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.security.EncryptionHelper

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long? = null, // Links to a saved contact if exists
    val phoneNumberEncrypted: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val isIncoming: Boolean = false,
    val isMissed: Boolean = false
) {
    companion object {
        fun create(
            id: Long = 0,
            contactId: Long? = null,
            phoneNumber: String,
            timestamp: Long = System.currentTimeMillis(),
            durationSeconds: Int = 0,
            isIncoming: Boolean = false,
            isMissed: Boolean = false
        ): CallLogEntity {
            return CallLogEntity(
                id = id,
                contactId = contactId,
                phoneNumberEncrypted = EncryptionHelper.encrypt(phoneNumber),
                timestamp = timestamp,
                durationSeconds = durationSeconds,
                isIncoming = isIncoming,
                isMissed = isMissed
            )
        }
    }

    val phoneNumber: String
        get() = EncryptionHelper.decrypt(phoneNumberEncrypted)
}
