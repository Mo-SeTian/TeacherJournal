package com.teacher.journal.data.dao

import androidx.room.*
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.data.entity.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY createdAt DESC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentByIdOnce(id: Long): Student?

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchStudents(query: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE paymentType = :type ORDER BY createdAt DESC")
    fun getStudentsByPaymentType(type: PaymentType): Flow<List<Student>>

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteById(id: Long)
}
