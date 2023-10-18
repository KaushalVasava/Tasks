package com.lahsuak.apps.tasks.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lahsuak.apps.tasks.data.model.Notification
import com.lahsuak.apps.tasks.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private var isAscOrder = false

    private val _notifications = repository.getAllNotifications(isAscOrder)
    val notifications = _notifications

    fun insert(notification: Notification) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertNotification(notification)
    }

    fun delete(notification: Notification) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNotification(notification)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllNotifications()
    }

    suspend fun getById(id: Int): Notification {
        return repository.getById(id)
    }
}
