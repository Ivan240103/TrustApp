package ivandesimone.trustapp.db

import androidx.lifecycle.LiveData
import ivandesimone.trustapp.remote.MockHandler
import ivandesimone.trustapp.remote.Web3Handler
import kotlinx.coroutines.flow.StateFlow
import java.math.BigInteger

class MeasureRepository(private val dao: MeasureDao) {

	private val mockHandler = MockHandler()
	private val web3Handler = Web3Handler()

	suspend fun approveZoniaTokens(uiState: StateFlow<Pair<String?, String?>>) {
		web3Handler.sendApprove(BigInteger.valueOf(1000000000000000), uiState)
	}

	suspend fun sendTransaction(query: String, uiState: StateFlow<Pair<String?, String?>>) {
		web3Handler.sendTransaction(query, uiState)
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

	// TODO: fire notification if a measure is > threshold
	private fun insertMultipleMeasures(measures: List<Measure>) {
		MeasureDatabase.databaseExecutor.execute {
			dao.insertMultipleMeasures(measures)
		}
		// if (toInsert.any { it.humidity > PreferenceManager.getDefaultSharedPreferences() })
	}

}