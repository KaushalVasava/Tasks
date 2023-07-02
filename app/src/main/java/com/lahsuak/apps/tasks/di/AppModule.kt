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
    fun provideTodoDatabase(app: Application): TaskDatabase {
        val migration1To2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE task_table RENAME COLUMN date to startDate")
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
        return Room.databaseBuilder(
            app,
            TaskDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(migration1To2)
            .addMigrations(migration2To3)
//            .fallbackToDestructiveMigration()
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

//    @Singleton
//    @Provides
//    fun provideDailyNotification(sharedPref: SharedPreferences) =
//        sharedPref.getBoolean(AppConstants.SharedPreference.DAILY_NOTIFICATION_KEY, false)

    @Provides
    @Singleton
    @Named(AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE)
    fun provideSharedPreferencesForLanguage(@ApplicationContext app: Context): SharedPreferences =
        app.getSharedPreferences(
            AppConstants.SharedPreference.DAILY_NOTIFICATION,
            Context.MODE_PRIVATE
        )

//    @Singleton
//    @Provides
//    fun provideLanguage(sharedPref: SharedPreferences) =
//        sharedPref.getBoolean(AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE_KEY, false)
//    @Singleton
//    @Provides
//    fun provideLanguageTag(sharedPref: SharedPreferences) =
//        sharedPref.getBoolean(AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY, false)
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope