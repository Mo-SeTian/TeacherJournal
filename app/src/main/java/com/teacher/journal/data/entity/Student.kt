package com.teacher.journal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    /** 备注 */
    val notes: String = "",
    /** 创建时间（毫秒时间戳） */
    val createdAt: Long = System.currentTimeMillis()
)
