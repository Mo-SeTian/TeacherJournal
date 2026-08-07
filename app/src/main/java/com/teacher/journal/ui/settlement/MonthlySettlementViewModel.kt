package com.teacher.journal.ui.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacher.journal.data.entity.MonthlySettlement
import com.teacher.journal.data.entity.SessionRecord
import com.teacher.journal.data.entity.Student
import com.teacher.journal.data.repository.MonthlySettlementRepository
import com.teacher.journal.data.repository.SessionRecordRepository
import com.teacher.journal.data.repository.StudentRepository
import com.teacher.journal.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonthlySettlementUiState(
    val student: Student? = null,
    val settlements: List<MonthlySettlement> = emptyList(),
    val unsettledRecords: List<SessionRecord> = emptyList(),
    /** 当前选中的结算标签月（0-11） */
    val selectedYear: Int = 0,
    val selectedMonth: Int = 0,
    /** 当前结算周期的起止时间戳（左闭右开） */
    val window: Pair<Long, Long> = 0L to 0L,
    val isLoading: Boolean = true
)

@HiltViewModel
class MonthlySettlementViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val monthlySettlementRepository: MonthlySettlementRepository,
    private val sessionRecordRepository: SessionRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlySettlementUiState())
    val uiState: StateFlow<MonthlySettlementUiState> = _uiState.asStateFlow()

    fun load(studentId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val student = studentRepository.getStudentByIdOnce(studentId)
            val defaultMonth = student?.let {
                DateUtils.getDefaultSettlementMonth(it.settlementDay, it.createdAt)
            } ?: DateUtils.getCurrentYearMonth()
            loadForMonth(studentId, student, defaultMonth.first, defaultMonth.second)
        }
    }

    fun loadForMonth(studentId: Long, year: Int, month: Int) {
        viewModelScope.launch {
            val student = studentRepository.getStudentByIdOnce(studentId)
            loadForMonth(studentId, student, year, month)
        }
    }

    private suspend fun loadForMonth(
        studentId: Long,
        student: Student?,
        year: Int,
        month: Int
    ) {
        val settlementDay = student?.settlementDay ?: 1
        val window = DateUtils.getSettlementWindow(year, month, settlementDay)

        _uiState.update {
            it.copy(
                student = student,
                selectedYear = year,
                selectedMonth = month,
                window = window,
                isLoading = true
            )
        }

        viewModelScope.launch {
            monthlySettlementRepository.getSettlementsForStudent(studentId).collect { settlements ->
                _uiState.update { it.copy(settlements = settlements) }
            }
        }

        val unsettled = sessionRecordRepository.getUnsettledRecordsForMonth(
            studentId, window.first, window.second
        )
        _uiState.update {
            it.copy(unsettledRecords = unsettled, isLoading = false)
        }
    }

    fun previousMonth(studentId: Long) {
        val state = _uiState.value
        var year = state.selectedYear
        var month = state.selectedMonth - 1
        if (month < 0) { month = 11; year -= 1 }
        loadForMonth(studentId, year, month)
    }

    fun nextMonth(studentId: Long) {
        val state = _uiState.value
        var year = state.selectedYear
        var month = state.selectedMonth + 1
        if (month > 11) { month = 0; year += 1 }
        loadForMonth(studentId, year, month)
    }

    fun createSettlement(
        studentId: Long,
        year: Int,
        month: Int,
        amount: Double,
        isPaid: Boolean,
        notes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val window = _uiState.value.window
            val unsettled = sessionRecordRepository.getUnsettledRecordsForMonth(
                studentId, window.first, window.second
            )
            val settlement = MonthlySettlement(
                studentId = studentId,
                year = year,
                month = month,
                sessionCount = unsettled.size,
                totalAmount = amount,
                isPaid = isPaid,
                notes = notes
            )
            monthlySettlementRepository.createSettlement(
                settlement,
                unsettled.map { it.id }
            )
            onComplete()
            loadForMonth(studentId, year, month)
        }
    }

    fun updateSettlement(
        settlement: MonthlySettlement,
        amount: Double,
        isPaid: Boolean,
        notes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            monthlySettlementRepository.updateSettlementAndSyncEarning(
                settlement, amount, isPaid, notes
            )
            onComplete()
            loadForMonth(settlement.studentId, _uiState.value.selectedYear, _uiState.value.selectedMonth)
        }
    }

    fun deleteSettlement(settlement: MonthlySettlement, onComplete: () -> Unit) {
        viewModelScope.launch {
            monthlySettlementRepository.deleteSettlementAndUnlink(settlement)
            onComplete()
            loadForMonth(settlement.studentId, _uiState.value.selectedYear, _uiState.value.selectedMonth)
        }
    }

    fun markSettlementAsPaid(settlement: MonthlySettlement, onPaid: (Boolean) -> Unit) {
        viewModelScope.launch {
            val paid = monthlySettlementRepository.markSettlementAsPaid(settlement)
            onPaid(paid)
            loadForMonth(settlement.studentId, _uiState.value.selectedYear, _uiState.value.selectedMonth)
        }
    }
}
