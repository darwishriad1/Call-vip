package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.security.EncryptionHelper

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val realNameEncrypted: String,
    val realNumberEncrypted: String,
    val displayNameEncrypted: String,
    val displayNumberEncrypted: String,
    val avatarColor: Int,
    val avatarEmoji: String,
    val isIdentityActive: Boolean = true,
    val category: String,
    val noteEncrypted: String?,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Helper constructor to create with clear text (will encrypt automatically)
    companion object {
        fun create(
            id: Long = 0,
            realName: String,
            realNumber: String,
            displayName: String,
            displayNumber: String,
            avatarColor: Int,
            avatarEmoji: String,
            isIdentityActive: Boolean = true,
            category: String,
            note: String?,
            createdAt: Long = System.currentTimeMillis()
        ): ContactEntity {
            return ContactEntity(
                id = id,
                realNameEncrypted = EncryptionHelper.encrypt(realName),
                realNumberEncrypted = EncryptionHelper.encrypt(realNumber),
                displayNameEncrypted = EncryptionHelper.encrypt(displayName),
                displayNumberEncrypted = EncryptionHelper.encrypt(displayNumber),
                avatarColor = avatarColor,
                avatarEmoji = avatarEmoji,
                isIdentityActive = isIdentityActive,
                category = category,
                noteEncrypted = note?.let { EncryptionHelper.encrypt(it) },
                createdAt = createdAt
            )
        }
    }

    val realName: String
        get() = EncryptionHelper.decrypt(realNameEncrypted)

    val realNumber: String
        get() = EncryptionHelper.decrypt(realNumberEncrypted)

    val displayName: String
        get() = EncryptionHelper.decrypt(displayNameEncrypted)

    val displayNumber: String
        get() = EncryptionHelper.decrypt(displayNumberEncrypted)

    val note: String?
        get() = noteEncrypted?.let { EncryptionHelper.decrypt(it) }

    // Helper to get active showing name and number based on status
    val activeName: String
        get() = if (isIdentityActive) displayName else realName

    val activeNumber: String
        get() = if (isIdentityActive) displayNumber else realNumber
}
