package ivandesimone.trustapp.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ivandesimone.trustapp.MainActivity
import ivandesimone.trustapp.R

class Notificator(private val context: Context) {
	companion object {
		private const val ALERT_CHANNEL_ID = "alert_channel"
		private const val ALERT_CHANNEL_NAME = "Alert"
		private const val ALERT_CHANNEL_DESCRIPTION = "Notifications for values over the threshold"
		private var alertId = 0
	}

	private val notificationManager = NotificationManagerCompat.from(context)

	init {
		val alertChannel = NotificationChannel(
			ALERT_CHANNEL_ID,
			ALERT_CHANNEL_NAME,
			NotificationManager.IMPORTANCE_HIGH
		)
		alertChannel.description = ALERT_CHANNEL_DESCRIPTION
		notificationManager.createNotificationChannel(alertChannel)
	}

	fun fireNotification(title: String, text: String) {
		if (ContextCompat.checkSelfPermission(
				context,
				Manifest.permission.POST_NOTIFICATIONS
			) == PackageManager.PERMISSION_GRANTED
		) {
			val intent = Intent(context, MainActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			val pendingIntent = PendingIntent.getActivity(
				context, 0, intent, PendingIntent.FLAG_IMMUTABLE
			)

			val builder = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
				.setSmallIcon(R.drawable.metamask)
				.setContentTitle(title)
				.setContentText(text)
				.setContentIntent(pendingIntent)
			notificationManager.notify(alertId, builder.build())
			alertId++
		}
	}
}