package ivandesimone.trustapp.remote

import android.content.SharedPreferences
import com.google.gson.Gson
import com.walletconnect.android.CoreClient
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import ivandesimone.trustapp.Debug
import ivandesimone.trustapp.ui.request.ChainParams
import ivandesimone.trustapp.utils.notifications.IRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.Contract
import java.io.IOException
import java.math.BigInteger

class Web3Handler(
	private val preferences: SharedPreferences,
	private val notifier: IRequest
) {

	companion object {
		private const val SEPOLIA_CHAIN_ID = "eip155:11155111"
		private const val GATE_ADDRESS = "0xbb6849DC5D97Bd55DE9A23B58CD5bBF3Bfdda0FA"
		private const val ZONIA_TOKEN_ADDRESS = "0x8821aFDa84d71988cf0b570C535FC502720B33DD"
		private const val RPC_NODE_URL = "https://eth-sepolia.g.alchemy.com/v2/0YwukylbE3vy-oWWK218B"
		private const val EVENT_COMPLETED = "RequestCompleted"
		private const val EVENT_FAILED = "RequestFailed"
	}

	private data class EthTransaction(val from: String, val to: String, val data: String)

	private val web3j = Web3j.build(HttpService(RPC_NODE_URL))

	fun connectWallet(onUriReady: (String) -> Unit) {
		// TrustApp needs for connection proposal to MetaMask
		val requiredNamespaces = mapOf(
			"eip155" to Sign.Model.Namespace.Proposal(
				chains = listOf(SEPOLIA_CHAIN_ID),
				methods = listOf(
					"eth_sendTransaction",
					"personal_sign",
					"eth_sign"
				),
				events = listOf("chainChanged", "accountsChanged")
			)
		)

		val pairing = CoreClient.Pairing.create { error ->
			Debug.e("connectWallet pairing error: $error")
		}
		Debug.d("connectWallet pairing: $pairing")

		pairing?.let {
			SignClient.connect(
				Sign.Params.Connect(namespaces = requiredNamespaces, pairing = it),
				onSuccess = { s: String ->
					Debug.d("SignClient connect success. Pairing return: $s")
					onUriReady(s)
				},
				onError = { error ->
					Debug.e("SignClient connect error: $error")
				}
			)
		} ?: Debug.e("Pairing not initialized, it's null!")
	}

	// use inside a coroutine
	fun sendApprove(amount: BigInteger, uiState: StateFlow<Pair<String?, String?>>, onApproveSent: () -> Unit) {
		val (sessionTopic, userAddress) = uiState.value

		Debug.d("sendApprove start, sessionTopic: $sessionTopic, userAddress: $userAddress")

		if (sessionTopic == null || userAddress == null) {
			// not connected
			Debug.e("sendApprove failed because not connected")
			return
		}

		val function = Function(
			"approve",
			listOf(Address(GATE_ADDRESS), Uint256(amount)),
			emptyList()
		)
		val encodedFunction = FunctionEncoder.encode(function)

		val transaction = EthTransaction(
			from = userAddress,
			to = ZONIA_TOKEN_ADDRESS,
			data = encodedFunction
		)
		val transactionJson = Gson().toJson(transaction)

		val requestParams = Sign.Params.Request(
			sessionTopic = sessionTopic,
			method = "eth_sendTransaction",
			params = "[$transactionJson]",
			chainId = SEPOLIA_CHAIN_ID
		)

		SignClient.request(
			request = requestParams,
			onSuccess = { pendingRequest: Sign.Model.SentRequest ->
				Debug.d("sendApprove success, request ID: ${pendingRequest.requestId}")
				onApproveSent()
			},
			onError = { error: Sign.Model.Error ->
				Debug.e("sendApprove error: $error")
				notifier.showRequestToast("Error in approval")
			}
		)
	}

	// use inside a coroutine
	fun sendTransaction(query: String, uiState: StateFlow<Pair<String?, String?>>, onRequestSent: () -> Unit) {
		val (sessionTopic, userAddress) = uiState.value

		Debug.d("sendTransaction start, sessionTopic: $sessionTopic, userAddress: $userAddress")

		if (sessionTopic == null || userAddress == null) {
			// not connected
			Debug.e("sendTransaction failed because not connected")
			return
		}

		val transactionData = createTransactionData(query)
		val transaction = EthTransaction(
			from = userAddress,
			to = GATE_ADDRESS,
			data = transactionData
		)
		val transactionJson = Gson().toJson(transaction)

		val requestParams = Sign.Params.Request(
			sessionTopic = sessionTopic,
			method = "eth_sendTransaction",
			params = "[$transactionJson]",
			chainId = SEPOLIA_CHAIN_ID
		)

		SignClient.request(
			request = requestParams,
			onSuccess = { pendingRequest: Sign.Model.SentRequest ->
				// The result (tx hash) will come via a webhook or you can check a block explorer.
				// The wallet sends the result to the WalletConnect server.
				Debug.d("sendTransaction success, request ID: ${pendingRequest.requestId}")
				onRequestSent()
			},
			onError = { error: Sign.Model.Error ->
				Debug.d("sendTransaction error: ${error.throwable.message}")
				notifier.showRequestToast("Error in transaction")
			}
		)
	}

	suspend fun waitForTransactionReceipt(txHash: String): Pair<Boolean, TransactionReceipt?> =
		withContext(Dispatchers.IO){
			// ask the chain every 10 seconds
			val pollingInterval = 10000L
			val timeout = preferences.getString("timeout", null)?.toLong() ?: 300L
			val maxAttempts = (timeout * 1000) / pollingInterval

			for (i in 0 until maxAttempts) {
				try {
					val receipt =
						web3j.ethGetTransactionReceipt(txHash).send()?.transactionReceipt?.orElse(null)

					if (receipt != null) {
						Debug.d("Attempt ${i + 1}: receipt found for tx $txHash on block ${receipt.blockNumber}")
						return@withContext if (receipt.isStatusOK) {
							Debug.d("Transaction successful")
							Pair(true, receipt)
						} else {
							Debug.e("Transaction failed. Status ${receipt.revertReason}")
							Pair(false, receipt)
						}
					} else {
						Debug.d("Still waiting for receipt, attempt ${i + 1}")
						delay(pollingInterval)
					}
				} catch (e: IOException) {
					Debug.e("Network error while waiting for tx receipt: ${e.message}")
					delay(pollingInterval)
				}
			}
			Debug.e("Data request timed out for tx $txHash")
			return@withContext Pair(false, null)
		}

	fun extractResultFromLogs(receipt: TransactionReceipt): Pair<Boolean, String> {
		Debug.d("Scanning receipt for result string ${receipt.transactionHash}")

		val requestCompletedEvent = Event(EVENT_COMPLETED,
			listOf(
				object : TypeReference<Bytes32>(true) {},
				object : TypeReference<Utf8String>(false) {}
			)
		)
		val requestCompletedSignature = EventEncoder.encode(requestCompletedEvent)

		val requestFailedEvent = Event(EVENT_FAILED,
			listOf(
				object : TypeReference<Bytes32>(true) {},
				object : TypeReference<Utf8String>(false) {}
			)
		)
		val requestFailedSignature = EventEncoder.encode(requestFailedEvent)

		receipt.logs.forEach { log ->
			val eventSignature = log.topics.getOrNull(0)

			when (eventSignature) {
				requestCompletedSignature -> {
					val eventValues = Contract.staticExtractEventParameters(requestCompletedEvent, log)
					val requestId = eventValues.indexedValues[0] as Bytes32
					val resultString = eventValues.nonIndexedValues[0] as Utf8String
					Debug.d("Request COMPLETED: ${resultString.value}")
					return Pair(true, resultString.value)
				}
				requestFailedSignature -> {
					val eventValues = Contract.staticExtractEventParameters(requestFailedEvent, log)
					val requestId = eventValues.indexedValues[0] as Bytes32
					val resultString = eventValues.nonIndexedValues[0] as Utf8String
					Debug.e("Request FAILED: ${resultString.value}")
					return Pair(false, resultString.value)
				}
			}
		}

		return Pair(false, "No event $EVENT_COMPLETED or $EVENT_FAILED was found in logs")
	}

	private fun createTransactionData(queryValue: String): String {
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

}