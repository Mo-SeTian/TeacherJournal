package com.teacher.journal.data.dao

import androidx.room.*
import com.teacher.journal.data.entity.MonthlySettlement
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlySettlementDao {

    @Query("SELECT * FROM monthly_settlements WHERE studentId = :studentId ORDER BY year DESC, month DESC")
    fun getSettlementsForStudent(studentId: Long): Flow<List<MonthlySettlement>>

    @Query("SELECT * FROM monthly_settlements WHERE studentId = :studentId ORDER BY year DESC, month DESC")
    suspend fun getSettlementsForStudentOnce(studentId: Long): List<MonthlySettlement>

    @Query("SELECT * FROM monthly_settlements WHERE id = :id")
    suspend fun getById(id: Long): MonthlySettlement?

    /**
     * 获取所有未收款的月结算
     */
    @Query("SELECT * FROM monthly_settlements WHERE isPaid = 0 ORDER BY year ASC, month ASC")
    fun getUnpaidSettlements(): Flow<List<MonthlySettlement>>

    /**
     * 获取未收款月结算数量
     */
    @Query("SELECT COUNT(*) FROM monthly_settlements WHERE isPaid = 0")
    fun getUnpaidCount(): Flow<Int>

    /**
     * 获取学生指定月份的结算
     */
    @Query("SELECT * FROM monthly_settlements WHERE studentId = :studentId AND year = :year AND month = :month LIMIT 1")
    suspend fun getSettlementForMonth(studentId: Long, year: Int, month: Int): MonthlySettlement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settlement: MonthlySettlement): Long

    @Update
    suspend fun update(settlement: MonthlySettlement)

    /**
     * 标记结算为已收款
     */
    @Query("UPDATE monthly_settlements SET isPaid = 1 WHERE id = :id")
    suspend fun markAsPaid(id: Long)

    @Delete
    suspend fun delete(settlement: MonthlySettlement)
}
