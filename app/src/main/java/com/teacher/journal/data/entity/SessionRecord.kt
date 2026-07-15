package com.teacher.journal.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 收费状态
 */
enum class PaymentStatus {
    /** 已收费 */
    PAID,
    /** 待收费 */
    UNPAID
}

/**
 * 上课记录实体
 */
@Entity(
    tableName = "session_records",
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
data class SessionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 所属学生 ID */
    val studentId: Long,
    /** 上课日期（毫秒时间戳，只取日期部分） */
    val date: Long,
    /** 开始时间（格式 HH:mm） */
    val startTime: String,
    /** 结束时间（格式 HH:mm） */
    val endTime: String,
    /** 上课地点 */
    val location: String = "",
    /** 课程内容 / 备注 */
    val content: String = "",
    /** 收费状态 */
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    /** 单次课时费金额（按次付费学生使用） */
    val amount: Double = 0.0,
    /** 关联的课时包 ID（预付费学生使用，-1 表示不关联） */
    val coursePackageId: Long = -1,
    /** 创建时间（毫秒时间戳） */
    val createdAt: Long = System.currentTimeMillis()
)
