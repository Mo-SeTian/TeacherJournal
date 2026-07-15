package com.teacher.journal.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.teacher.journal.data.dao.*
import com.teacher.journal.data.entity.*

@Database(
    entities = [
        Student::class,
        CoursePackage::class,
        SessionRecord::class,
        Earning::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun coursePackageDao(): CoursePackageDao
    abstract fun sessionRecordDao(): SessionRecordDao
    abstract fun earningDao(): EarningDao
}
