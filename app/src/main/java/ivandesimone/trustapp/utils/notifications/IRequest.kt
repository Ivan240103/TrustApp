package ivandesimone.trustapp.utils.notifications

/* Define a Contract (Interface): You create an interface that describes the action you want to
 * perform (e.g., "show a notification").
 * The Repository will depend on this abstract interface, not a concrete Android class.
 */
interface IRequest {
	fun showRequestNotification(title: String, message: String)

	fun showRequestToast(text: String)

	fun showRequestSnack()
}