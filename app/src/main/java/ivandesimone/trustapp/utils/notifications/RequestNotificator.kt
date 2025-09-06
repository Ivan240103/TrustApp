package ivandesimone.trustapp.utils.notifications

/**
 * Concrete implementation for notification contract to send information related to data requests.
 * @param notificator implementation of notify operations
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