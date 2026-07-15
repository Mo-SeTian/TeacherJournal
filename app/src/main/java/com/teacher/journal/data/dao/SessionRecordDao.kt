package com.teacher.journal.data.dao

import androidx.room.*
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.data.entity.SessionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionRecordDao {

    @Query("SELECT * FROM session_records WHERE studentId = :studentId ORDER BY date DESC, startTime DESC")
    fun getRecordsForStudent(studentId: Long): Flow<List<SessionRecord>>

    @Query("SELECT * FROM session_records ORDER BY date DESC, startTime DESC")
    fun getAllRecords(): Flow<List<SessionRecord>>

    @Query("SELECT * FROM session_records WHERE date BETWEEN :startOfMonth AND :endOfMonth ORDER BY date DESC, startTime DESC")
    fun getRecordsForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<SessionRecord>>

    @Query("SELECT * FROM session_records WHERE id = :id")
    fun getRecordById(id: Long): Flow<SessionRecord?>

    @Query("SELECT * FROM session_records WHERE id = :id")
    suspend fun getRecordByIdOnce(id: Long): SessionRecord?

    /**
     * 获取所有待收费的上课记录
     */
    @Query("SELECT * FROM session_records WHERE paymentStatus = 'UNPAID' ORDER BY date ASC")
    fun getUnpaidRecords(): Flow<List<SessionRecord>>

    /**
     * 获取待收费记录数量
     */
    @Query("SELECT COUNT(*) FROM session_records WHERE paymentStatus = 'UNPAID'")
    fun getUnpaidCount(): Flow<Int>

    /**
     * 获取最近 N 条记录
     */
    @Query("SELECT * FROM session_records ORDER BY date DESC, createdAt DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<SessionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SessionRecord): Long

    @Update
    suspend fun update(record: SessionRecord)

    /**
     * 更新收费状态
     */
    @Query("UPDATE session_records SET paymentStatus = :status WHERE id = :id")
    suspend fun updatePaymentStatus(id: Long, status: PaymentStatus)

    /**
     * 获取学生未结算的上课记录（月结算模式）
     */
    @Query("SELECT * FROM session_records WHERE studentId = :studentId AND settlementId = -1 ORDER BY date ASC")
    suspend fun getUnsettledRecords(studentId: Long): List<SessionRecord>

    /**
     * 获取学生指定月份未结算的上课记录
     */
    @Query("""
        SELECT * FROM session_records 
        WHERE studentId = :studentId AND settlementId = -1 
        AND date BETWEEN :startOfMonth AND :endOfMonth 
        ORDER BY date ASC
    """)
    suspend fun getUnsettledRecordsForMonth(studentId: Long, startOfMonth: Long, endOfMonth: Long): List<SessionRecord>

    /**
     * 批量更新记录的结算 ID
     */
    @Query("UPDATE session_records SET settlementId = :settlementId WHERE id IN (:recordIds)")
    suspend fun updateSettlementId(recordIds: List<Long>, settlementId: Long)

    @Delete
    suspend fun delete(record: SessionRecord)
}
