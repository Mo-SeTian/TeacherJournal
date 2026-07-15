package com.teacher.journal.data.repository

import com.teacher.journal.data.dao.StudentDao
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.data.entity.Student
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val studentDao: StudentDao
) {
    fun getAllStudents(): Flow<List<Student>> = studentDao.getAllStudents()

    fun getStudentById(id: Long): Flow<Student?> = studentDao.getStudentById(id)

    suspend fun getStudentByIdOnce(id: Long): Student? = studentDao.getStudentByIdOnce(id)

    fun searchStudents(query: String): Flow<List<Student>> = studentDao.searchStudents(query)

    fun getStudentsByPaymentType(type: PaymentType): Flow<List<Student>> =
        studentDao.getStudentsByPaymentType(type)

    fun getStudentCount(): Flow<Int> = studentDao.getStudentCount()

    suspend fun insert(student: Student): Long = studentDao.insert(student)

    suspend fun update(student: Student) = studentDao.update(student)

    suspend fun deleteById(id: Long) = studentDao.deleteById(id)
}
