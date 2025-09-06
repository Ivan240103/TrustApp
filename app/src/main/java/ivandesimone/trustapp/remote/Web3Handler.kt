package ivandesimone.trustapp.remote

import android.content.SharedPreferences
import com.google.gson.Gson
import com.walletconnect.android.CoreClient
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import ivandesimone.trustapp.ui.request.ChainParams
import ivandesimone.trustapp.utils.Debug
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

/**
 * Handler for the remote operations involving MetaMask and the blockchain.
 * @param preferences reference to default shared preferences
 * @param notifier concrete implementation of IRequest contract
 */
class Web3Handler(
	private val preferences: SharedPreferences,
	private val notifier: IRequest
) {

	companion object {
		private const val SEPOLIA_CHAIN_ID = "eip155:11155111"
		private const val GATE_ADDRESS = "0xbb6849DC5D97Bd55DE9A23B58CD5bBF3Bfdda0FA"
		private const val ZONIA_TOKEN_ADDRESS = "0x8821aFDa84d71988cf0b570C535FC502720B33DD"
		private const val RPC_NODE_URL = "https://eth-sepolia.g.alchemy.com/v2/0YwukylbE3vy-oWWK218B"
		private const val EVENT_COMPLETED_NAME = "RequestCompleted"
		private const val EVENT_FAILED_NAME = "RequestFailed"
	}

	/**
	 * Utility data class for eth transaction
	 * @param from source sepolia address
	 * @param to destination sepolia address
	 * @param data arguments for the transaction
	 */
	private data class EthTransaction(val from: String, val to: String, val data: String)

	private val web3j = Web3j.build(HttpService(RPC_NODE_URL))

	/**
	 * Perform the connection to MetaMask's wallet.
	 * @param onUriReady callback to use URI
	 */
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

		pairing?.let {
			SignClient.connect(
				Sign.Params.Connect(namespaces = requiredNamespaces, pairing = it),
				onSuccess = { uri: String ->
					onUriReady(uri)
				},
				onError = { error ->
					Debug.e("connectWallet SignClient connect error: $error")
				}
			)
		} ?: Debug.e("connect wallet error: pairing not initialized")
	}

	/**
	 * Blocking function to send approval to Gate for spending ZONIA tokens.
	 * @param amount spending cap
	 * @param connection wallet connection state
	 * @param onApproveSent callback when the transaction has been sent to MetaMask
	 */
	fun sendApprove(
		amount: BigInteger,
		connection: StateFlow<Pair<String?, String?>>,
		onApproveSent: () -> Unit
	) {
		val (sessionTopic, userAddress) = connection.value

		if (sessionTopic == null || userAddress == null) {
			Debug.e("sendApprove failed because sessionTopic or userAddress is missing")
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
				// TODO: convert in log
				notifier.showRequestToast("Error in approval")
			}
		)
	}

	/**
	 * Blocking function to send the request for data to Gate.
	 * @param query query for the smart contract to execute
	 * @param connection wallet connection state
	 * @param onRequestSent callback when the transaction has been sent to MetaMask
	 */
	fun sendTransaction(
		query: String,
		connection: StateFlow<Pair<String?, String?>>,
		onRequestSent: () -> Unit
	) {
		val (sessionTopic, userAddress) = connection.value

		if (sessionTopic == null || userAddress == null) {
			Debug.e("sendApprove failed because sessionTopic or userAddress is missing")
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
				Debug.d("sendTransaction success, request ID: ${pendingRequest.requestId}")
				onRequestSent()
			},
			onError = { error: Sign.Model.Error ->
				Debug.e("sendTransaction error: ${error.throwable.message}")
				// TODO: convert in log
				notifier.showRequestToast("Error in transaction")
			}
		)
	}

	/**
	 * Blocking function to wait for the transaction to be confirmed by the network.
	 * @param txHash hash of the transaction to wait
	 * @return pair <tx confirmed, tx receipt>
	 */
	suspend fun waitForTransactionReceipt(txHash: String): Pair<Boolean, TransactionReceipt?> =
		withContext(Dispatchers.IO) {
			// ask the chain every 10 seconds
			val pollingInterval = 10000L
			val timeout = preferences.getString("timeout", null)?.toLong() ?: 300L
			val maxAttempts = (timeout * 1000) / pollingInterval

			for (i in 0 until maxAttempts) {
				try {
					val receipt =
						web3j.ethGetTransactionReceipt(txHash).send()?.transactionReceipt?.orElse(null)

					if (receipt != null) {
						return@withContext if (receipt.isStatusOK) {
							Debug.d("Transaction confirmed")
							// TODO: log?
							Pair(true, receipt)
						} else {
							// TODO: remove logs
							Debug.e("Transaction failed. Logs: ${receipt.logs}")
							// TODO: log?
							Pair(false, receipt)
						}
					} else {
						delay(pollingInterval)
					}
				} catch (e: IOException) {
					Debug.e("Network error while waiting for tx receipt: ${e.message}")
					delay(pollingInterval)
				}
			}
			Debug.e("Request timed out for tx $txHash")
			// TODO: log?
			return@withContext Pair(false, null)
		}

	/**
	 * Retrieve the results of the operation from the receipt.
	 * @param receipt receipt of the confirmed transaction
	 * @return pair <is successful, result>
	 */
	fun extractResultFromLogs(receipt: TransactionReceipt): Pair<Boolean, String> {
		val parameters = listOf(
			object : TypeReference<Bytes32>(true) {},
			object : TypeReference<Utf8String>(false) {}
		)

		val requestCompletedEvent = Event(EVENT_COMPLETED_NAME, parameters)
		val requestCompletedSignature = EventEncoder.encode(requestCompletedEvent)
		val requestFailedEvent = Event(EVENT_FAILED_NAME, parameters)
		val requestFailedSignature = EventEncoder.encode(requestFailedEvent)

		receipt.logs.forEach { log ->
			val eventSignature = log.topics.getOrNull(0)

			when (eventSignature) {
				requestCompletedSignature -> {
					val eventParams = Contract.staticExtractEventParameters(requestCompletedEvent, log)
					val resultString = eventParams.nonIndexedValues[0] as Utf8String
					Debug.d("Request COMPLETED: ${resultString.value}")
					// TODO: log?
					return Pair(true, resultString.value)
				}

				requestFailedSignature -> {
					val eventParams = Contract.staticExtractEventParameters(requestFailedEvent, log)
					val resultString = eventParams.nonIndexedValues[0] as Utf8String
					Debug.e("Request FAILED: ${resultString.value}")
					// TODO: log?
					return Pair(false, resultString.value)
				}
			}
		}

		return Pair(false, "No event $EVENT_COMPLETED_NAME or $EVENT_FAILED_NAME was found in logs")
	}

	/**
	 * Generate transaction data for submitRequest function of Gate.
	 * @param queryValue query for the smart contract to execute
	 * @return transaction data encoded
	 */
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