package com.teacher.journal.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacher.journal.data.entity.*
import com.teacher.journal.data.repository.CoursePackageRepository
import com.teacher.journal.data.repository.MonthlySettlementRepository
import com.teacher.journal.data.repository.SessionRecordRepository
import com.teacher.journal.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentListUiState(
    val students: List<Student> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

data class StudentDetailUiState(
    val student: Student? = null,
    val coursePackages: List<CoursePackage> = emptyList(),
    val remainingSessions: Int = 0,
    val sessionRecords: List<SessionRecord> = emptyList(),
    val unpaidRecords: List<SessionRecord> = emptyList(),
    val monthlySettlements: List<MonthlySettlement> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val coursePackageRepository: CoursePackageRepository,
    private val sessionRecordRepository: SessionRecordRepository,
    private val monthlySettlementRepository: MonthlySettlementRepository
) : ViewModel() {

    private val _listUiState = MutableStateFlow(StudentListUiState())
    val listUiState: StateFlow<StudentListUiState> = _listUiState.asStateFlow()

    private val _detailUiState = MutableStateFlow(StudentDetailUiState())
    val detailUiState: StateFlow<StudentDetailUiState> = _detailUiState.asStateFlow()

    init {
        loadStudents()
    }

    fun loadStudents() {
        viewModelScope.launch {
            studentRepository.getAllStudents().collect { students ->
                _listUiState.update { it.copy(students = students, isLoading = false) }
            }
        }
    }

    fun searchStudents(query: String) {
        _listUiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                studentRepository.getAllStudents().collect { students ->
                    _listUiState.update { it.copy(students = students) }
                }
            } else {
                studentRepository.searchStudents(query).collect { students ->
                    _listUiState.update { it.copy(students = students) }
                }
            }
        }
    }

    fun loadStudentDetail(studentId: Long) {
        viewModelScope.launch {
            // 学生信息
            launch {
                studentRepository.getStudentById(studentId).collect { student ->
                    _detailUiState.update { it.copy(student = student) }
                }
            }

            // 课时包
            launch {
                coursePackageRepository.getPackagesForStudent(studentId).collect { packages ->
                    _detailUiState.update { it.copy(coursePackages = packages) }
                }
            }

            // 剩余课时
            launch {
                coursePackageRepository.getTotalRemainingSessions(studentId).collect { remaining ->
                    _detailUiState.update { it.copy(remainingSessions = remaining) }
                }
            }

            // 上课记录
            launch {
                sessionRecordRepository.getRecordsForStudent(studentId).collect { records ->
                    _detailUiState.update {
                        it.copy(
                            sessionRecords = records,
                            unpaidRecords = records.filter { r -> r.paymentStatus == PaymentStatus.UNPAID },
                            isLoading = false
                        )
                    }
                }
            }

            // 月结算记录
            launch {
                monthlySettlementRepository.getSettlementsForStudent(studentId).collect { settlements ->
                    _detailUiState.update { it.copy(monthlySettlements = settlements) }
                }
            }
        }
    }

    fun insertStudent(
        name: String,
        phone: String,
        subject: String,
        location: String,
        paymentType: PaymentType,
        monthlyRate: Double,
        notes: String,
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val student = Student(
                name = name,
                phone = phone,
                subject = subject,
                location = location,
                paymentType = paymentType,
                monthlyRate = monthlyRate,
                notes = notes
            )
            val id = studentRepository.insert(student)
            onComplete(id)
        }
    }

    fun updateStudent(
        id: Long,
        name: String,
        phone: String,
        subject: String,
        location: String,
        paymentType: PaymentType,
        monthlyRate: Double,
        notes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val existing = studentRepository.getStudentByIdOnce(id) ?: return@launch
            studentRepository.update(
                existing.copy(
                    name = name,
                    phone = phone,
                    subject = subject,
                    location = location,
                    paymentType = paymentType,
                    monthlyRate = monthlyRate,
                    notes = notes
                )
            )
            onComplete()
        }
    }

    fun deleteStudent(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            studentRepository.deleteById(id)
            onComplete()
        }
    }
}
