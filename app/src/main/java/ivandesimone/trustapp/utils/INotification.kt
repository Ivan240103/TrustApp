package ivandesimone.trustapp.utils

/* Define a Contract (Interface): You create an interface that describes the action you want to
 * perform (e.g., "show a notification").
 * The Repository will depend on this abstract interface, not a concrete Android class.
 */
interface INotification {
	fun showAlertNotification(title: String, text: String)
}