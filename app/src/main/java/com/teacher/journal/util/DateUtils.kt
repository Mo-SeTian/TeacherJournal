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
     * 默认结算标签月：最近一个已结束的结算周期。
     * 例如结算日 25 号，8 月 5 日打开 → 6 月（6.25–7.24 已结束）；8 月 30 日打开 → 7 月。
     */
    fun getDefaultSettlementMonth(settlementDay: Int): Pair<Int, Int> {
        val today = LocalDate.now(zone)
        val monthsAgo = if (today.dayOfMonth >= settlementDay.coerceIn(1, 28)) 1L else 2L
        val closeMonth = today.minusMonths(monthsAgo)
        return closeMonth.year to closeMonth.monthValue - 1
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
