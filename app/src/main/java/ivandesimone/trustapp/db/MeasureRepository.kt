package ivandesimone.trustapp.db

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import ivandesimone.trustapp.remote.MockHandler
import ivandesimone.trustapp.remote.Web3Handler
import ivandesimone.trustapp.utils.notifications.IRequest
import kotlinx.coroutines.flow.StateFlow
import org.web3j.protocol.core.methods.response.TransactionReceipt
import java.math.BigInteger

/*
 * Inject the Implementation: You provide the concrete implementation to your Repository through
 * its constructor (this is called Dependency Injection).
 */
class MeasureRepository(
	private val dao: MeasureDao,
	private val preferences: SharedPreferences,
	private val notifier: IRequest
) {

	private val mockHandler = MockHandler()
	private val web3Handler = Web3Handler(preferences, notifier)

	fun connectWallet(onUriReady: (String) -> Unit) {
		web3Handler.connectWallet { uri -> onUriReady(uri) }
	}

	suspend fun approveZoniaTokens(uiState: StateFlow<Pair<String?, String?>>) {
		web3Handler.sendApprove(BigInteger.valueOf(1000000000000000), uiState) {
			notifier.showRequestSnack()
		}
	}

	suspend fun sendTransaction(query: String, uiState: StateFlow<Pair<String?, String?>>) {
		web3Handler.sendTransaction(query, uiState) {
			notifier.showRequestSnack()
		}
	}

	suspend fun waitForTransactionReceipt(txHash: String): Pair<Boolean, TransactionReceipt?> {
		return web3Handler.waitForTransactionReceipt(txHash)
	}

	fun extractResultFromLogs(receipt: TransactionReceipt): Pair<Boolean, String> {
		return web3Handler.extractResultFromLogs(receipt)
	}

	fun requestMockMeasures(coord: String, location: String, radius: Int, count: Byte) {
		mockHandler.requestMeasures(coord, location, radius, count) { newMeasures ->
			insertMultipleMeasures(newMeasures)
		}
	}

	fun getAllMeasures(): LiveData<List<Measure>> = dao.getAllMeasures()

	fun getLastMeasure(): LiveData<Measure> = dao.getLastMeasure()

	fun getLastTenMeasures(): LiveData<List<Measure>> = dao.getLastTenMeasures()

	suspend fun getMeasureById(id: Int): Measure {
		return dao.getMeasureById(id)
	}

	fun deleteMeasure(measure: Measure) {
		MeasureDatabase.databaseExecutor.execute {
			dao.deleteMeasure(measure)
		}
	}

	private fun insertMultipleMeasures(measures: List<Measure>) {
		MeasureDatabase.databaseExecutor.execute {
			dao.insertMultipleMeasures(measures)
		}
		// if notifications are enabled, the permission has been previously granted
		val isNotificationEnabled = preferences.getBoolean("notification", false)
		val threshold = preferences.getString("threshold", null)?.toFloat()
		threshold?.let { t ->
			if (isNotificationEnabled && t >= 0 && t <= 100 && measures.any { m -> m.humidity > t }) {
				notifier.showRequestNotification(
					"Threshold exceeded!",
					"It has been registered a humidity value over $t %"
				)
			}
		}
	}

}