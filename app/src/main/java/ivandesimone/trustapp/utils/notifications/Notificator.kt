package ivandesimone.trustapp.utils.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import ivandesimone.trustapp.MainActivity
import ivandesimone.trustapp.R

class Notificator(private val context: Context, private val intentSnack: Snackbar) {
	companion object {
		private const val REQUEST_CHANNEL_ID = "request_channel"
		private const val REQUEST_CHANNEL_NAME = "Request"
		private const val REQUEST_CHANNEL_DESCRIPTION = "Notifications related to data requests"
		private var requestNotificationId = 0
	}

	private val notificationManager = NotificationManagerCompat.from(context)

	init {
		val requestChannel = NotificationChannel(
			REQUEST_CHANNEL_ID,
			REQUEST_CHANNEL_NAME,
			NotificationManager.IMPORTANCE_HIGH
		)
		requestChannel.description = REQUEST_CHANNEL_DESCRIPTION
		notificationManager.createNotificationChannel(requestChannel)
	}

	fun fireRequestNotification(title: String, message: String) {
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

			val builder = NotificationCompat.Builder(context, REQUEST_CHANNEL_ID)
				.setSmallIcon(R.drawable.metamask)
				.setContentTitle(title)
				.setContentText(message)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentIntent(pendingIntent)
			notificationManager.notify(requestNotificationId, builder.build())
			requestNotificationId++
		}
	}

	fun fireRequestToast(text: String) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
	}

	fun fireRequestSnack() {
		intentSnack.show()
	}
}