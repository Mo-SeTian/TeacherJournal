package com.teacher.journal.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacher.journal.data.entity.*
import com.teacher.journal.data.repository.EarningRepository
import com.teacher.journal.data.repository.SessionRecordRepository
import com.teacher.journal.data.repository.StudentRepository
import com.teacher.journal.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class CalendarDay(
    val date: Long,
    val dayOfMonth: Int,
    val sessions: List<SessionRecord>,
    val sessionCount: Int,
    val totalAmount: Double,
    val studentNames: List<String>,
    /** 是否为相邻月份（上/下月）的补位日期，渲染为浅色 */
    val isAdjacentMonth: Boolean = false
)

data class SessionListUiState(
    val records: List<SessionRecord> = emptyList(),
    val calendarDays: List<CalendarDay> = emptyList(),
    val students: Map<Long, Student> = emptyMap(),
    val currentYear: Int = 0,
    val currentMonth: Int = 0,
    val totalMonthSessions: Int = 0,
    val totalMonthAmount: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionRecordRepository: SessionRecordRepository,
    private val studentRepository: StudentRepository,
    private val earningRepository: EarningRepository
) : ViewModel() {

    private val _listUiState = MutableStateFlow(SessionListUiState())
    val listUiState: StateFlow<SessionListUiState> = _listUiState.asStateFlow()

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

            studentRepository.getAllStudents().first().let { students ->
                _listUiState.update { it.copy(students = students.associateBy { s -> s.id }) }
            }

            sessionRecordRepository.getRecordsForMonth(start, end).collect { records ->
                val studentMap = _listUiState.value.students
                val calendar = Calendar.getInstance()
                calendar.set(year, month, 1)
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val firstDow = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun

                val calendarDays = mutableListOf<CalendarDay>()
                val leading = (firstDow + 5) % 7
                // 填充上月最后几天（浅色补位）
                calendar.set(year, month, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -leading)
                repeat(leading) {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val t = calendar.timeInMillis
                    calendarDays.add(
                        CalendarDay(
                            date = t,
                            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
                            sessions = emptyList(),
                            sessionCount = 0,
                            totalAmount = 0.0,
                            studentNames = emptyList(),
                            isAdjacentMonth = true
                        )
                    )
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                // 填充本月日期
                for (day in 1..daysInMonth) {
                    calendar.set(year, month, day, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val dayStart = calendar.timeInMillis
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    val dayEnd = calendar.timeInMillis
                    val dayRecords = records.filter { it.date in dayStart..dayEnd }
                    val amount = dayRecords.filter { it.amount > 0 && it.paymentStatus == PaymentStatus.PAID }.sumOf { it.amount }
                    calendarDays.add(
                        CalendarDay(
                            date = dayStart,
                            dayOfMonth = day,
                            sessions = dayRecords,
                            sessionCount = dayRecords.size,
                            totalAmount = amount,
                            studentNames = dayRecords.mapNotNull { r -> studentMap[r.studentId]?.name }.distinct()
                        )
                    )
                }
                // 填充下月前几天（浅色补位）
                val trailing = (7 - (leading + daysInMonth) % 7) % 7
                if (trailing > 0) {
                    calendar.set(year, month, daysInMonth, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    repeat(trailing) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        calendarDays.add(
                            CalendarDay(
                                date = calendar.timeInMillis,
                                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
                                sessions = emptyList(),
                                sessionCount = 0,
                                totalAmount = 0.0,
                                studentNames = emptyList(),
                                isAdjacentMonth = true
                            )
                        )
                    }
                }

                val totalSessions = records.size
                val totalAmount = records.filter { it.paymentStatus == PaymentStatus.PAID }.sumOf { it.amount }

                _listUiState.update {
                    it.copy(
                        records = records,
                        calendarDays = calendarDays,
                        totalMonthSessions = totalSessions,
                        totalMonthAmount = totalAmount,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun previousMonth() {
        val state = _listUiState.value
        var year = state.currentYear
        var month = state.currentMonth - 1
        if (month < 0) { month = 11; year -= 1 }
        loadRecordsForMonth(year, month)
    }

    fun nextMonth() {
        val state = _listUiState.value
        var year = state.currentYear
        var month = state.currentMonth + 1
        if (month > 11) { month = 0; year += 1 }
        loadRecordsForMonth(year, month)
    }

    fun recordSession(
        studentId: Long, date: Long, startTime: String, endTime: String,
        location: String, content: String, student: Student,
        amount: Double, paymentStatus: PaymentStatus, onComplete: (String?) -> Unit
    ) {
        viewModelScope.launch {
            if (student.paymentType == PaymentType.PREPAID) {
                val record = SessionRecord(
                    studentId = studentId, date = date, startTime = startTime,
                    endTime = endTime, location = location, content = content,
                    paymentStatus = PaymentStatus.PAID, amount = 0.0
                )
                val deducted = sessionRecordRepository.insertPrepaidSession(record)
                if (!deducted) {
                    // 课时包已用完：记录照存、不扣次，标记待收费，避免数据丢失
                    sessionRecordRepository.insert(record.copy(
                        paymentStatus = PaymentStatus.UNPAID,
                        amount = 0.0
                    ))
                    onComplete("课时包已用完，本次已记录但未扣课时，可在记录中补记收费")
                } else {
                    onComplete(null)
                }
            } else if (student.paymentType == PaymentType.MONTHLY) {
                sessionRecordRepository.insert(SessionRecord(
                    studentId = studentId, date = date, startTime = startTime,
                    endTime = endTime, location = location, content = content,
                    paymentStatus = PaymentStatus.PAID, amount = 0.0, settlementId = -1
                ))
                onComplete(null)
            } else {
                val record = SessionRecord(
                    studentId = studentId, date = date, startTime = startTime,
                    endTime = endTime, location = location, content = content,
                    paymentStatus = paymentStatus, amount = amount
                )
                if (paymentStatus == PaymentStatus.PAID && amount > 0) {
                    sessionRecordRepository.insertSessionWithEarning(
                        record,
                        Earning(
                            studentId = studentId, type = EarningType.SESSION_PAYMENT,
                            amount = amount, date = date
                        )
                    )
                } else {
                    sessionRecordRepository.insert(record)
                }
                onComplete(null)
            }
            val state = _listUiState.value
            loadRecordsForMonth(state.currentYear, state.currentMonth)
        }
    }

    fun getRecordOnce(recordId: Long, onLoaded: (SessionRecord?) -> Unit) {
        viewModelScope.launch {
            onLoaded(sessionRecordRepository.getRecordByIdOnce(recordId))
        }
    }

    fun updateRecord(
        recordId: Long,
        date: Long, startTime: String, endTime: String,
        location: String, content: String,
        amount: Double, paymentStatus: PaymentStatus,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val existing = sessionRecordRepository.getRecordByIdOnce(recordId) ?: return@launch
            val updated = existing.copy(
                date = date, startTime = startTime, endTime = endTime,
                location = location, content = content,
                amount = amount, paymentStatus = paymentStatus
            )
            val earning = if (paymentStatus == PaymentStatus.PAID && amount > 0) {
                Earning(
                    studentId = existing.studentId,
                    type = EarningType.SESSION_PAYMENT,
                    amount = amount,
                    date = date
                )
            } else {
                null
            }
            sessionRecordRepository.updateRecordWithEarning(updated, earning)
            val state = _listUiState.value
            loadRecordsForMonth(state.currentYear, state.currentMonth)
            onComplete()
        }
    }

    fun deleteRecord(recordId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            val record = sessionRecordRepository.getRecordByIdOnce(recordId) ?: return@launch
            sessionRecordRepository.deleteRecordAndRelated(record)
            val state = _listUiState.value
            loadRecordsForMonth(state.currentYear, state.currentMonth)
            onComplete()
        }
    }

    fun markAsPaid(recordId: Long) {
        viewModelScope.launch {
            val record = sessionRecordRepository.getRecordByIdOnce(recordId) ?: return@launch
            if (record.paymentStatus == PaymentStatus.PAID) return@launch
            sessionRecordRepository.updatePaymentStatus(recordId, PaymentStatus.PAID)
            // 防重复入账：已有收入记录则不再写入
            if (record.amount > 0 && earningRepository.getBySessionId(recordId) == null) {
                earningRepository.insert(Earning(
                    studentId = record.studentId, type = EarningType.SESSION_PAYMENT,
                    amount = record.amount, sessionId = recordId, date = record.date
                ))
            }
            val state = _listUiState.value
            loadRecordsForMonth(state.currentYear, state.currentMonth)
        }
    }
}
