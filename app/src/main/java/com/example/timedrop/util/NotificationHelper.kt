package com.example.timedrop.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import com.example.timedrop.services.CalendarNotificationReceiver
import com.example.timedrop.data.local.CalendarEvent
import java.text.SimpleDateFormat
import java.util.*

object NotificationHelper {
    private const val CHANNEL_ID = "timedrop_notifications"
    private const val CHANNEL_NAME = "TimeDrop Alerts"
    private const val CHANNEL_DESC = "Notifications for Pomodoro and Calendar Events"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun scheduleEventAlarms(context: Context, event: CalendarEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Parse "yyyy-MM-dd" and "HH:mm AM/PM"
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
        val eventDateTimeString = "${event.date} ${event.time}"
        val date = sdf.parse(eventDateTimeString) ?: return
        
        val calendar = Calendar.getInstance().apply {
            time = date
        }

        // Alarm at the exact time
        val exactMessage = "Es la hora de ${event.title}"
        scheduleExactAlarm(context, alarmManager, event, calendar.timeInMillis, 0, exactMessage)
        
        // Alarm 5 minutes before
        val fiveMinutesBefore = calendar.timeInMillis - (5 * 60 * 1000)
        if (fiveMinutesBefore > System.currentTimeMillis()) {
            val prepMessage = "Faltan 5 minutos para ${event.title}"
            scheduleInexactAlarm(context, alarmManager, event, fiveMinutesBefore, 1, prepMessage)
        }
    }

    private fun scheduleExactAlarm(context: Context, alarmManager: AlarmManager, event: CalendarEvent, timeMillis: Long, requestIdSuffix: Int, message: String) {
        val intent = Intent(context, CalendarNotificationReceiver::class.java).apply {
            putExtra("title", "Calendario")
            putExtra("message", message)
        }
        
        val requestId = event.id * 10 + requestIdSuffix
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // For exact time, use setAlarmClock (most reliable)
        // We need a showIntent for the alarm clock info
        val showIntent = Intent(context, com.example.timedrop.MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestId + 100, // Offset for activity intent
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
        }
    }

    private fun scheduleInexactAlarm(context: Context, alarmManager: AlarmManager, event: CalendarEvent, timeMillis: Long, requestIdSuffix: Int, message: String) {
        val intent = Intent(context, CalendarNotificationReceiver::class.java).apply {
            putExtra("title", "Calendario")
            putExtra("message", message)
        }
        
        val requestId = event.id * 10 + requestIdSuffix
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
        }
    }

    fun cancelEventAlarms(context: Context, event: CalendarEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(0, 1).forEach { suffix ->
            val intent = Intent(context, CalendarNotificationReceiver::class.java)
            val requestId = event.id * 10 + suffix
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }
}
