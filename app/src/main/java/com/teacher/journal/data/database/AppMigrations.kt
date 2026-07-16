package com.teacher.journal.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room 数据库迁移 — 绝对不丢失数据
 */
object AppMigrations {

    /** v1 → v2: 新增月结算表 + session_records 增加 settlementId */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS monthly_settlements (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    studentId INTEGER NOT NULL,
                    year INTEGER NOT NULL,
                    month INTEGER NOT NULL,
                    sessionCount INTEGER NOT NULL,
                    totalAmount REAL NOT NULL,
                    isPaid INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    notes TEXT NOT NULL DEFAULT '',
                    FOREIGN KEY (studentId) REFERENCES students(id) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_monthly_settlements_studentId ON monthly_settlements(studentId)")
            db.execSQL("ALTER TABLE session_records ADD COLUMN settlementId INTEGER NOT NULL DEFAULT -1")
            db.execSQL("ALTER TABLE students ADD COLUMN monthlyRate REAL NOT NULL DEFAULT 0.0")
        }
    }

    /** v2 → v3: Student 新增 settlementDay 字段 */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE students ADD COLUMN settlementDay INTEGER NOT NULL DEFAULT 1")
        }
    }

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
