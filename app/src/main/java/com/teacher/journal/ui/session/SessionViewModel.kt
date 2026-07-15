package com.teacher.journal.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacher.journal.data.entity.*
import com.teacher.journal.data.repository.CoursePackageRepository
import com.teacher.journal.data.repository.EarningRepository
import com.teacher.journal.data.repository.SessionRecordRepository
import com.teacher.journal.data.repository.StudentRepository
import com.teacher.journal.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionListUiState(
    val records: List<SessionRecord> = emptyList(),
    val students: Map<Long, Student> = emptyMap(),
    val currentYear: Int = 0,
    val currentMonth: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionRecordRepository: SessionRecordRepository,
    private val studentRepository: StudentRepository,
    private val coursePackageRepository: CoursePackageRepository,
    private val earningRepository: EarningRepository
) : ViewModel() {

    private val _listUiState = MutableStateFlow(SessionListUiState())
    val listUiState: StateFlow<SessionListUiState> = _listUiState.asStateFlow()

    // 所有学生列表（供选择器使用）
    val allStudents: StateFlow<List<Student>> = studentRepository.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val (year, month) = DateUtils.getCurrentYearMonth()
        _listUiState.update { it.copy(currentYear = year, currentMonth = month) }
        loadRecordsForMonth(year, month)
    }

    fun loadRecordsForMonth(year: Int, month: Int) {
        _listUiState.update { it.copy(currentYear = year, currentMonth = month, isLoading = true) }
        viewModelScope.launch {
            val start = DateUtils.getStartOfMonth(year, month)
            val end = DateUtils.getEndOfMonth(year, month)

            // 加载学生列表映射
            studentRepository.getAllStudents().first().let { students ->
                _listUiState.update { it.copy(students = students.associateBy { s -> s.id }) }
            }

            sessionRecordRepository.getRecordsForMonth(start, end).collect { records ->
                _listUiState.update { it.copy(records = records, isLoading = false) }
            }
        }
    }

    fun previousMonth() {
        val state = _listUiState.value
        var year = state.currentYear
        var month = state.currentMonth - 1
        if (month < 0) {
            month = 11
            year -= 1
        }
        loadRecordsForMonth(year, month)
    }

    fun nextMonth() {
        val state = _listUiState.value
        var year = state.currentYear
        var month = state.currentMonth + 1
        if (month > 11) {
            month = 0
            year += 1
        }
        loadRecordsForMonth(year, month)
    }

    /**
     * 记录上课
     */
    fun recordSession(
        studentId: Long,
        date: Long,
        startTime: String,
        endTime: String,
        location: String,
        content: String,
        student: Student,
        amount: Double,
        paymentStatus: PaymentStatus,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            if (student.paymentType == PaymentType.PREPAID) {
                // 预付费：从课时包扣减
                val packages = coursePackageRepository.getAvailablePackages(studentId)
                if (packages.isNotEmpty()) {
                    // 使用第一个有剩余的课时包
                    val pkg = packages.first()
                    coursePackageRepository.incrementUsedCount(pkg.id)

                    val record = SessionRecord(
                        studentId = studentId,
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        location = location,
                        content = content,
                        paymentStatus = PaymentStatus.PAID,
                        amount = 0.0,
                        coursePackageId = pkg.id
                    )
                    sessionRecordRepository.insert(record)
                }
            } else {
                // 按次付费
                val record = SessionRecord(
                    studentId = studentId,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    location = location,
                    content = content,
                    paymentStatus = paymentStatus,
                    amount = amount
                )
                val recordId = sessionRecordRepository.insert(record)

                // 如果已收费，记录收入
                if (paymentStatus == PaymentStatus.PAID) {
                    earningRepository.insert(
                        Earning(
                            studentId = studentId,
                            type = EarningType.SESSION_PAYMENT,
                            amount = amount,
                            sessionId = recordId
                        )
                    )
                }
            }

            // 刷新列表
            val state = _listUiState.value
            loadRecordsForMonth(state.currentYear, state.currentMonth)
            onComplete()
        }
    }

    fun markAsPaid(recordId: Long) {
        viewModelScope.launch {
            val record = sessionRecordRepository.getRecordByIdOnce(recordId) ?: return@launch
            sessionRecordRepository.updatePaymentStatus(recordId, PaymentStatus.PAID)

            // 记录收入
            if (record.amount > 0) {
                earningRepository.insert(
                    Earning(
                        studentId = record.studentId,
                        type = EarningType.SESSION_PAYMENT,
                        amount = record.amount,
                        sessionId = recordId
                    )
                )
            }

            // 刷新
            val state = _listUiState.value
            loadRecordsForMonth(state.currentYear, state.currentMonth)
        }
    }
}
