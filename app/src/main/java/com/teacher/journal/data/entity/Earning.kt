package com.teacher.journal.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 收入类型
 */
enum class EarningType {
    /** 课时包购买 */
    PACKAGE_PURCHASE,
    /** 单次课时费 */
    SESSION_PAYMENT
}

/**
 * 收入记录实体
 */
@Entity(
    tableName = "earnings",
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
data class Earning(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 所属学生 ID */
    val studentId: Long,
    /** 收入类型 */
    val type: EarningType,
    /** 金额 */
    val amount: Double,
    /** 日期（毫秒时间戳） */
    val date: Long = System.currentTimeMillis(),
    /** 关联上课记录 ID（单次付费时使用，-1 表示不关联） */
    val sessionId: Long = -1,
    /** 关联课时包 ID（购买课时包时使用，-1 表示不关联） */
    val packageId: Long = -1,
    /** 备注 */
    val notes: String = ""
)
