package ivandesimone.trustapp.db

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import ivandesimone.trustapp.remote.MockHandler
import ivandesimone.trustapp.remote.Web3Handler
import ivandesimone.trustapp.utils.notifications.IRequest
import kotlinx.coroutines.flow.StateFlow
import org.web3j.protocol.core.methods.response.TransactionReceipt
import java.math.BigInteger

/**
 * Repository implementation, acting as a mediator between ViewModels and data sources.
 * @param dao dao instance to operate on database
 * @param preferences reference to default shared preferences
 * @param notifier concrete implementation of IRequest contract
 */
class MeasurementRepository(
	private val dao: MeasurementDao,
	private val preferences: SharedPreferences,
	private val notifier: IRequest
) {

	private val mockHandler = MockHandler()
	private val web3Handler = Web3Handler(preferences, notifier)

	/**
	 * Perform the connection to MetaMask's wallet.
	 * @param onUriReady callback to use URI
	 */
	fun connectWallet(onUriReady: (String) -> Unit) {
		web3Handler.connectWallet(onUriReady)
	}

	/**
	 * Blocking function to send approval to Gate for spending ZONIA tokens.
	 * @param connection wallet connection state
	 */
	fun approveZoniaTokens(connection: StateFlow<Pair<String?, String?>>) {
		// displays snack bar to open MetaMask
		web3Handler.sendApprove(BigInteger.valueOf(1000000000000000), connection) {
			notifier.showRequestSnack()
		}
	}

	/**
	 * Blocking function to send the request for data to Gate.
	 * @param query query for the smart contract to execute
	 * @param connection wallet connection state
	 */
	fun sendTransaction(query: String, connection: StateFlow<Pair<String?, String?>>) {
		// displays snack bar to open MetaMask
		web3Handler.sendTransaction(query, connection) {
			notifier.showRequestSnack()
		}
	}

	/**
	 * Blocking function to wait for the transaction to be confirmed by the network.
	 * @param txHash hash of the transaction to wait
	 * @return pair <tx confirmed, tx receipt>
	 */
	suspend fun waitForTransactionReceipt(txHash: String): Pair<Boolean, TransactionReceipt?> {
		return web3Handler.waitForTransactionReceipt(txHash)
	}

	/**
	 * Retrieve the results of the operation from the receipt.
	 * @param receipt receipt of the confirmed transaction
	 * @return pair <is successful, result>
	 */
	fun extractResultFromLogs(receipt: TransactionReceipt): Pair<Boolean, String> {
		return web3Handler.extractResultFromLogs(receipt)
	}

	/**
	 * Request measurements from a mock source of data to be inserted in database.
	 * @param coord coordinates of the point
	 * @param location name of the point location
	 * @param radius radius of the area of interest
	 * @param count number of elements to request
	 */
	fun requestMockMeasurements(coord: String, location: String, radius: Int, count: Byte) {
		mockHandler.requestMeasurements(coord, location, radius, count) { data ->
			// TODO: send notification
			insertMeasurements(data)
		}
	}

	/**
	 * Retrieve all measurements ordered by timestamp descending.
	 * @return observable list of measurements
	 */
	fun getAllMeasurements(): LiveData<List<Measurement>> = dao.getAllMeasurements()

	/**
	 * Retrieve the last measurement inserted.
	 * @return observable single measurement
	 */
	fun getLastMeasurement(): LiveData<Measurement> = dao.getLastMeasurement()

	/**
	 * Retrieve the last ten measurements ordered by timestamp descending.
	 * @return observable list of, at most, ten elements
	 */
	fun getLatestMeasurements(): LiveData<List<Measurement>> = dao.getLatestMeasurements()

	/**
	 * Retrieve the measurement associated at one specific id.
	 * @param id unique identifier of measurement
	 * @return plain measurement object corresponding
	 */
	suspend fun getMeasurementById(id: Int): Measurement = dao.getMeasurementById(id)

	/**
	 * Delete a specific measurement.
	 * @param measurement measurement to remove
	 */
	fun deleteMeasurement(measurement: Measurement) {
		MeasurementDatabase.databaseExecutor.execute {
			dao.deleteMeasurement(measurement)
		}
	}

	/**
	 * Insert one or more measurements, triggering a notification if the threshold is set and active
	 * @param measurements list of measurements to insert
	 */
	fun insertMeasurements(measurements: List<Measurement>) {
		MeasurementDatabase.databaseExecutor.execute {
			dao.insertMultipleMeasurements(measurements)
		}
		// if notifications are enabled, the permission has been previously granted
		val isNotificationEnabled = preferences.getBoolean("notification", false)
		val threshold = preferences.getString("threshold", null)?.toFloat()
		threshold?.let { t ->
			if (isNotificationEnabled && t >= 0 && t <= 100 && measurements.any { m -> m.humidity > t }) {
				notifier.showRequestNotification(
					"Threshold exceeded!",
					"It has been registered a humidity value over $t %"
				)
			}
		}
	}

}