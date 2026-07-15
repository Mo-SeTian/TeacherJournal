package com.teacher.journal.data.dao

import androidx.room.*
import com.teacher.journal.data.entity.Earning
import kotlinx.coroutines.flow.Flow

@Dao
interface EarningDao {

    @Query("SELECT * FROM earnings WHERE studentId = :studentId ORDER BY date DESC")
    fun getEarningsForStudent(studentId: Long): Flow<List<Earning>>

    @Query("SELECT * FROM earnings ORDER BY date DESC")
    fun getAllEarnings(): Flow<List<Earning>>

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

    @Delete
    suspend fun delete(earning: Earning)
}
