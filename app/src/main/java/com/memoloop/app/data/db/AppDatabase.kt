package com.memoloop.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.memoloop.app.data.model.ListeningResult
import com.memoloop.app.data.model.MonthlyScore
import com.memoloop.app.data.model.QuizResult
import com.memoloop.app.data.model.ReviewSession

@Database(
    entities = [ReviewSession::class, QuizResult::class, MonthlyScore::class, ListeningResult::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reviewSessionDao(): ReviewSessionDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun monthlyScoreDao(): MonthlyScoreDao
    abstract fun listeningResultDao(): ListeningResultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS quiz_results (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateMillis INTEGER NOT NULL,
                        dateKey TEXT NOT NULL,
                        correctCount INTEGER NOT NULL,
                        totalCount INTEGER NOT NULL,
                        passed INTEGER NOT NULL,
                        pointsEarned INTEGER NOT NULL
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS monthly_scores (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        yearMonth TEXT NOT NULL,
                        totalPoints INTEGER NOT NULL
                    )"""
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS listening_results (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateMillis INTEGER NOT NULL,
                        dateKey TEXT NOT NULL,
                        correctCount INTEGER NOT NULL,
                        totalCount INTEGER NOT NULL,
                        passed INTEGER NOT NULL,
                        pointsEarned INTEGER NOT NULL
                    )"""
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "memoloop.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
