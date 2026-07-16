package com.teacher.journal.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacher.journal.data.entity.MonthlySettlement
import com.teacher.journal.data.entity.SessionRecord
import com.teacher.journal.data.entity.Student
import com.teacher.journal.data.repository.CoursePackageRepository
import com.teacher.journal.data.repository.EarningRepository
import com.teacher.journal.data.repository.MonthlySettlementRepository
import com.teacher.journal.data.repository.SessionRecordRepository
import com.teacher.journal.data.repository.StudentRepository
import com.teacher.journal.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val studentCount: Int = 0,
    val totalRemainingSessions: Int = 0,
    val monthlyIncome: Double = 0.0,
    val unpaidRecords: List<UnpaidRecordItem> = emptyList(),
    val unpaidSettlements: List<UnpaidSettlementItem> = emptyList(),
    val lowSessionStudents: List<LowSessionStudentItem> = emptyList(),
    val recentRecords: List<RecentRecordItem> = emptyList(),
    val isLoading: Boolean = true
)

data class UnpaidRecordItem(
    val record: SessionRecord,
    val studentName: String,
    val isOverdue: Boolean
)

data class UnpaidSettlementItem(
    val settlement: MonthlySettlement,
    val studentName: String
)

data class LowSessionStudentItem(
    val studentName: String,
    val remainingSessions: Int,
    val studentId: Long
)

data class RecentRecordItem(
    val record: SessionRecord,
    val studentName: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val coursePackageRepository: CoursePackageRepository,
    private val sessionRecordRepository: SessionRecordRepository,
    private val earningRepository: EarningRepository,
    private val monthlySettlementRepository: MonthlySettlementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            // 学生总数
            launch {
                studentRepository.getStudentCount().collect { count ->
                    _uiState.update { it.copy(studentCount = count) }
                }
            }

            // 总剩余课时
            launch {
                coursePackageRepository.getTotalRemainingSessionsAll().collect { total ->
                    _uiState.update { it.copy(totalRemainingSessions = total) }
                }
            }

            // 本月收入
            launch {
                val (start, end) = DateUtils.getCurrentMonthRange()
                earningRepository.getMonthlyEarning(start, end).collect { income ->
                    _uiState.update { it.copy(monthlyIncome = income) }
                }
            }

            // 待收费记录（按次付费）
            launch {
                combine(
                    sessionRecordRepository.getUnpaidRecords(),
                    studentRepository.getAllStudents()
                ) { records, students ->
                    val studentMap = students.associateBy { it.id }
                    records.map { record ->
                        UnpaidRecordItem(
                            record = record,
                            studentName = studentMap[record.studentId]?.name ?: "未知",
                            isOverdue = DateUtils.isOverDays(record.date, 7)
                        )
                    }
                }.collect { items ->
                    _uiState.update { it.copy(unpaidRecords = items) }
                }
            }

            // 未收款月结算
            launch {
                combine(
                    monthlySettlementRepository.getUnpaidSettlements(),
                    studentRepository.getAllStudents()
                ) { settlements, students ->
                    val studentMap = students.associateBy { it.id }
                    settlements.map { settlement ->
                        UnpaidSettlementItem(
                            settlement = settlement,
                            studentName = studentMap[settlement.studentId]?.name ?: "未知"
                        )
                    }
                }.collect { items ->
                    _uiState.update { it.copy(unpaidSettlements = items) }
                }
            }

            // 课时不足学生（剩余 ≤ 2）
            launch {
                studentRepository.getStudentsByPaymentType(
                    com.teacher.journal.data.entity.PaymentType.PREPAID
                ).collect { prepaidStudents ->
                    val lowSessionList = mutableListOf<LowSessionStudentItem>()
                    for (student in prepaidStudents) {
                        coursePackageRepository.getTotalRemainingSessions(student.id).first().let { remaining ->
                            if (remaining <= 2) {
                                lowSessionList.add(
                                    LowSessionStudentItem(
                                        studentName = student.name,
                                        remainingSessions = remaining,
                                        studentId = student.id
                                    )
                                )
                            }
                        }
                    }
                    _uiState.update { it.copy(lowSessionStudents = lowSessionList) }
                }
            }

            // 最近上课记录
            launch {
                combine(
                    sessionRecordRepository.getRecentRecords(5),
                    studentRepository.getAllStudents()
                ) { records, students ->
                    val studentMap = students.associateBy { it.id }
                    records.map { record ->
                        RecentRecordItem(
                            record = record,
                            studentName = studentMap[record.studentId]?.name ?: "未知"
                        )
                    }
                }.collect { items ->
                    _uiState.update { it.copy(recentRecords = items, isLoading = false) }
                }
            }
        }
    }

    fun markAsPaid(recordId: Long) {
        viewModelScope.launch {
            val record = sessionRecordRepository.getRecordByIdOnce(recordId) ?: return@launch
            sessionRecordRepository.updatePaymentStatus(
                recordId,
                com.teacher.journal.data.entity.PaymentStatus.PAID
            )
            // 记录收入，日期用实际上课日
            if (record.amount > 0) {
                earningRepository.insert(
                    com.teacher.journal.data.entity.Earning(
                        studentId = record.studentId,
                        type = com.teacher.journal.data.entity.EarningType.SESSION_PAYMENT,
                        amount = record.amount,
                        sessionId = recordId,
                        date = record.date
                    )
                )
            }
        }
    }
}
