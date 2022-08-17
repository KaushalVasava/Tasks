package com.lahsuak.apps.mytask.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lahsuak.apps.mytask.data.db.TaskDatabase
import com.lahsuak.apps.mytask.data.repository.TodoRepository
import com.lahsuak.apps.mytask.data.repository.TodoRepositoryImpl
import com.lahsuak.apps.mytask.data.util.Constants.DATABASE_NAME
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
        return Room.databaseBuilder(
            app,
            TaskDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(migration_1_2)
            //.allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideTodoRepository(db: TaskDatabase): TodoRepository {
        return TodoRepositoryImpl(db.dao)
    }

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope