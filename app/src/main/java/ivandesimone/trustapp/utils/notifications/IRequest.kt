package ivandesimone.trustapp.utils.notifications

/**
 * Contract to describe on which operations should the repository rely for notifications.
 */
interface IRequest {

	/**
	 * Send a push notification to the user.
	 * @param title header of notification
	 * @param message content of notification
	 */
	fun showRequestNotification(title: String, message: String)

	/**
	 * Display a Toast on screen.
	 * @param text Toast content
	 */
	fun showRequestToast(text: String)

	/**
	 * Display a SnackBar on screen to open MetaMask.
	 */
	fun showRequestSnack()
}