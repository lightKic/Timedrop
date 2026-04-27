package com.example.timedrop.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timedrop.util.NotificationHelper

class CalendarNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Event Reminder"
        val message = intent.getStringExtra("message") ?: "You have an upcoming event."
        
        NotificationHelper.showNotification(context, title, message)
    }
}
