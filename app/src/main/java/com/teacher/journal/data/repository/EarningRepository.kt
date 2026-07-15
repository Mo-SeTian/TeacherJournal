package com.teacher.journal.data.repository

import com.teacher.journal.data.dao.EarningDao
import com.teacher.journal.data.entity.Earning
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EarningRepository @Inject constructor(
    private val earningDao: EarningDao
) {
    fun getEarningsForStudent(studentId: Long): Flow<List<Earning>> =
        earningDao.getEarningsForStudent(studentId)

    fun getAllEarnings(): Flow<List<Earning>> =
        earningDao.getAllEarnings()

    fun getMonthlyEarning(startOfMonth: Long, endOfMonth: Long): Flow<Double> =
        earningDao.getMonthlyEarning(startOfMonth, endOfMonth)

    suspend fun insert(earning: Earning): Long =
        earningDao.insert(earning)

    suspend fun update(earning: Earning) =
        earningDao.update(earning)

    suspend fun delete(earning: Earning) =
        earningDao.delete(earning)
}
