package com.lahsuak.apps.tasks.ui.screens

import android.content.res.Configuration
import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.ui.screens.components.CircularProgressStatus
import com.lahsuak.apps.tasks.ui.theme.TaskAppTheme
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.DateUtil
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(navController: NavController, taskViewModel: TaskViewModel) {
    val tasks by taskViewModel.tasksFlow.collectAsState(initial = emptyList())
    var selectedDate: Long? by rememberSaveable {
        mutableStateOf(null)
    }
    val filterTasks by remember {
        derivedStateOf {
            if (selectedDate == null) {
                getTasksByDate(tasks, true, System.currentTimeMillis())
            } else {
                getTasksByDate(tasks, false, selectedDate!!)
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.overview)) },
            navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        painterResource(R.drawable.ic_back),
                        stringResource(R.string.back)
                    )
                }
            }
        )
    }) { paddingValues ->
        LazyVerticalStaggeredGrid(
            modifier = Modifier.padding(paddingValues),
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column(
                    Modifier
                        .fillMaxWidth()
                ) {

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Card(
                            Modifier.weight(0.5f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                stringResource(R.string.pending, tasks.count { !it.isDone }),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Card(
                            Modifier.weight(0.5f),
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(R.color.light_green)
                            )
                        )
                        {
                            Text(
                                stringResource(R.string.completed, tasks.count { it.isDone }),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
                            )

                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    AndroidView(
                        factory = { CalendarView(it) },
                        modifier = Modifier.wrapContentWidth(),
                        update = { views ->
                            selectedDate?.let {
                                views.date = it
                            }
                            views.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
                                val calendar = Calendar.getInstance()
                                calendar.set(year, month, dayOfMonth)
                                selectedDate = calendar.timeInMillis
                                selectedDate?.let {
                                    calendarView.date = it
                                }
                            }
                        }
                    )
                }
            }
            items(filterTasks, key = {
                it.id
            }) {
                TaskOverviewItem(it)
            }
        }
    }
}

@Composable
fun TaskOverviewItem(task: Task) {
    Card(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(TaskApp.categoryTypes[task.color].color).copy(alpha = 0.30f)
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                task.title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
            )
            if (task.subTaskList != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(task.subTaskList!!)
            }
            if (task.progress != -1f) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressStatus(
                    progress = task.progress,
                    color = Color(TaskApp.categoryTypes[task.color].color),
                    trackColor = Color.LightGray,
                    size = 32.dp
                )
            }
        }
    }
}

private fun getTasksByDate(
    list: List<Task>,
    isNew: Boolean,
    selectedDate: Long,
): List<Task> {
    val selectedDateStr = if (isNew) {
        DateUtil.getDateForOverview(Calendar.getInstance().timeInMillis)
    } else {
        DateUtil.getDateForOverview(selectedDate)
    }
    return list.filter {
        val taskDate = DateUtil.getDateForOverview(it.startDate!!)
        taskDate == selectedDateStr
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewOverviewScreen() {
    val viewModel: TaskViewModel = viewModel()
    TaskAppTheme {
        Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
            OverviewScreen(rememberNavController(), viewModel)
        }
    }
}