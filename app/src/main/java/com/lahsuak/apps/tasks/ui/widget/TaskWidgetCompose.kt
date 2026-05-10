package com.lahsuak.apps.tasks.ui.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.currentState
import androidx.glance.ColorFilter
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.ui.MainActivity
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.ADD_UPDATE_TASK_DEEP_LINK
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking


object TaskWidgetCompose : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                TaskWidgetEntryPoint::class.java
            )
            val repository = entryPoint.taskWidgetRepository()
            
            // Get fresh tasks from repository
            val tasks = repository.getRecentTasks(4)

            provideContent {
                TaskWidgetUI(context = context, tasks = tasks)
            }
        } catch (e: Exception) {
            provideContent {
                TaskWidgetUI(context = context, tasks = emptyList())
            }
        }
    }
}

@SuppressLint("StringFormatInvalid")
@Composable
private fun TaskWidgetUI(
    context: Context,
    tasks: List<Task>,
    preferences: Preferences? = null,
) {
    val prefs = preferences ?: currentState<Preferences>()
    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(12.dp)
                .cornerRadius(16.dp)
        ) {
            // Header
            HeaderRow(context = context)
            Text(
                text = context.getString(R.string.pending, calculatePendingCount(tasks, prefs)),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            if (tasks.isEmpty()) {
                // Empty state
                Column(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = GlanceModifier.height(16.dp))
                    Text(
                        text = "🎯",
                        style = TextStyle(fontSize = 32.sp)
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.no_tasks_yet),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onBackground
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = context.getString(R.string.tap_to_add_first_task),
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            } else {
                // Task list
                tasks.forEach { task ->
                    TaskWidgetItem(task = task, prefs = prefs)
                    if (task != tasks.last()) {
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }
                }
                
                if (tasks.size >= 4) {
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.view_all_tasks),
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.primary
                        ),
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .clickable(actionRunCallback(OpenAppActionCallback::class.java))
                    )
                }
            }
        }
    }
}

// Helper function to calculate pending count with optimistic updates
private fun calculatePendingCount(tasks: List<Task>, prefs: Preferences): Int {
    return tasks.count { task ->
        val optimisticState = prefs[intPreferencesKey("optimistic_task_${task.id}")]
        val actualIsDone = when (optimisticState) {
            1 -> true
            0 -> false
            else -> task.isDone
        }
        !actualIsDone
    }
}

@Composable
private fun HeaderRow(context: Context) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = context.getString(R.string.app_name),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onBackground
            ),
            modifier = GlanceModifier.defaultWeight()
        )

        AddTaskButton(context)
    }
}

@Composable
private fun AddTaskButton(context: Context) {
    Box(
        modifier = GlanceModifier
            .size(40.dp)
            .background(Color(0xFF367EF2))
            .cornerRadius(20.dp)
            .clickable(actionRunCallback(AddTaskActionCallback::class.java)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_add),
            contentDescription = context.getString(R.string.add_task),
            modifier = GlanceModifier.size(18.dp),
            colorFilter = ColorFilter.tint(
                ColorProvider(Color.White)
            )
        )
    }
}

@Composable
private fun TaskWidgetItem(task: Task, prefs: Preferences) {
    // Check for optimistic state override
    val optimisticState = prefs[intPreferencesKey("optimistic_task_${task.id}")]
    val actualIsDone = when (optimisticState) {
        1 -> true
        0 -> false
        else -> task.isDone
    }
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.surfaceVariant)
            .cornerRadius(8.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Task title with strikethrough effect for completed tasks
        Text(
            text = task.title,
            style = TextStyle(
                fontSize = 14.sp,
                color = if (actualIsDone) 
                    GlanceTheme.colors.onSurfaceVariant 
                else 
                    GlanceTheme.colors.onSurface
            ),
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(
                    actionRunCallback(
                        TaskItemActionCallback::class.java,
                        actionParametersOf(ActionParameters.Key<Int>(TaskItemActionCallback.TASK_ID_KEY) to task.id)
                    )
                )
        )
    }
}

class TaskWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TaskWidgetCompose
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        
        // Update all widget instances when system requests update
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context)
        }
    }
    
    private fun updateAppWidget(
        context: Context,
    ) {
        // Trigger widget refresh
        runBlocking {
            TaskWidgetCompose.updateAll(context)
        }
    }
}

class OpenAppActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

class TaskItemActionCallback : ActionCallback {
    companion object {
        const val TASK_ID_KEY = "task_id"
    }
    
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val taskId = parameters[ActionParameters.Key<Int>(TASK_ID_KEY)] ?: return
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            "tasks://com.lahsuak.apps.tasks/edittask/$taskId".toUri(),
            context,
            MainActivity::class.java
        ).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        val flag = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else PendingIntent.FLAG_UPDATE_CURRENT
        val deepLinkPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(0, flag)
        }
        deepLinkPendingIntent.send()
    }
}

class AddTaskActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            ADD_UPDATE_TASK_DEEP_LINK.toUri(),
            context,
            MainActivity::class.java
        ).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
            putExtra(MainActivity.EXTRA_OPEN_ADD_TASK_FROM_WIDGET, true)
        }
        val flag = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else PendingIntent.FLAG_UPDATE_CURRENT
        TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(1, flag)
        }?.send()
    }
}