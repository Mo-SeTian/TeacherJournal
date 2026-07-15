package com.teacher.journal.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 课时包实体 — 仅预付费学生使用
 */
@Entity(
    tableName = "course_packages",
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
data class CoursePackage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 所属学生 ID */
    val studentId: Long,
    /** 购买次数 */
    val sessionsPurchased: Int,
    /** 已使用次数 */
    val usedCount: Int = 0,
    /** 购买金额 */
    val amount: Double = 0.0,
    /** 购买日期（毫秒时间戳） */
    val purchaseDate: Long = System.currentTimeMillis(),
    /** 备注 */
    val notes: String = ""
) {
    /** 剩余次数 */
    val remainingSessions: Int get() = sessionsPurchased - usedCount

    /** 是否已用完 */
    val isExhausted: Boolean get() = remainingSessions <= 0
}
