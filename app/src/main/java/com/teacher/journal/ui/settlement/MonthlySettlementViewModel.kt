package com.teacher.journal.ui.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacher.journal.data.entity.*
import com.teacher.journal.data.repository.EarningRepository
import com.teacher.journal.data.repository.MonthlySettlementRepository
import com.teacher.journal.data.repository.SessionRecordRepository
import com.teacher.journal.data.repository.StudentRepository
import com.teacher.journal.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class MonthlySettlementUiState(
    val student: Student? = null,
    val settlements: List<MonthlySettlement> = emptyList(),
    val unsettledRecords: List<SessionRecord> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MonthlySettlementViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val monthlySettlementRepository: MonthlySettlementRepository,
    private val sessionRecordRepository: SessionRecordRepository,
    private val earningRepository: EarningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlySettlementUiState())
    val uiState: StateFlow<MonthlySettlementUiState> = _uiState.asStateFlow()

    fun load(studentId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val student = studentRepository.getStudentByIdOnce(studentId)
            _uiState.update { it.copy(student = student) }

            // 加载结算记录
            launch {
                monthlySettlementRepository.getSettlementsForStudent(studentId).collect { settlements ->
                    _uiState.update { it.copy(settlements = settlements) }
                }
            }

            // 加载未结算的上课记录
            val unsettled = sessionRecordRepository.getUnsettledRecords(studentId)
            _uiState.update { it.copy(unsettledRecords = unsettled, isLoading = false) }
        }
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
            val calendar = Calendar.getInstance()
            calendar.set(year, month, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfMonth = calendar.timeInMillis

            val unsettled = sessionRecordRepository.getUnsettledRecordsForMonth(studentId, startOfMonth, endOfMonth)

            val settlement = MonthlySettlement(
                studentId = studentId,
                year = year,
                month = month,
                sessionCount = unsettled.size,
                totalAmount = amount,
                isPaid = isPaid,
                notes = notes
            )
            val settlementId = monthlySettlementRepository.insert(settlement)

            // 关联上课记录
            if (unsettled.isNotEmpty()) {
                sessionRecordRepository.updateSettlementId(unsettled.map { it.id }, settlementId)
            }

            // 如果已收款，记录收入
            if (isPaid) {
                earningRepository.insert(
                    Earning(
                        studentId = studentId,
                        type = EarningType.SESSION_PAYMENT,
                        amount = amount,
                        date = System.currentTimeMillis(),
                        notes = "${year}年${month + 1}月结算"
                    )
                )
            }

            onComplete()
        }
    }

    fun markSettlementAsPaid(settlementId: Long) {
        viewModelScope.launch {
            val settlement = monthlySettlementRepository.getById(settlementId) ?: return@launch
            monthlySettlementRepository.markAsPaid(settlementId)

            earningRepository.insert(
                Earning(
                    studentId = settlement.studentId,
                    type = EarningType.SESSION_PAYMENT,
                    amount = settlement.totalAmount,
                    date = System.currentTimeMillis(),
                    notes = "${settlement.year}年${settlement.month + 1}月结算"
                )
            )
        }
    }
}
