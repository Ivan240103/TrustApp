package ivandesimone.trustapp.remote

import ivandesimone.trustapp.db.Measurement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date

/**
 * Handler for the remote operations against the mock source.
 */
class MockHandler {

	private val retrofit = RetrofitClientInstance.getRetrofitInstance().create(RetroCalls::class.java)

	/**
	 * Request mock measurements from the remote source.
	 * @param coord coordinates of the point
	 * @param location name of the point location
	 * @param radius radius of the area of interest
	 * @param count number of elements to request
	 * @param onDataReady callback when data are available
	 */
	fun requestMeasurements(
		coord: String,
		location: String,
		radius: Int,
		count: Byte,
		onDataReady: (List<Measurement>) -> Unit
	) {
		// enqueue already runs in a background thread
		retrofit.getValues(count).enqueue(object : Callback<List<Int>> {
			override fun onResponse(call: Call<List<Int>>, response: Response<List<Int>>) {
				// operation successful
				response.body()?.let {
					val toInsert = mutableListOf<Measurement>()
					for (v in it) {
						val humidity = v / 100.0f
						toInsert.add(
							Measurement(0, coord, location, radius, Date(System.currentTimeMillis()), humidity)
						)
					}
					onDataReady(toInsert)
				}
			}

			override fun onFailure(call: Call<List<Int>>, t: Throwable) {
				// operation failed
				throw t
			}
		})
	}
}