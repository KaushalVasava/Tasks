package com.lahsuak.apps.tasks.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lahsuak.apps.tasks.data.db.TaskDatabase
import com.lahsuak.apps.tasks.data.repository.TaskRepository
import com.lahsuak.apps.tasks.data.repository.TaskRepositoryImpl
import com.lahsuak.apps.tasks.util.AppConstants.DATABASE_NAME
import com.lahsuak.apps.tasks.util.AppConstants.REMINDER_DATA
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
//        ALTER TABLE table_name RENAME COLUMN column_name TO new_column_name
        return Room.databaseBuilder(
            app,
            TaskDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(migration1To2)
//            .fallbackToDestructiveMigration()
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

    @Singleton
    @Provides
    fun provideSharedPreferencesForReminder(@ApplicationContext app: Context): SharedPreferences =
        app.getSharedPreferences(REMINDER_DATA, Context.MODE_PRIVATE)
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope