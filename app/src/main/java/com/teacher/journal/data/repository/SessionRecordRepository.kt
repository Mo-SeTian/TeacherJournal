package com.teacher.journal.data.repository

import androidx.room.withTransaction
import com.teacher.journal.data.dao.CoursePackageDao
import com.teacher.journal.data.dao.EarningDao
import com.teacher.journal.data.dao.SessionRecordDao
import com.teacher.journal.data.database.AppDatabase
import com.teacher.journal.data.entity.Earning
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.data.entity.SessionRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRecordRepository @Inject constructor(
    private val sessionRecordDao: SessionRecordDao,
    private val coursePackageDao: CoursePackageDao,
    private val earningDao: EarningDao,
    private val db: AppDatabase
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

    /**
     * 预付费学生记录上课：在事务里扣减最早可用课时包并插入记录。
     * @return true 表示成功扣次；false 表示该学生没有可用课时包（记录未写入）。
     */
    suspend fun insertPrepaidSession(record: SessionRecord): Boolean = db.withTransaction {
        val packages = coursePackageDao.getAvailablePackages(record.studentId)
        if (packages.isEmpty()) return@withTransaction false
        val pkg = packages.first()
        coursePackageDao.incrementUsedCount(pkg.id)
        sessionRecordDao.insert(record.copy(coursePackageId = pkg.id))
        true
    }

    /** 按次付费记录上课：在事务里插入记录与收入，保证原子性。 */
    suspend fun insertSessionWithEarning(record: SessionRecord, earning: Earning) = db.withTransaction {
        val recordId = sessionRecordDao.insert(record)
        earningDao.insert(earning.copy(sessionId = recordId))
    }

    /**
     * 更新记录并同步收入（幂等）：
     * - earning != null：先清掉旧收入再写新收入（避免重复入账）
     * - earning == null：删除该记录关联的收入
     */
    suspend fun updateRecordWithEarning(record: SessionRecord, earning: Earning?) = db.withTransaction {
        sessionRecordDao.update(record)
        earningDao.deleteBySessionId(record.id)
        if (earning != null) {
            earningDao.insert(earning.copy(sessionId = record.id))
        }
    }

    /** 删除记录：同步删除关联收入，若关联课时包则回补一次课时。 */
    suspend fun deleteRecordAndRelated(record: SessionRecord) = db.withTransaction {
        if (record.coursePackageId > 0) {
            coursePackageDao.decrementUsedCount(record.coursePackageId)
        }
        earningDao.deleteBySessionId(record.id)
        sessionRecordDao.deleteById(record.id)
    }

    suspend fun update(record: SessionRecord) =
        sessionRecordDao.update(record)

    suspend fun updatePaymentStatus(id: Long, status: PaymentStatus) =
        sessionRecordDao.updatePaymentStatus(id, status)

    suspend fun getUnsettledRecords(studentId: Long): List<SessionRecord> =
        sessionRecordDao.getUnsettledRecords(studentId)

    suspend fun getUnsettledRecordsForMonth(studentId: Long, startOfMonth: Long, endOfMonth: Long): List<SessionRecord> =
        sessionRecordDao.getUnsettledRecordsForMonth(studentId, startOfMonth, endOfMonth)

    suspend fun updateSettlementId(recordIds: List<Long>, settlementId: Long) =
        sessionRecordDao.updateSettlementId(recordIds, settlementId)

    suspend fun delete(record: SessionRecord) =
        sessionRecordDao.delete(record)
}
