package ivandesimone.trustapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import ivandesimone.trustapp.Debug
import ivandesimone.trustapp.db.MeasureRepository
import ivandesimone.trustapp.remote.Web3Handler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger

class EthViewModel(private val repo: MeasureRepository) : ViewModel() {

	private val handler = Web3Handler()
	// using StateFlow cause it's more reliable with coroutines
	private val _uiState = MutableStateFlow<Pair<String?, String?>>(null to null)
	val uiState: StateFlow<Pair<String?, String?>> = _uiState

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
						Debug.d("Transaction approved! Hash: ${result.result}")
					}
					is Sign.Model.JsonRpcResponse.JsonRpcError ->{
						Debug.d("Transaction failed! Code ${result.code}: ${result.message}")
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
		handler.connectWallet(onUriReady)
	}

	fun approveZoniaTokens() {
		viewModelScope.launch {
			handler.sendApprove(BigInteger.valueOf(1000000000000000), uiState)
		}
	}

	fun sendTransaction(query: String) {
		viewModelScope.launch {
			handler.sendTransaction(query, uiState)
		}
	}
}

@Suppress("UNCHECKED_CAST")
class EthViewModelFactory(private val repo: MeasureRepository) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return EthViewModel(repo) as T
	}
}