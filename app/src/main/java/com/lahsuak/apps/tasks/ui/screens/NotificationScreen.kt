package com.lahsuak.apps.tasks.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.Notification
import com.lahsuak.apps.tasks.ui.viewmodel.NotificationViewModel
import com.lahsuak.apps.tasks.util.DateUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(notificationViewModel: NotificationViewModel, navController: NavController) {

    var isChecked by rememberSaveable {
        mutableStateOf(false)
    }
    val notifications by notificationViewModel.notifications.collectAsState(
        initial = emptyList()
    )
    val tempNotifications by remember {
        mutableStateOf(
            if (isChecked)
                notifications.sortedBy { it.date }
            else
                notifications.sortedByDescending { it.date }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.notifications)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painterResource(id = R.drawable.ic_back),
                            stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .toggleable(isChecked, onValueChange = {
                            isChecked = it
                        })
                        .semantics(mergeDescendants = true) { },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(id = R.string.sort_by_old_to_new))
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = isChecked, onCheckedChange = {
                        isChecked = it
                    })
                }
            }
            items(notifications, key = {
                it.id
            }) {
                NotificationItem(notification = it)
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Card(Modifier.padding(vertical = 8.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(notification.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                DateUtil.getDate(notification.date),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview
@Composable
fun PreviewNotificationScreen() {
    val viewModel: NotificationViewModel = viewModel()
    NotificationScreen(
        viewModel,
        rememberNavController()
    )
}