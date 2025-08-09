package ivandesimone.trustapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.walletconnect.android.CoreClient
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import ivandesimone.trustapp.ui.request.ChainParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class EthViewModel : ViewModel() {

	private val _uiState = MutableStateFlow<Pair<String?, String?>>(null to null)
	val uiState: StateFlow<Pair<String?, String?>> = _uiState

	init {
		val delegate = object : SignClient.DappDelegate {
			override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
				// get topic and user address
				val sessionTopic = approvedSession.topic
				val userAddress =
					approvedSession.namespaces["eip155"]?.accounts?.firstOrNull()?.split(":")?.get(2)
				viewModelScope.launch {
					_uiState.value = sessionTopic to userAddress
				}
			}

			override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
				viewModelScope.launch { _uiState.value = null to null }
			}

			override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {}
			override fun onError(error: Sign.Model.Error) {}
			override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) {}
			override fun onSessionExtend(session: Sign.Model.Session) {}
			override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {}
			override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) {}

			// not in gemini
			override fun onProposalExpired(proposal: Sign.Model.ExpiredProposal) {}
			override fun onRequestExpired(request: Sign.Model.ExpiredRequest) {}
			override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {}
		}
		SignClient.setDappDelegate(delegate)
	}

	fun connectToWallet(onUriReady: (String) -> Unit) {
		// Define what your dApp needs: Sepolia chain + one method
		val requiredNamespaces = mapOf(
			"eip155" to Sign.Model.Namespace.Proposal(
				chains = listOf("eip155:11155111"), // sepolia testnet
				methods = listOf("eth_sendTransaction"),
				events = listOf("chainChanged", "accountsChanged")
			)
		)

		val pairing = CoreClient.Pairing.create { error ->
			Log.e("WalletConnect", "Pairing error: $error")
		}

		if (pairing != null) {
			val connectParams = Sign.Params.Connect(namespaces = requiredNamespaces, pairing = pairing)

			SignClient.connect(connectParams,
				onSuccess = { _ ->
					onUriReady(pairing.uri)
				},
				onError = { error ->
					Log.e("WalletConnect", "SignClient connect error: $error")
				}
			)
		} else {
			Log.e("WalletConnect", "Pairing not initialized!")
		}

	}

	private fun createTransactionData(queryValue: String): String {
		// TODO: step 4 gemini -> web3j missing ?
		val chainParams = ChainParams(25, 25, 25, 25)
		val chainParamsStr = Gson().toJson(chainParams)
		val ko = BigInteger.valueOf(1)
		val ki = BigInteger.valueOf(1)
		val feeValue = BigInteger.valueOf(1000000000000000) // 0.001 ETH in wei

		val inputRequest = DynamicStruct(
			Utf8String(queryValue),
			Utf8String(chainParamsStr),
			Uint256(ko),
			Uint256(ki),
			Uint256(feeValue)
		)

		val function = Function(
			"submitRequest",
			listOf(inputRequest),
			emptyList()
		)

		return FunctionEncoder.encode(function)
	}

	fun sendTransaction(query: String) {
		viewModelScope.launch {
			val (sessionTopic, userAddress) = uiState.value
			if (sessionTopic == null || userAddress == null) {
				// not connected
				return@launch
			}

			val contractAddress = "0xfb663f4fc2624366B527c0d97271405D14503121"
			val transactionData = createTransactionData(query)
			val chainId = "eip155:11155111"

			val transaction = """
				{
					"from": "$userAddress",
					"to": "$contractAddress",
					"data": "$transactionData"
				}
			""".trimIndent()

			val requestParams = Sign.Params.Request(
				sessionTopic = sessionTopic,
				method = "eth_sendTransaction",
				params = "[$transaction]",
				chainId = chainId
			)

			SignClient.request(requestParams,
				onSuccess = { pendingRequest: Sign.Model.SentRequest ->
					// Request sent successfully. MetaMask will now prompt the user.
					// The result (tx hash) will come via a webhook or you can check a block explorer.
					// WalletConnect v2 for dApps doesn't directly return the result here.
					// The wallet sends the result to the WalletConnect server.
					Log.d("WalletConnect", "Request sent, ID: ${pendingRequest.requestId}")
				},
				onError = { error: Sign.Model.Error ->
					Log.d("WalletConnect", "Request error: ${error.throwable.message}")
				}
			)
		}
	}
}