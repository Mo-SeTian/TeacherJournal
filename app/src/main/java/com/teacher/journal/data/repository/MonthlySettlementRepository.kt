package com.teacher.journal.data.repository

import com.teacher.journal.data.dao.MonthlySettlementDao
import com.teacher.journal.data.entity.MonthlySettlement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonthlySettlementRepository @Inject constructor(
    private val dao: MonthlySettlementDao
) {
    fun getSettlementsForStudent(studentId: Long): Flow<List<MonthlySettlement>> =
        dao.getSettlementsForStudent(studentId)

    suspend fun getSettlementsForStudentOnce(studentId: Long): List<MonthlySettlement> =
        dao.getSettlementsForStudentOnce(studentId)

    suspend fun getById(id: Long): MonthlySettlement? = dao.getById(id)

    fun getUnpaidSettlements(): Flow<List<MonthlySettlement>> = dao.getUnpaidSettlements()

    fun getUnpaidCount(): Flow<Int> = dao.getUnpaidCount()

    suspend fun getSettlementForMonth(studentId: Long, year: Int, month: Int): MonthlySettlement? =
        dao.getSettlementForMonth(studentId, year, month)

    suspend fun insert(settlement: MonthlySettlement): Long = dao.insert(settlement)

    suspend fun update(settlement: MonthlySettlement) = dao.update(settlement)

    suspend fun markAsPaid(id: Long) = dao.markAsPaid(id)

    suspend fun delete(settlement: MonthlySettlement) = dao.delete(settlement)
}
