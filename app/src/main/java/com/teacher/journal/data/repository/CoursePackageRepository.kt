package com.teacher.journal.data.repository

import com.teacher.journal.data.dao.CoursePackageDao
import com.teacher.journal.data.entity.CoursePackage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoursePackageRepository @Inject constructor(
    private val coursePackageDao: CoursePackageDao
) {
    fun getPackagesForStudent(studentId: Long): Flow<List<CoursePackage>> =
        coursePackageDao.getPackagesForStudent(studentId)

    suspend fun getPackagesForStudentOnce(studentId: Long): List<CoursePackage> =
        coursePackageDao.getPackagesForStudentOnce(studentId)

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

    suspend fun delete(pkg: CoursePackage) = coursePackageDao.delete(pkg)
}
