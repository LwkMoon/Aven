package com.aven.app.androidintegration.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aven.app.MainActivity
import com.aven.app.R

/**
 * NotificationService:
 *
 * Follows calm philosophy:
 * "Extremely restrained, always fully optional and toggleable off in Settings. Never guilt-based."
 * Dispatches gentle milestone notifications celebrating mindful moments.
 */
interface NotificationService {
    fun sendQuietMilestone(title: String, message: String)
}

class AvenNotificationService(private val context: Context) : NotificationService {

    companion object {
        const val CHANNEL_ID = "aven_calm_milestones"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_milestones_name)
            val descriptionText = context.getString(R.string.notification_channel_milestones_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun sendQuietMilestone(title: String, message: String) {
        Log.i("AvenNotifications", "Calm notification dispatch: [$title] $message")

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val manager = NotificationManagerCompat.from(context)
            if (manager.areNotificationsEnabled()) {
                manager.notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            Log.w("AvenNotifications", "Notification permission not yet granted by user", e)
        } catch (e: Exception) {
            Log.e("AvenNotifications", "Error dispatching milestone notification", e)
        }
    }
}

