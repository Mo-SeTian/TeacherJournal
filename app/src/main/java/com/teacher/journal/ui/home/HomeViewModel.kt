package com.teacher.journal.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacher.journal.data.entity.MonthlySettlement
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.data.entity.SessionRecord
import com.teacher.journal.data.entity.Student
import com.teacher.journal.data.entity.StudentStatus
import com.teacher.journal.data.repository.CoursePackageRepository
import com.teacher.journal.data.repository.EarningRepository
import com.teacher.journal.data.repository.MonthlySettlementRepository
import com.teacher.journal.data.repository.SessionRecordRepository
import com.teacher.journal.data.repository.StudentRepository
import com.teacher.journal.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
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
    /** 月结算提醒 — 结算日已过但未结算或未收款 */
    val monthlyReminders: List<ReminderItem> = emptyList(),
    /** 课时提醒 — 冻结/不带了但仍有剩余课时 */
    val unfinishedSessions: List<UnfinishedSessionsItem> = emptyList(),
    val isLoading: Boolean = true
)

enum class ReminderType {
    /** 结算日已过，尚未创建结算 */
    MONTHLY_UNSETTLED,
    /** 已创建结算但未收款 */
    MONTHLY_UNPAID
}

data class ReminderItem(
    val type: ReminderType,
    val studentName: String,
    val studentId: Long,
    val amount: Double,
    val detail: String,
    val isOverdue: Boolean,
    val settlementId: Long = -1
)

