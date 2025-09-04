package ivandesimone.trustapp.utils.notifications

/*
 * Implement the Contract in the UI Layer: You create a class that implements this interface.
 * This implementation class will live in your app's main module and will use your
 * NotificationHelper to do the actual work.
 */
class AlertNotificator(private val notificator: Notificator) : IAlert {

	override fun showAlertNotification(title: String, text: String) {
		notificator.fireAlertNotification(title, text)
	}

}