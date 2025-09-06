package ivandesimone.trustapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import ivandesimone.trustapp.db.MeasurementRepository
import ivandesimone.trustapp.ui.request.RequestFragment
import ivandesimone.trustapp.utils.Debug
import ivandesimone.trustapp.utils.notifications.IRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to provide data related to MetaMask and the blockchain.
 * @param repo repository to interact with data sources
 * @param notifier concrete implementation of IRequest contract
 */
class Web3ViewModel(
	private val repo: MeasurementRepository,
	private val notifier: IRequest
) : ViewModel() {
	// using StateFlow because it's more reliable with coroutines
	private val _connection = MutableStateFlow<Pair<String?, String?>>(null to null)
	val connection: StateFlow<Pair<String?, String?>> = _connection

	private lateinit var gateQuery: String
	private lateinit var logger: RequestFragment.Logger

	// flag to trace MetaMask responses
	private var isWaitingForApproval = false

	init {
		// check for existing MetaMask session
		val existingSession = SignClient.getListOfSettledSessions().firstOrNull()
		existingSession?.let {
			val sessionTopic = it.topic
			val fullAccount = it.namespaces["eip155"]?.accounts?.firstOrNull()
			val userAddress = fullAccount?.split(":")?.get(2)
			viewModelScope.launch { _connection.value = sessionTopic to userAddress }
			Debug.d("Found existing session")
		} ?: Debug.d("No existing session found")

		// set delegate
		val delegate = object : SignClient.DappDelegate {
			override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
				val sessionTopic = approvedSession.topic
				var userAddress: String? = null

				val fullAccount = approvedSession.namespaces["eip155"]?.accounts?.firstOrNull()
				userAddress = fullAccount?.split(":")?.get(2)
				viewModelScope.launch { _connection.value = sessionTopic to userAddress }
			}

			override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
				Debug.d("onSessionDelete called")
				viewModelScope.launch { _connection.value = null to null }
			}

			override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
				viewModelScope.launch {
					when (val result = response.result) {
						is Sign.Model.JsonRpcResponse.JsonRpcResult -> {
							val txHash = result.result
							Debug.d("Transaction sent on Sepolia. Hash: $txHash")

							if (isWaitingForApproval) {
								// waiting for approval to spend
								isWaitingForApproval = false
								logger.log("Approve to spend sent...")

								val (isApproved, receipt) = repo.waitForTransactionReceipt(txHash)

								if (isApproved) {
									Debug.d("approve confirmed, now sending request transaction...")
									logger.log("Approve to spend confirmed...")
									repo.sendTransaction(gateQuery, connection)
								} else {
									Debug.e("approve failed. Aborting...")
									logger.log("Approve to spend not confirmed.")
									if (receipt == null) {
										notifier.showRequestNotification(
											"Approval timed out!",
											"The approve request has exceeded the time bound."
										)
									} else {
										notifier.showRequestNotification(
											"Transaction not confirmed!",
											"The approve transaction has not been confirmed, request aborted."
										)
									}
								}
							} else {
								// waiting for return data from zonia
								logger.log("Request sent...")
								val (isApproved, receipt) = repo.waitForTransactionReceipt(txHash)

								if (isApproved) {
									Debug.d("submitRequest confirmed, now getting result data...")
									logger.log("Request confirmed...")
									// if isApproved is true, receipt cannot be null
									val (isCompleted, resultString) = repo.extractResultFromLogs(receipt!!)
									if (isCompleted) {
										logger.log("Request completed!")
										notifier.showRequestNotification(
											"Request completed!",
											"Your data has been successfully retrieved: $resultString"
										)
										// TODO: insert data in db
										// repo.insertMeasurements()
									} else {
										logger.log("Request failed!")
										notifier.showRequestNotification(
											"Request failed!",
											resultString
										)
									}
								} else {
									Debug.e("submitRequest failed. Aborting...")
									logger.log("Request not confirmed.")
									if (receipt == null) {
										notifier.showRequestNotification(
											"Request timed out!",
											"The data request has exceeded the time bound."
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

						is Sign.Model.JsonRpcResponse.JsonRpcError -> {
							Debug.e("Transaction not sent! ${result.code}: ${result.message}")
							logger.log("Transaction not sent.")
							isWaitingForApproval = false
						}
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

	/**
	 * Perform the connection to MetaMask's wallet.
	 * @param onUriReady callback to use URI
	 */
	fun connectWallet(onUriReady: (String) -> Unit) {
		repo.connectWallet(onUriReady)
	}

	/**
	 * Request measurements from ZONIA to be inserted in database.
	 * @param query query for the smart contract to execute
	 * @param logger logger to display info on UI
	 */
	fun requestZoniaMeasures(query: String, logger: RequestFragment.Logger) {
		this.gateQuery = query
		this.logger = logger
		isWaitingForApproval = true
		viewModelScope.launch {
			repo.approveZoniaTokens(connection)
		}
	}

}

/**
 * Factory to create Web3ViewModel
 * @param repo repository to interact with data sources
 * @param notifier concrete implementation of IRequest contract
 */
@Suppress("UNCHECKED_CAST")
class Web3ViewModelFactory(
	private val repo: MeasurementRepository,
	private val notifier: IRequest
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return Web3ViewModel(repo, notifier) as T
	}
}