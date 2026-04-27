package com.example.timedrop.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.timedrop.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeDropWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        
        // Update Time
        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        views.setTextViewText(R.id.widget_clock, timeFormat.format(Date()))
        
        // In a more advanced version, we would fetch the next event from the DB here
        // using a CoroutineScope and the AppDatabase.
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
