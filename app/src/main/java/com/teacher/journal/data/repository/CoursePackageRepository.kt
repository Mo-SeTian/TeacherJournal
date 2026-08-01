package com.teacher.journal.data.repository

import androidx.room.withTransaction
import com.teacher.journal.data.dao.EarningDao
import com.teacher.journal.data.dao.CoursePackageDao
import com.teacher.journal.data.database.AppDatabase
import com.teacher.journal.data.entity.CoursePackage
import com.teacher.journal.data.entity.Earning
import com.teacher.journal.data.entity.EarningType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoursePackageRepository @Inject constructor(
    private val coursePackageDao: CoursePackageDao,
    private val earningDao: EarningDao,
    private val db: AppDatabase
) {
    fun getPackagesForStudent(studentId: Long): Flow<List<CoursePackage>> =
        coursePackageDao.getPackagesForStudent(studentId)

    suspend fun getPackagesForStudentOnce(studentId: Long): List<CoursePackage> =
        coursePackageDao.getPackagesForStudentOnce(studentId)

    suspend fun getById(id: Long): CoursePackage? = coursePackageDao.getById(id)

    suspend fun getAvailablePackages(studentId: Long): List<CoursePackage> =
        coursePackageDao.getAvailablePackages(studentId)

    fun getTotalRemainingSessions(studentId: Long): Flow<Int> =
        coursePackageDao.getTotalRemainingSessions(studentId)

    fun getTotalRemainingSessionsAll(): Flow<Int> =
        coursePackageDao.getTotalRemainingSessionsAll()

    suspend fun insert(pkg: CoursePackage): Long = coursePackageDao.insert(pkg)

    suspend fun update(pkg: CoursePackage) = coursePackageDao.update(pkg)

    suspend fun incrementUsedCount(packageId: Long) =
        coursePackageDao.incrementUsedCount(packageId)

    /**
     * 退款：把剩余次数全部退掉，记录退款金额与日期，并写入一笔负收入（幂等）。
     * @return true 表示退款成功；false 表示已退款或已用完，无可退。
     */
    suspend fun refundPackage(pkg: CoursePackage, refundAmount: Double): Boolean = db.withTransaction {
        if (pkg.isRefunded || pkg.remainingSessions <= 0) return@withTransaction false
        val remaining = pkg.remainingSessions
        val date = System.currentTimeMillis()
        coursePackageDao.markRefunded(pkg.id, remaining, refundAmount, date)
        earningDao.insert(
            Earning(
                studentId = pkg.studentId,
                type = EarningType.PACKAGE_REFUND,
                amount = -refundAmount,
                packageId = pkg.id,
                date = date,
                notes = "退课时包 ${pkg.sessionsPurchased} 次剩余 $remaining 次"
            )
        )
        true
    }

    suspend fun delete(pkg: CoursePackage) = coursePackageDao.delete(pkg)
}
