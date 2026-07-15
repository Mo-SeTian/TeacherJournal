package com.teacher.journal.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 月结算记录 — 仅月结算模式学生使用
 * 将一个学生某个月的所有上课记录归入一次结算
 */
@Entity(
    tableName = "monthly_settlements",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId")]
)
data class MonthlySettlement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 所属学生 ID */
    val studentId: Long,
    /** 结算年份 */
    val year: Int,
    /** 结算月份（0-11） */
    val month: Int,
    /** 本月上课次数 */
    val sessionCount: Int,
    /** 结算金额 */
    val totalAmount: Double,
    /** 是否已收款 */
    val isPaid: Boolean = false,
    /** 创建日期（毫秒时间戳） */
    val createdAt: Long = System.currentTimeMillis(),
    /** 备注 */
    val notes: String = ""
)
