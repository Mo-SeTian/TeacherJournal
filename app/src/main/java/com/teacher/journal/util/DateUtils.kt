package com.teacher.journal.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val zone: ZoneId = ZoneId.systemDefault()

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA)
    private val dateDisplayFormat = DateTimeFormatter.ofPattern("MM/dd", Locale.CHINA)
    private val fullDisplayFormat = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA)
    private val monthFormat = DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA)
    private val weekdayFormat = DateTimeFormatter.ofPattern("EEEE", Locale.CHINA)
    private val dayMonthFormat = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)

    private fun toLocalDate(timestamp: Long): LocalDate =
        Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()

    private fun startOfDay(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun formatDate(timestamp: Long): String = dateFormat.format(toLocalDate(timestamp))

    fun formatDateDisplay(timestamp: Long): String = dateDisplayFormat.format(toLocalDate(timestamp))

    fun formatDateFull(timestamp: Long): String = fullDisplayFormat.format(toLocalDate(timestamp))

    fun formatMonth(timestamp: Long): String = monthFormat.format(toLocalDate(timestamp))

    fun formatDayMonth(timestamp: Long): String = dayMonthFormat.format(toLocalDate(timestamp))

    fun getWeekday(timestamp: Long): String = weekdayFormat.format(toLocalDate(timestamp))

    /** 结算周期标签：如 "7月25日 - 8月24日" */
    fun formatWindow(window: Pair<Long, Long>): String =
        "${formatDayMonth(window.first)} - ${formatDayMonth(window.second - 1)}"

    /**
     * 获取指定月份的第一天零点时间戳
     */
    fun getStartOfMonth(year: Int, month: Int): Long {
        return startOfDay(LocalDate.of(year, month + 1, 1))
    }

    /**
     * 获取指定月份的最后一天 23:59:59.999 时间戳
     */
    fun getEndOfMonth(year: Int, month: Int): Long {
        return startOfDay(LocalDate.of(year, month + 1, 1).plusMonths(1)) - 1
    }

    /**
     * 结算周期（标签月 M）：从 M 月结算日零点，到 M+1 月结算日零点（左闭右开）。
     * 结算日为 1 时即自然月。
     */
    fun getSettlementWindow(year: Int, month: Int, settlementDay: Int): Pair<Long, Long> {
        val startMonth = LocalDate.of(year, month + 1, 1)
        val start = startOfDay(startMonth.withDayOfMonth(settlementDay.coerceIn(1, 28)))
        val end = startOfDay(startMonth.plusMonths(1).withDayOfMonth(settlementDay.coerceIn(1, 28)))
        return start to end
    }

    /**
     * 默认结算标签月：最近一个已结束的结算周期，但不早于学生创建日所在周期。
     * - settlementDay=25，8/5 打开 → 6月（6.25–7.24 已结束）
     * - settlementDay=25，8/30 打开 → 7月（7.25–8.24 已结束）
     * - 学生 8/6 创建，settlementDay=6，8/7 打开 → 8月不成立（8.6–9.5 未结束）→ 返回创建月
     */
    fun getDefaultSettlementMonth(settlementDay: Int, createdAt: Long = 0L): Pair<Int, Int> {
        val today = LocalDate.now(zone)
        val day = settlementDay.coerceIn(1, 28)

        // 最近一个已结束的结算周期
        val monthsAgo = if (today.dayOfMonth >= day) 1L else 2L
        val closeMonth = today.minusMonths(monthsAgo)
        var result = closeMonth.year to closeMonth.monthValue - 1

        // 不能早于学生创建日所在的结算周期
        if (createdAt > 0) {
            val created = Instant.ofEpochMilli(createdAt).atZone(zone).toLocalDate()
            // 创建日所在的结算标签月：如果创建日 < 结算日，属于上一个月的窗口
            val createdLabelMonth = if (created.dayOfMonth < day) {
                val m = created.minusMonths(1)
                m.year to m.monthValue - 1
            } else {
                created.year to created.monthValue - 1
            }
            // 取较晚的那个
            if (compareYearMonth(createdLabelMonth, result) > 0) {
                result = createdLabelMonth
            }
        }
        return result
    }

    /** 比较两个 (year, month) 对，返回正/零/负 */
    private fun compareYearMonth(a: Pair<Int, Int>, b: Pair<Int, Int>): Int {
        return if (a.first != b.first) a.first - b.first else a.second - b.second
    }

    fun getCurrentMonthRange(): Pair<Long, Long> {
        val today = LocalDate.now(zone)
        return getStartOfMonth(today.year, today.monthValue - 1) to
                getEndOfMonth(today.year, today.monthValue - 1)
    }

    /**
     * 获取今日零时时间戳
     */
    fun getTodayStart(): Long = startOfDay(LocalDate.now(zone))

    /**
     * 判断时间戳是否超过 N 天
     */
    fun isOverDays(timestamp: Long, days: Int): Boolean {
        val diff = System.currentTimeMillis() - timestamp
        return diff > days * 24 * 60 * 60 * 1000L
    }

    /**
     * 获取当前年月
     */
    fun getCurrentYearMonth(): Pair<Int, Int> {
        val today = LocalDate.now(zone)
        return today.year to today.monthValue - 1
    }

    /** 今日中文显示：如 "8月6日 周四" */
    fun formatToday(): String {
        val today = LocalDate.now(zone)
        val weekday = weekdayFormat.format(today)
        return "${today.monthValue}月${today.dayOfMonth}日 $weekday"
    }
}
