package com.lahsuak.apps.tasks.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lahsuak.apps.tasks.data.model.Notification
import com.lahsuak.apps.tasks.util.DateUtil

@Composable
fun NotificationScreen(notifications: List<Notification>) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(notifications) {
            NotificationItem(notification = it)
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(notification.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(DateUtil.getDate(notification.date), fontSize = 12.sp)
    }
}

@Preview
@Composable
fun PreviewNotificationScreen() {
    NotificationScreen(
        listOf(
            Notification(1, 1, "Alarm", System.currentTimeMillis())
        )
    )
}