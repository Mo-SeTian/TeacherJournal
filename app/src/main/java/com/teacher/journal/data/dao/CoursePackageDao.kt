package com.teacher.journal.data.dao

import androidx.room.*
import com.teacher.journal.data.entity.CoursePackage
import kotlinx.coroutines.flow.Flow

@Dao
interface CoursePackageDao {

    @Query("SELECT * FROM course_packages WHERE studentId = :studentId ORDER BY purchaseDate DESC")
    fun getPackagesForStudent(studentId: Long): Flow<List<CoursePackage>>

    @Query("SELECT * FROM course_packages WHERE studentId = :studentId ORDER BY purchaseDate DESC")
    suspend fun getPackagesForStudentOnce(studentId: Long): List<CoursePackage>

    /**
     * 获取学生所有还有剩余次数的课时包，按购买日期升序（先用最早买的）
     */
    @Query("""
        SELECT * FROM course_packages 
        WHERE studentId = :studentId AND usedCount < sessionsPurchased 
        ORDER BY purchaseDate ASC
    """)
    suspend fun getAvailablePackages(studentId: Long): List<CoursePackage>

    /**
     * 获取学生总剩余课时
     */
    @Query("""
        SELECT COALESCE(SUM(sessionsPurchased - usedCount), 0) 
        FROM course_packages 
        WHERE studentId = :studentId
    """)
    fun getTotalRemainingSessions(studentId: Long): Flow<Int>

    /**
     * 获取所有学生的总剩余课时
     */
    @Query("SELECT COALESCE(SUM(sessionsPurchased - usedCount), 0) FROM course_packages")
    fun getTotalRemainingSessionsAll(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pkg: CoursePackage): Long

    @Update
    suspend fun update(pkg: CoursePackage)

    @Query("UPDATE course_packages SET usedCount = usedCount + 1 WHERE id = :packageId")
    suspend fun incrementUsedCount(packageId: Long)

    @Delete
    suspend fun delete(pkg: CoursePackage)
}
