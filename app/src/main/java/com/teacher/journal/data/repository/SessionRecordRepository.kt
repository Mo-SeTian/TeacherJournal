package com.teacher.journal.data.repository

import com.teacher.journal.data.dao.SessionRecordDao
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.data.entity.SessionRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRecordRepository @Inject constructor(
    private val sessionRecordDao: SessionRecordDao
) {
    fun getRecordsForStudent(studentId: Long): Flow<List<SessionRecord>> =
        sessionRecordDao.getRecordsForStudent(studentId)

    fun getAllRecords(): Flow<List<SessionRecord>> =
        sessionRecordDao.getAllRecords()

    fun getRecordsForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<SessionRecord>> =
        sessionRecordDao.getRecordsForMonth(startOfMonth, endOfMonth)

    fun getRecordById(id: Long): Flow<SessionRecord?> =
        sessionRecordDao.getRecordById(id)

    suspend fun getRecordByIdOnce(id: Long): SessionRecord? =
        sessionRecordDao.getRecordByIdOnce(id)

    fun getUnpaidRecords(): Flow<List<SessionRecord>> =
        sessionRecordDao.getUnpaidRecords()

    fun getUnpaidCount(): Flow<Int> =
        sessionRecordDao.getUnpaidCount()

    fun getRecentRecords(limit: Int): Flow<List<SessionRecord>> =
        sessionRecordDao.getRecentRecords(limit)

    suspend fun insert(record: SessionRecord): Long =
        sessionRecordDao.insert(record)

    suspend fun update(record: SessionRecord) =
        sessionRecordDao.update(record)

    suspend fun updatePaymentStatus(id: Long, status: PaymentStatus) =
        sessionRecordDao.updatePaymentStatus(id, status)

    suspend fun delete(record: SessionRecord) =
        sessionRecordDao.delete(record)
}
