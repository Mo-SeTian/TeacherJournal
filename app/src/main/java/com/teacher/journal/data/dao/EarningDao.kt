package com.teacher.journal.data.dao

import androidx.room.*
import com.teacher.journal.data.entity.Earning
import kotlinx.coroutines.flow.Flow

@Dao
interface EarningDao {

    @Query("SELECT * FROM earnings WHERE studentId = :studentId ORDER BY date DESC")
    fun getEarningsForStudent(studentId: Long): Flow<List<Earning>>

    @Query("SELECT * FROM earnings")
    suspend fun getAllOnce(): List<Earning>

    @Query("SELECT * FROM earnings ORDER BY date DESC")
    fun getAllEarnings(): Flow<List<Earning>>

    @Query("SELECT * FROM earnings WHERE sessionId = :sessionId AND sessionId != -1 LIMIT 1")
    suspend fun getBySessionId(sessionId: Long): Earning?

    @Query("SELECT * FROM earnings WHERE packageId = :packageId AND packageId != -1 LIMIT 1")
    suspend fun getByPackageId(packageId: Long): Earning?

    @Query("SELECT * FROM earnings WHERE settlementId = :settlementId AND settlementId != -1 LIMIT 1")
    suspend fun getBySettlementId(settlementId: Long): Earning?

    /**
     * 获取本月收入总额
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) 
        FROM earnings 
        WHERE date BETWEEN :startOfMonth AND :endOfMonth
    """)
    fun getMonthlyEarning(startOfMonth: Long, endOfMonth: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(earning: Earning): Long

    @Update
    suspend fun update(earning: Earning)

    @Query("DELETE FROM earnings WHERE sessionId = :sessionId AND sessionId != -1")
    suspend fun deleteBySessionId(sessionId: Long)

    @Query("DELETE FROM earnings WHERE settlementId = :settlementId AND settlementId != -1")
    suspend fun deleteBySettlementId(settlementId: Long)

    @Delete
    suspend fun delete(earning: Earning)
}
