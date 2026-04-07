package com.memoloop.app.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.memoloop.app.MainActivity
import com.memoloop.app.R
import com.memoloop.app.data.repository.DifficultyManager
import com.memoloop.app.data.repository.WordRepository

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Pick a random word for daily word notification
        val wordRepo = WordRepository(context)
        val difficultyManager = DifficultyManager(context)
        val allWords = wordRepo.getAllWords(difficultyManager.current)
        val dailyWord = allWords.randomOrNull()

        val title: String
        val text: String
        if (dailyWord != null) {
            title = "\uD83D\uDCDA ${dailyWord.word}"
            text = dailyWord.examples.firstOrNull() ?: dailyWord.definition
        } else {
            title = context.getString(R.string.reminder_title)
            text = context.getString(R.string.reminder_text)
        }

        val notification = NotificationCompat.Builder(context, ReminderManager.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted
        }

        // Schedule next day's alarm (exact alarms don't repeat)
        val reminderManager = ReminderManager(context)
        reminderManager.scheduleReminder()
    }
}
