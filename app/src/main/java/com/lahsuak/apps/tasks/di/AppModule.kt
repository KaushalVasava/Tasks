package com.lahsuak.apps.tasks.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lahsuak.apps.tasks.data.db.TaskDatabase
import com.lahsuak.apps.tasks.data.repository.NotificationRepository
import com.lahsuak.apps.tasks.data.repository.NotificationRepositoryImpl
import com.lahsuak.apps.tasks.data.repository.TaskRepository
import com.lahsuak.apps.tasks.data.repository.TaskRepositoryImpl
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppConstants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideTaskDatabase(app: Application): TaskDatabase {
        val migration1To2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `task_temporary` (" +
                            "`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `status` INTEGER NOT NULL, `importance` INTEGER NOT NULL," +
                            "`reminder` INTEGER,`progress` REAL NOT NULL, `subtask` TEXT,`color` INTEGER NOT NULL,`startDate` INTEGER, PRIMARY KEY(`id`))"
                )
                database.execSQL(
                    "INSERT INTO task_temporary(id, title, status, importance, reminder, progress, subtask, color, startDate)" +
                            " SELECT id, title, status, importance, reminder, progress, subtask, color, date FROM task_table"
                )
                database.execSQL("DROP TABLE task_table")
                database.execSQL("ALTER TABLE task_temporary RENAME TO task_table")
                database.execSQL("ALTER TABLE task_table ADD COLUMN endDate INTEGER")
            }
        }
        val migration2To3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS notification_table (id INTEGER NOT NULL, taskId INTEGER NOT NULL," +
                            "title TEXT NOT NULL, date INTEGER NOT NULL, PRIMARY KEY(id))"
                )
            }
        }
        val migration3To4 =  object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // this above android 10
//                database.execSQL("ALTER TABLE task_table RENAME COLUMN startDate TO date")
               // this for all versions
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `task_temporary` (" +
                            "`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `status` INTEGER NOT NULL, `importance` INTEGER NOT NULL," +
                            "`reminder` INTEGER,`progress` REAL NOT NULL, `subtask` TEXT,`color` INTEGER NOT NULL,`start_date` INTEGER, `endDate` INTEGER, PRIMARY KEY(`id`))"
                )
                database.execSQL(
                    "INSERT INTO task_temporary(id, title, status, importance, reminder, progress, subtask, color, start_date, endDate)" +
                            " SELECT id, title, status, importance, reminder, progress, subtask, color, startDate, endDate FROM task_table"
                )
                database.execSQL("DROP TABLE task_table")
                database.execSQL("ALTER TABLE task_temporary RENAME TO task_table")

            }
        }
        return Room.databaseBuilder(
            app,
            TaskDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(migration1To2)
            .addMigrations(migration2To3)
            .addMigrations(migration3To4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(db: TaskDatabase): TaskRepository {
        return TaskRepositoryImpl(db.dao)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(database: TaskDatabase): NotificationRepository {
        return NotificationRepositoryImpl(database.notificationDao)
    }

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    @Named(AppConstants.SharedPreference.DAILY_NOTIFICATION)
    fun provideSharedPreferencesForNotification(@ApplicationContext app: Context): SharedPreferences =
        app.getSharedPreferences(
            AppConstants.SharedPreference.DAILY_NOTIFICATION,
            Context.MODE_PRIVATE
        )

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope