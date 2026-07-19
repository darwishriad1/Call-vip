package com.example.data.repository

import com.example.data.dao.CallLogDao
import com.example.data.dao.ContactDao
import com.example.data.model.CallLogEntity
import com.example.data.model.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(
    private val contactDao: ContactDao,
    private val callLogDao: CallLogDao
) {
    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val allCallLogs: Flow<List<CallLogEntity>> = callLogDao.getAllCallLogs()

    suspend fun getContactById(id: Long): ContactEntity? = contactDao.getContactById(id)

    suspend fun insertContact(contact: ContactEntity): Long = contactDao.insertContact(contact)

    suspend fun updateContact(contact: ContactEntity) = contactDao.updateContact(contact)

    suspend fun deleteContact(contact: ContactEntity) = contactDao.deleteContact(contact)

    suspend fun deleteAllContacts() = contactDao.deleteAllContacts()

    suspend fun insertCallLog(callLog: CallLogEntity): Long = callLogDao.insertCallLog(callLog)

    suspend fun deleteCallLog(callLog: CallLogEntity) = callLogDao.deleteCallLog(callLog)

    suspend fun deleteAllCallLogs() = callLogDao.deleteAllCallLogs()
}
