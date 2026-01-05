package app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import java.util.concurrent.TimeUnit
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): ListenableWorker.Result {
        val context = applicationContext
        val allTodos = loadTodos(context)

        val currentTime = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000L
        
        val upcomingTasks = allTodos.filter { todo ->
            !todo.isDone && 
            todo.deadline > currentTime && 
            todo.deadline <= (currentTime + oneDayInMillis)
        }

        if (upcomingTasks.isNotEmpty()) {
            val mostUrgentTask = upcomingTasks.minByOrNull { it.deadline }
            if (mostUrgentTask != null) {
                sendNotification(context, mostUrgentTask)
            }
        }

        return ListenableWorker.Result.success()
    }

    private fun sendNotification(context: Context, task: TodoItem) {
        val channelId = "SERS_CHANNEL_ID"
        val notificationId = task.id.hashCode() 

        val notificationManager = 
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event Reminders"
            val descriptionText = "Notifications for upcoming tasks"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("SERS Reminder: ${task.name}")
            .setContentText("Deadline: ${formatDate(task.deadline)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || 
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(notificationId, builder.build())
        }
    }
}


fun scheduleReminderWorker(context: Context) {
    val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "SERS_Reminder_Work",
        ExistingPeriodicWorkPolicy.KEEP, 
        reminderRequest
    )
}