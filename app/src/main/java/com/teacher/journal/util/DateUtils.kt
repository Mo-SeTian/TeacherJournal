package com.teacher.journal.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val dateDisplayFormat = SimpleDateFormat("MM/dd", Locale.CHINA)
    private val fullDisplayFormat = SimpleDateFormat("yyyy年M月d日", Locale.CHINA)
    private val monthFormat = SimpleDateFormat("yyyy年M月", Locale.CHINA)
    private val weekdayFormat = SimpleDateFormat("EEEE", Locale.CHINA)

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    fun formatDateDisplay(timestamp: Long): String = dateDisplayFormat.format(Date(timestamp))

    fun formatDateFull(timestamp: Long): String = fullDisplayFormat.format(Date(timestamp))

    fun formatMonth(timestamp: Long): String = monthFormat.format(Date(timestamp))

    fun getWeekday(timestamp: Long): String = weekdayFormat.format(Date(timestamp))

    /**
     * 获取指定月份的第一天零点时间戳
     */
    fun getStartOfMonth(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * 获取指定月份的最后一天 23:59:59 时间戳
     */
    fun getEndOfMonth(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    /**
     * 获取当前月份的起止时间戳
     */
    fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        return getStartOfMonth(year, month) to getEndOfMonth(year, month)
    }

    /**
     * 获取今日零时时间戳
     */
    fun getTodayStart(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

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
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) to calendar.get(Calendar.MONTH)
    }
}
