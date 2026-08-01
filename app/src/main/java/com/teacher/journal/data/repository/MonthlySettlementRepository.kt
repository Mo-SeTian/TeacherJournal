package com.teacher.journal.data.repository

import androidx.room.withTransaction
import com.teacher.journal.data.dao.EarningDao
import com.teacher.journal.data.dao.MonthlySettlementDao
import com.teacher.journal.data.dao.SessionRecordDao
import com.teacher.journal.data.database.AppDatabase
import com.teacher.journal.data.entity.Earning
import com.teacher.journal.data.entity.EarningType
import com.teacher.journal.data.entity.MonthlySettlement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonthlySettlementRepository @Inject constructor(
    private val dao: MonthlySettlementDao,
    private val sessionRecordDao: SessionRecordDao,
    private val earningDao: EarningDao,
    private val db: AppDatabase
) {
    fun getSettlementsForStudent(studentId: Long): Flow<List<MonthlySettlement>> =
        dao.getSettlementsForStudent(studentId)

    suspend fun getSettlementsForStudentOnce(studentId: Long): List<MonthlySettlement> =
        dao.getSettlementsForStudentOnce(studentId)

    suspend fun getById(id: Long): MonthlySettlement? = dao.getById(id)

    fun getUnpaidSettlements(): Flow<List<MonthlySettlement>> = dao.getUnpaidSettlements()

    fun getUnpaidCount(): Flow<Int> = dao.getUnpaidCount()

    suspend fun getSettlementForMonth(studentId: Long, year: Int, month: Int): MonthlySettlement? =
        dao.getSettlementForMonth(studentId, year, month)

    /**
     * 创建结算：事务里插入结算、关联上课记录，若已收款则写入收入（带 settlementId）。
     */
    suspend fun createSettlement(
        settlement: MonthlySettlement,
        recordIds: List<Long>
    ): Long = db.withTransaction {
        val id = dao.insert(settlement)
        if (recordIds.isNotEmpty()) {
            sessionRecordDao.updateSettlementId(recordIds, id)
        }
        if (settlement.isPaid) {
            earningDao.insert(
                Earning(
                    studentId = settlement.studentId,
                    type = EarningType.MONTHLY_SETTLEMENT,
                    amount = settlement.totalAmount,
                    settlementId = id,
                    date = settlement.createdAt,
                    notes = settlement.notes
                )
            )
        }
        id
    }

    suspend fun update(settlement: MonthlySettlement) = dao.update(settlement)

    /**
     * 标记结算已收款（幂等）：已收款或已有收入记录则不再重复入账。
     * @return true 表示本次执行了收款入账。
     */
    suspend fun markSettlementAsPaid(settlement: MonthlySettlement): Boolean = db.withTransaction {
        if (settlement.isPaid) return@withTransaction false
        if (earningDao.getBySettlementId(settlement.id) != null) return@withTransaction false
        dao.markAsPaid(settlement.id)
        earningDao.insert(
            Earning(
                studentId = settlement.studentId,
                type = EarningType.MONTHLY_SETTLEMENT,
                amount = settlement.totalAmount,
                settlementId = settlement.id,
                date = System.currentTimeMillis(),
                notes = settlement.notes
            )
        )
        true
    }

    /**
     * 编辑结算：更新字段并同步收入（isPaid=true 写入/更新收入，false 删除收入）。
     */
    suspend fun updateSettlementAndSyncEarning(
        settlement: MonthlySettlement,
        amount: Double,
        isPaid: Boolean,
        notes: String
    ) = db.withTransaction {
        dao.update(settlement.copy(totalAmount = amount, isPaid = isPaid, notes = notes))
        earningDao.deleteBySettlementId(settlement.id)
        if (isPaid) {
            earningDao.insert(
                Earning(
                    studentId = settlement.studentId,
                    type = EarningType.MONTHLY_SETTLEMENT,
                    amount = amount,
                    settlementId = settlement.id,
                    date = System.currentTimeMillis(),
                    notes = notes
                )
            )
        }
    }

    /** 删除结算：解除记录关联并删除关联收入。 */
    suspend fun deleteSettlementAndUnlink(settlement: MonthlySettlement) = db.withTransaction {
        earningDao.deleteBySettlementId(settlement.id)
        sessionRecordDao.unlinkSettlement(settlement.id)
        dao.delete(settlement)
    }

    suspend fun delete(settlement: MonthlySettlement) = dao.delete(settlement)
}
