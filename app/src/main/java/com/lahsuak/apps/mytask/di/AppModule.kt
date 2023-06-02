package com.lahsuak.apps.mytask.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lahsuak.apps.mytask.data.db.TaskDatabase
import com.lahsuak.apps.mytask.data.repository.TaskRepository
import com.lahsuak.apps.mytask.data.repository.TaskRepositoryImpl
import com.lahsuak.apps.mytask.util.AppConstants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(app: Application): TaskDatabase {
        val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE task_table ADD COLUMN subtask TEXT")
            }
        }
        val migration_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE task_table ADD COLUMN date INTEGER")
            }
        }
        val migration_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE sub_task_table ADD COLUMN dateTime INTEGER")
            }
        }
        val migration_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `task_temporary` (" +
                            "`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `status` INTEGER NOT NULL, `importance` INTEGER NOT NULL," +
                            "`reminder` INTEGER,`progress` REAL NOT NULL, `subtask` TEXT,`date` INTEGER, PRIMARY KEY(`id`))"
                )
                database.execSQL(
                    "INSERT INTO task_temporary(id, title, status, importance, reminder, progress, subtask ,date)" +
                            " SELECT id, title, status, importance, reminder, progress, subtask ,date FROM task_table"
                )
                database.execSQL("DROP TABLE task_table")
                database.execSQL("ALTER TABLE task_temporary RENAME TO task_table")
            }
        }
        val migration_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE task_table ADD COLUMN color INTEGER NOT NULL DEFAULT 0")
            }
        }
        val migration_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE sub_task_table ADD COLUMN reminder INTEGER")
            }
        }
        return Room.databaseBuilder(
            app,
            TaskDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(migration_1_2)
            .addMigrations(migration_2_3)
            .addMigrations(migration_3_4)
            .addMigrations(migration_4_5)
            .addMigrations(migration_5_6)
            .addMigrations(migration_6_7)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTodoRepository(db: TaskDatabase): TaskRepository {
        return TaskRepositoryImpl(db.dao)
    }

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope