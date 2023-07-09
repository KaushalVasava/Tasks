package com.lahsuak.apps.tasks.data.db

import androidx.room.*
import com.lahsuak.apps.tasks.data.model.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    fun getAllNotifications(
        isAscOrder: Boolean,
    ): Flow<List<Notification>> {
        return if (isAscOrder) {
            getNotificationsByCreatedDate()
        } else {
            getNotificationsByLatestDate()
        }
    }

    @Query(
        "SELECT * FROM notification_table ORDER BY date ASC"
    )
    fun getNotificationsByCreatedDate(): Flow<List<Notification>>

    @Query(
        "SELECT * FROM notification_table ORDER BY date DESC"
    )
    fun getNotificationsByLatestDate(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: Notification)

    @Delete
    suspend fun delete(notification: Notification)


    @Query("SELECT * FROM notification_table WHERE id=:notificationId")
    suspend fun getById(notificationId: Int): Notification

    @Query("DELETE FROM notification_table")
    suspend fun deleteAllNotifications()
}