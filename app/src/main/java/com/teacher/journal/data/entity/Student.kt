package com.teacher.journal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 学生状态
 */
enum class StudentStatus {
    /** 正常上课 */ ACTIVE,
    /** 先冻结（课时保留，后续可恢复） */ FROZEN,
    /** 不带了（结算/课时不再提示） */ DROPPED
}

/**
 * 付费类型
 */
enum class PaymentType {
    /** 预付费 — 购买课时包，每次上课扣减 */
    PREPAID,
    /** 按次付费 — 先上课后付款 */
    PER_SESSION,
    /** 月结算 — 按月统计课时，月底统一结算 */
    MONTHLY
}

/**
 * 学生实体
 */
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 姓名 */
    val name: String,
    /** 联系电话 */
    val phone: String = "",
    /** 所学科目 */
    val subject: String = "",
    /** 上课地点 */
    val location: String = "",
    /** 付费类型 */
    val paymentType: PaymentType = PaymentType.PREPAID,
    /** 月薪/月费（月结算模式使用） */
    val monthlyRate: Double = 0.0,
    /** 每月结算日（1-28，月结算模式使用） */
    val settlementDay: Int = 1,
    /** 学生状态：正常 / 冻结 / 不带了 */
    val status: StudentStatus = StudentStatus.ACTIVE,
    /** 状态变更日期（毫秒时间戳，用于判断冻结/不带了之后不再提示结算） */
    val statusChangedAt: Long = -1,
    /** 备注 */
    val notes: String = "",
    /** 创建时间（毫秒时间戳） */
    val createdAt: Long = System.currentTimeMillis()
) {
    /** 状态变更后是否影响某个月结算提醒（true = 不再提示） */
    fun isInactiveAfter(year: Int, month: Int): Boolean {
        if (status == StudentStatus.ACTIVE) return false
        if (statusChangedAt <= 0) return false
        val cutoff = java.time.LocalDate.of(year, month + 1, 1).atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        return statusChangedAt >= cutoff
    }
}
