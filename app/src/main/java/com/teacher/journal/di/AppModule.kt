package com.teacher.journal.di

import android.content.Context
import androidx.room.Room
import com.teacher.journal.data.database.AppDatabase
import com.teacher.journal.data.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "teacher_journal.db"
        ).build()
    }

    @Provides
    fun provideStudentDao(db: AppDatabase): StudentDao = db.studentDao()

    @Provides
    fun provideCoursePackageDao(db: AppDatabase): CoursePackageDao = db.coursePackageDao()

    @Provides
    fun provideSessionRecordDao(db: AppDatabase): SessionRecordDao = db.sessionRecordDao()

    @Provides
    fun provideEarningDao(db: AppDatabase): EarningDao = db.earningDao()
}
