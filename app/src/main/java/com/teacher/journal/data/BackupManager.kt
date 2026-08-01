package com.teacher.journal.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.teacher.journal.data.dao.CoursePackageDao
import com.teacher.journal.data.dao.EarningDao
import com.teacher.journal.data.dao.MonthlySettlementDao
import com.teacher.journal.data.dao.SessionRecordDao
import com.teacher.journal.data.dao.StudentDao
import com.teacher.journal.data.database.AppDatabase
import com.teacher.journal.data.entity.CoursePackage
import com.teacher.journal.data.entity.Earning
import com.teacher.journal.data.entity.EarningType
import com.teacher.journal.data.entity.MonthlySettlement
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.data.entity.SessionRecord
import com.teacher.journal.data.entity.Student
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class ImportResult(
    val students: Int,
    val records: Int,
    val packages: Int,
    val earnings: Int,
    val settlements: Int
)

/**
 * 数据备份 / 恢复：导出为 JSON 文件，导入时整体替换本地数据（导入前请先导出备份）。
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val studentDao: StudentDao,
    private val coursePackageDao: CoursePackageDao,
    private val sessionRecordDao: SessionRecordDao,
    private val earningDao: EarningDao,
    private val monthlySettlementDao: MonthlySettlementDao
) {

    /** @return 导出的学生数量 */
    suspend fun exportTo(uri: Uri): Int {
        val students = studentDao.getAllOnce()
        val packages = coursePackageDao.getAllOnce()
        val records = sessionRecordDao.getAllOnce()
        val earnings = earningDao.getAllOnce()
        val settlements = monthlySettlementDao.getAllOnce()

        val json = JSONObject().apply {
            put("app", "TeacherJournal")
            put("formatVersion", 1)
            put("exportedAt", System.currentTimeMillis())
            put("students", JSONArray().apply { students.forEach { put(it.toJson()) } })
            put("coursePackages", JSONArray().apply { packages.forEach { put(it.toJson()) } })
            put("sessionRecords", JSONArray().apply { records.forEach { put(it.toJson()) } })
            put("earnings", JSONArray().apply { earnings.forEach { put(it.toJson()) } })
            put("monthlySettlements", JSONArray().apply { settlements.forEach { put(it.toJson()) } })
        }

        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(json.toString(2).toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("无法打开目标文件")
        return students.size
    }

    suspend fun importFrom(uri: Uri): ImportResult {
        val text = context.contentResolver.openInputStream(uri)?.use {
            it.readBytes().toString(Charsets.UTF_8)
        } ?: throw IllegalStateException("无法读取备份文件")

        val json = JSONObject(text)
        require(json.optString("app") == "TeacherJournal") { "不是授业札记的备份文件" }

        val students = parseStudents(json.getJSONArray("students"))
        val packages = parsePackages(json.getJSONArray("coursePackages"))
        val records = parseRecords(json.getJSONArray("sessionRecords"))
        val earnings = parseEarnings(json.getJSONArray("earnings"))
        val settlements = parseSettlements(json.getJSONArray("monthlySettlements"))

        db.withTransaction {
            studentDao.deleteAll()

            val studentIds = mutableMapOf<Long, Long>()
            students.forEach {
                val id = studentDao.insert(it)
                studentIds[it.id] = id
            }

            val packageIds = mutableMapOf<Long, Long>()
            packages.forEach {
                val id = coursePackageDao.insert(it.copy(studentId = studentIds[it.studentId] ?: it.studentId))
                packageIds[it.id] = id
            }

            val settlementIds = mutableMapOf<Long, Long>()
            settlements.forEach {
                val id = monthlySettlementDao.insert(it.copy(studentId = studentIds[it.studentId] ?: it.studentId))
                settlementIds[it.id] = id
            }

            val recordIds = mutableMapOf<Long, Long>()
            records.forEach {
                val id = sessionRecordDao.insert(
                    it.copy(
                        studentId = studentIds[it.studentId] ?: it.studentId,
                        coursePackageId = if (it.coursePackageId > 0) packageIds[it.coursePackageId] ?: -1 else it.coursePackageId,
                        settlementId = if (it.settlementId > 0) settlementIds[it.settlementId] ?: -1 else it.settlementId
                    )
                )
                recordIds[it.id] = id
            }

            earnings.forEach {
                earningDao.insert(
                    it.copy(
                        studentId = studentIds[it.studentId] ?: it.studentId,
                        sessionId = if (it.sessionId > 0) recordIds[it.sessionId] ?: -1 else -1,
                        packageId = if (it.packageId > 0) packageIds[it.packageId] ?: -1 else it.packageId,
                        settlementId = if (it.settlementId > 0) settlementIds[it.settlementId] ?: -1 else it.settlementId
                    )
                )
            }
        }

        return ImportResult(
            students = students.size,
            records = records.size,
            packages = packages.size,
            earnings = earnings.size,
            settlements = settlements.size
        )
    }

    // ── JSON 解析 ──

    private fun Student.toJson() = JSONObject().apply {
        put("id", id); put("name", name); put("phone", phone)
        put("subject", subject); put("location", location)
        put("paymentType", paymentType.name)
        put("monthlyRate", monthlyRate); put("settlementDay", settlementDay)
        put("notes", notes); put("createdAt", createdAt)
    }

    private fun CoursePackage.toJson() = JSONObject().apply {
        put("id", id); put("studentId", studentId)
        put("sessionsPurchased", sessionsPurchased); put("usedCount", usedCount)
        put("amount", amount); put("refundedSessions", refundedSessions)
        put("refundAmount", refundAmount); put("refundDate", refundDate)
        put("purchaseDate", purchaseDate); put("notes", notes)
    }

    private fun SessionRecord.toJson() = JSONObject().apply {
        put("id", id); put("studentId", studentId); put("date", date)
        put("startTime", startTime); put("endTime", endTime)
        put("location", location); put("content", content)
        put("paymentStatus", paymentStatus.name); put("amount", amount)
        put("coursePackageId", coursePackageId); put("settlementId", settlementId)
        put("createdAt", createdAt)
    }

    private fun Earning.toJson() = JSONObject().apply {
        put("id", id); put("studentId", studentId); put("type", type.name)
        put("amount", amount); put("date", date); put("sessionId", sessionId)
        put("packageId", packageId); put("settlementId", settlementId); put("notes", notes)
    }

    private fun MonthlySettlement.toJson() = JSONObject().apply {
        put("id", id); put("studentId", studentId); put("year", year); put("month", month)
        put("sessionCount", sessionCount); put("totalAmount", totalAmount)
        put("isPaid", isPaid); put("createdAt", createdAt); put("notes", notes)
    }

    private fun parseStudents(arr: JSONArray): List<Student> =
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Student(
                id = o.getLong("id"),
                name = o.getString("name"),
                phone = o.optString("phone", ""),
                subject = o.optString("subject", ""),
                location = o.optString("location", ""),
                paymentType = runCatching { PaymentType.valueOf(o.optString("paymentType")) }
                    .getOrDefault(PaymentType.PREPAID),
                monthlyRate = o.optDouble("monthlyRate", 0.0),
                settlementDay = o.optInt("settlementDay", 1),
                notes = o.optString("notes", ""),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }

    private fun parsePackages(arr: JSONArray): List<CoursePackage> =
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            CoursePackage(
                id = o.getLong("id"),
                studentId = o.getLong("studentId"),
                sessionsPurchased = o.getInt("sessionsPurchased"),
                usedCount = o.optInt("usedCount", 0),
                amount = o.optDouble("amount", 0.0),
                refundedSessions = o.optInt("refundedSessions", 0),
                refundAmount = o.optDouble("refundAmount", 0.0),
                refundDate = o.optLong("refundDate", -1),
                purchaseDate = o.optLong("purchaseDate", System.currentTimeMillis()),
                notes = o.optString("notes", "")
            )
        }

    private fun parseRecords(arr: JSONArray): List<SessionRecord> =
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            SessionRecord(
                id = o.getLong("id"),
                studentId = o.getLong("studentId"),
                date = o.getLong("date"),
                startTime = o.optString("startTime", ""),
                endTime = o.optString("endTime", ""),
                location = o.optString("location", ""),
                content = o.optString("content", ""),
                paymentStatus = runCatching { PaymentStatus.valueOf(o.optString("paymentStatus")) }
                    .getOrDefault(PaymentStatus.PAID),
                amount = o.optDouble("amount", 0.0),
                coursePackageId = o.optLong("coursePackageId", -1),
                settlementId = o.optLong("settlementId", -1),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }

    private fun parseEarnings(arr: JSONArray): List<Earning> =
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Earning(
                id = o.getLong("id"),
                studentId = o.getLong("studentId"),
                type = runCatching { EarningType.valueOf(o.optString("type")) }
                    .getOrDefault(EarningType.SESSION_PAYMENT),
                amount = o.optDouble("amount", 0.0),
                date = o.optLong("date", System.currentTimeMillis()),
                sessionId = o.optLong("sessionId", -1),
                packageId = o.optLong("packageId", -1),
                settlementId = o.optLong("settlementId", -1),
                notes = o.optString("notes", "")
            )
        }

    private fun parseSettlements(arr: JSONArray): List<MonthlySettlement> =
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            MonthlySettlement(
                id = o.getLong("id"),
                studentId = o.getLong("studentId"),
                year = o.getInt("year"),
                month = o.getInt("month"),
                sessionCount = o.optInt("sessionCount", 0),
                totalAmount = o.optDouble("totalAmount", 0.0),
                isPaid = o.optBoolean("isPaid", false),
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                notes = o.optString("notes", "")
            )
        }
}
