package ivandesimone.trustapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import ivandesimone.trustapp.Debug
import ivandesimone.trustapp.db.MeasureRepository
import ivandesimone.trustapp.ui.request.RequestFragment
import ivandesimone.trustapp.utils.notifications.IRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EthViewModel(
	private val repo: MeasureRepository,
	private val notifier: IRequest
) : ViewModel() {
	// using StateFlow cause it's more reliable with coroutines
	private val _uiState = MutableStateFlow<Pair<String?, String?>>(null to null)
	val uiState: StateFlow<Pair<String?, String?>> = _uiState

	private var zoniaQuery = ""
	private lateinit var logger: RequestFragment.Logger
	private var isWaitingForApproval = false

	init {
		// check for existing session
		val existingSession = SignClient.getListOfSettledSessions().firstOrNull()
		existingSession?.let {
			val sessionTopic = it.topic
			val fullAccount = it.namespaces["eip155"]?.accounts?.firstOrNull()
			val userAddress = fullAccount?.split(":")?.get(2)

			Debug.d("Found existing session: $sessionTopic")

			userAddress?.let { it2 ->
				Debug.d("Restoring session for address: $it2")
				_uiState.value = sessionTopic to it2
			}
		} ?: Debug.d("No existing session found")

		// set delegate
		val delegate = object : SignClient.DappDelegate {
			override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
				Debug.d("onSessionApproved called")
				val sessionTopic = approvedSession.topic
				var userAddress: String? = null

				try {
					val fullAccount = approvedSession.namespaces["eip155"]?.accounts?.firstOrNull()
					fullAccount?.let {
						userAddress = it.split(":")[2]
						Debug.d("Successfully parsed address: $userAddress")
					} ?: Debug.e("Could not find eip155 account in session approval.")
				} catch (e: Exception) {
					Debug.e("Error parsing address from session approval $e")
				}

				userAddress?.let {
					viewModelScope.launch {
						_uiState.value = sessionTopic to it
					}
				} ?: Debug.e("Failed to get userAddress, UI state will not be updated")
			}

			override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
				Debug.d("onSessionDelete called")
				viewModelScope.launch { _uiState.value = null to null }
			}

			override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
				when (val result = response.result) {
					is Sign.Model.JsonRpcResponse.JsonRpcResult -> {
						val txHash = result.result
						Debug.d("Transaction sent! Hash: $txHash")

						// check if the hash if for approve
						if (isWaitingForApproval) {
							isWaitingForApproval = false

							viewModelScope.launch {
								Debug.d("Waiting for approve tx to be confirmed")
								val (isApproved, receipt) = repo.waitForTransactionReceipt(txHash)

								if (isApproved) {
									Debug.d("Approval confirmed, now sending the real transaction...")
									logger.log("Approve confirmed")
									repo.sendTransaction(zoniaQuery, uiState)
								} else {
									Debug.e("Approval failed. Aborting...")
									if (receipt == null) {
										notifier.showRequestNotification(
											"Approval timed out!",
											"The approve request has exceeded the time bound"
										)
									} else {
										notifier.showRequestNotification(
											"Transaction not confirmed!",
											"The approve transaction has not been confirmed, request aborted."
										)
									}
								}
							}
						} else {
							// waiting for return data from zonia
							viewModelScope.launch {
								Debug.d("Waiting for submitRequest tx to be confirmed")
								val (isApproved, receipt) = repo.waitForTransactionReceipt(txHash)

								if (isApproved) {
									Debug.d("submitRequest tx confirmed, now getting result data...")
									// if isApproved is true, receipt cannot be null
									val (isCompleted, resultString) = repo.extractResultFromLogs(receipt!!)
									if (isCompleted) {
										notifier.showRequestNotification(
											"Request completed!",
											"Your data has been successfully retrieved: $resultString"
										)
									} else {
										notifier.showRequestNotification(
											"Request failed!",
											resultString
										)
									}
								} else {
									Debug.e("submitRequest failed. Aborting...")
									if (receipt == null) {
										notifier.showRequestNotification(
											"Request timed out!",
											"The data request has exceeded the time bound"
										)
									} else {
										notifier.showRequestNotification(
											"Transaction not confirmed!",
											"The submitRequest transaction has not been confirmed, request aborted."
										)
									}
								}
							}
						}
					}
					is Sign.Model.JsonRpcResponse.JsonRpcError ->{
						Debug.d("Transaction failed! Code ${result.code}: ${result.message}")
						isWaitingForApproval = false
					}
				}
			}

			override fun onConnectionStateChange(state: Sign.Model.ConnectionState) =
				Debug.d("onConnectionStateChange called")

			override fun onError(error: Sign.Model.Error) =
				Debug.d("onError called: $error")

			override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) =
				Debug.d("onSessionEvent called")

			override fun onSessionExtend(session: Sign.Model.Session) =
				Debug.d("onSessionExtend called")

			override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) =
				Debug.d("onSessionRejected called")

			override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) =
				Debug.d("onSessionUpdate called")

			override fun onProposalExpired(proposal: Sign.Model.ExpiredProposal) =
				Debug.d("onProposalExpired called")

			override fun onRequestExpired(request: Sign.Model.ExpiredRequest) =
				Debug.d("onRequestExpired called")
		}

		SignClient.setDappDelegate(delegate)
	}

	fun connectWallet(onUriReady: (String) -> Unit) {
		repo.connectWallet { uri -> onUriReady(uri) }
	}

	fun requestZoniaMeasures(query: String, logger: RequestFragment.Logger) {
		zoniaQuery = query
		this.logger = logger
		isWaitingForApproval = true
		viewModelScope.launch {
			repo.approveZoniaTokens(uiState)
		}
	}

}

@Suppress("UNCHECKED_CAST")
class EthViewModelFactory(
	private val repo: MeasureRepository,
	private val notifier: IRequest
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return EthViewModel(repo, notifier) as T
	}
}