data class UnfinishedSessionsItem(
    val studentName: String,
    val remainingSessions: Int,
    val studentId: Long,
    val isFrozen: Boolean
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

            // 课时提醒 — 冻结/不带了但仍有剩余课时
            launch {
                studentRepository.getAllStudents().collect { allStudents ->
                    val list = mutableListOf<UnfinishedSessionsItem>()
                    for (student in allStudents) {
                        if (student.paymentType == PaymentType.PREPAID && student.status != StudentStatus.ACTIVE) {
                            val remaining = coursePackageRepository.getTotalRemainingSessions(student.id).first()
                            if (remaining > 0) {
                                list.add(
                                    UnfinishedSessionsItem(
                                        studentName = student.name,
                                        remainingSessions = remaining,
                                        studentId = student.id,
                                        isFrozen = student.status == StudentStatus.FROZEN
                                    )
                                )
                            }
                        }
                    }
                    _uiState.update { it.copy(unfinishedSessions = list) }
                }
            }

            // 月结算提醒 — 从学生创建/结算日起，每个月的结算日提示，未完成则一直提示
            launch {
                studentRepository.getAllStudents().collect { allStudents ->
                    val reminders = mutableListOf<ReminderItem>()
                    val zone = ZoneId.systemDefault()
                    val today = LocalDate.now(zone)
                    val monthlyStudents = allStudents.filter { it.paymentType == PaymentType.MONTHLY }

                    for (student in monthlyStudents) {
                        val day = student.settlementDay.coerceIn(1, 28)
                        val created = Instant.ofEpochMilli(student.createdAt).atZone(zone).toLocalDate()
                        val firstYear = created.year
                        val firstMonth = created.monthValue - 1
                        val currentYear = today.year
                        val currentMonth = today.monthValue - 1

                        // 遍历从创建月到当前月的所有结算周期
                        var y = firstYear
                        var m = firstMonth
                        var first = true
                        while (yearMonthCompare(y, m, currentYear, currentMonth) <= 0) {
                            // 跳过创建当月之前的结算（如果结算日在创建日之前）
                            val periodStart = LocalDate.of(y, m + 1, day)
                            if (first && periodStart.isBefore(created)) {
                                advanceMonth(y, m).let { y = it.first; m = it.second }
                                first = false
                                continue
                            }
                            first = false

                            // 如果是当前月，只在结算日之后提示
                            if (y == currentYear && m == currentMonth) {
                                val currentSettlementStart = LocalDate.of(currentYear, currentMonth + 1, day)
                                if (today.isBefore(currentSettlementStart)) break
                            }

                            // 冻结/不带了之后，该日期之后的不再提示
                            if (student.status != StudentStatus.ACTIVE && student.statusChangedAt > 0) {
                                val cutoffDate = Instant.ofEpochMilli(student.statusChangedAt).atZone(zone).toLocalDate()
                                val periodStartDate = periodStart
                                if (periodStartDate.isAfter(cutoffDate) || periodStartDate.isEqual(cutoffDate)) {
                                    break
                                }
                            }

                            // 查询该月结算
                            val settlement = monthlySettlementRepository.getSettlementForMonth(student.id, y, m)
                            if (settlement == null) {
                                reminders.add(
                                    ReminderItem(
                                        type = ReminderType.MONTHLY_UNSETTLED,
                                        studentName = student.name,
                                        studentId = student.id,
                                        amount = 0.0,
                                        detail = "结算日${day}号 · ${y}年${m + 1}月未结算",
                                        isOverdue = true
                                    )
                                )
                            } else if (!settlement.isPaid) {
                                reminders.add(
                                    ReminderItem(
                                        type = ReminderType.MONTHLY_UNPAID,
                                        studentName = student.name,
                                        studentId = student.id,
                                        amount = settlement.totalAmount,
                                        detail = "${y}年${m + 1}月结算 · ${settlement.sessionCount}次课",
                                        isOverdue = false,
                                        settlementId = settlement.id
                                    )
                                )
                            }

                            advanceMonth(y, m).let { y = it.first; m = it.second }
                        }
                    }
                    _uiState.update { it.copy(monthlyReminders = reminders) }
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
            if (record.paymentStatus == com.teacher.journal.data.entity.PaymentStatus.PAID) return@launch
            sessionRecordRepository.updatePaymentStatus(
                recordId,
                com.teacher.journal.data.entity.PaymentStatus.PAID
            )
            // 记录收入（幂等），日期用实际上课日
            if (record.amount > 0 && earningRepository.getBySessionId(recordId) == null) {
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

    fun markSettlementAsPaid(settlementId: Long) {
        viewModelScope.launch {
            val settlement = monthlySettlementRepository.getById(settlementId) ?: return@launch
            monthlySettlementRepository.markSettlementAsPaid(settlement)
        }
    }

    /** 冻结学生（课时保留，结算不再提示） */
    fun setStudentFrozen(studentId: Long) {
        viewModelScope.launch {
            val student = studentRepository.getStudentByIdOnce(studentId) ?: return@launch
            studentRepository.update(
                student.copy(
                    status = StudentStatus.FROZEN,
                    statusChangedAt = System.currentTimeMillis()
                )
            )
        }
    }

    /** 恢复学生为正常 */
    fun setStudentActive(studentId: Long) {
        viewModelScope.launch {
            val student = studentRepository.getStudentByIdOnce(studentId) ?: return@launch
            studentRepository.update(
                student.copy(
                    status = StudentStatus.ACTIVE,
                    statusChangedAt = -1
                )
            )
        }
    }

    /**
     * 退款清零：把学生所有未退课时包按剩余次数退款并标记退款。
     * @param refundAmount 用户输入的退款总金额
     */
    fun refundAllSessions(studentId: Long, refundAmount: Double, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val packages = coursePackageRepository.getAvailablePackages(studentId)
            if (packages.isEmpty()) {
                onComplete(false)
                return@launch
            }
            // 按比例分配退款金额到各包（按剩余次数占比）
            val totalRemaining = packages.sumOf { it.remainingSessions }
            var ok = false
            if (totalRemaining > 0) {
                packages.forEach { pkg ->
                    val share = refundAmount * pkg.remainingSessions / totalRemaining
                    val success = coursePackageRepository.refundPackage(pkg, share)
                    ok = ok || success
                }
            }
            // 退款后标记为不带了（不再提示课时）
            val student = studentRepository.getStudentByIdOnce(studentId)
            if (student != null && student.status == StudentStatus.ACTIVE) {
                studentRepository.update(
                    student.copy(
                        status = StudentStatus.DROPPED,
                        statusChangedAt = System.currentTimeMillis()
                    )
                )
            }
            onComplete(ok)
        }
    }

    companion object {
        private fun advanceMonth(year: Int, month: Int): Pair<Int, Int> {
            return if (month >= 11) (year + 1) to 0 else year to (month + 1)
        }

        private fun yearMonthCompare(y1: Int, m1: Int, y2: Int, m2: Int): Int {
            return when {
                y1 != y2 -> y1.compareTo(y2)
                else -> m1.compareTo(m2)
            }
        }
    }
}
