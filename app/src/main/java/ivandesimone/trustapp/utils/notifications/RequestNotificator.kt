package ivandesimone.trustapp.utils.notifications

/*
 * Implement the Contract in the UI Layer: You create a class that implements this interface.
 * This implementation class will live in your app's main module and will use your
 * NotificationHelper to do the actual work.
 */
class RequestNotificator(private val notificator: Notificator) : IRequest {

	override fun showRequestNotification(title: String, message: String) {
		notificator.fireRequestNotification(title, message)
	}

	override fun showRequestToast(text: String) {
		notificator.fireRequestToast(text)
	}

	override fun showRequestSnack() {
		notificator.fireRequestSnack()
	}

}