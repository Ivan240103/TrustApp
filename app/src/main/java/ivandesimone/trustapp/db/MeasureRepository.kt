package ivandesimone.trustapp.db

import androidx.lifecycle.LiveData
import ivandesimone.trustapp.Debug
import ivandesimone.trustapp.remote.RetroCalls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date

class MeasureRepository(private val dao: MeasureDao, private val retrofit: RetroCalls) {

	private val web3j = Web3j.build(HttpService("https://eth-sepolia.g.alchemy.com/v2/0YwukylbE3vy-oWWK218B"))
	private val contractAddress = "0xbb6849DC5D97Bd55DE9A23B58CD5bBF3Bfdda0FA"

	// TODO: function getRequest()

	suspend fun getResult(requestId: ByteArray): String? = withContext(Dispatchers.IO) {
		val function = Function(
			"getResult",
			listOf(Bytes32(requestId)),
			listOf(object : TypeReference<Utf8String>() {})
		)

		val encodedFunction = FunctionEncoder.encode(function)

		val response = web3j.ethCall(
			Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
			DefaultBlockParameterName.LATEST
		).send()

		if (response != null && !response.hasError() && response.value.isNotEmpty()) {
			val output = FunctionReturnDecoder.decode(response.value, function.outputParameters)
			return@withContext output.firstOrNull()?.value as? String
		} else {
			Debug.e("getResult error with web3j")
			return@withContext null
		}
	}

	// TODO: fire notification if a measure is > threshold
	fun requestMockMeasures(coord: String, location: String, radius: Int, count: Byte) {
		// enqueue already runs in a background thread
		retrofit.getValues(count).enqueue(object: Callback<List<Int>> {
			override fun onResponse(call: Call<List<Int>>, response: Response<List<Int>>) {
				// get values online and insert them in the db
				response.body()?.let {
					val toInsert = mutableListOf<Measure>()
					for (v in it) {
						val humidity: Float = v / 100.0f
						toInsert.add(
							Measure(0, coord, location, radius, Date(System.currentTimeMillis()), humidity)
						)
					}
					MeasureDatabase.databaseExecutor.execute {
						dao.insertMultipleMeasures(toInsert)
					}
				}
			}

			override fun onFailure(call: Call<List<Int>>, t: Throwable) {
				throw t
			}
		})
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

}