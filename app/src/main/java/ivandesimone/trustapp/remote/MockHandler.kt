package ivandesimone.trustapp.remote

import ivandesimone.trustapp.db.Measure
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date

class MockHandler {
	private val retrofit = RetrofitClientInstance.getRetrofitInstance().create(RetroCalls::class.java)

	fun requestMeasures(
		coord: String,
		location: String,
		radius: Int,
		count: Byte,
		onDataReady: (List<Measure>) -> Unit
	) {
		// enqueue already runs in a background thread
		retrofit.getValues(count).enqueue(object : Callback<List<Int>> {
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
					onDataReady(toInsert)
				}
			}

			override fun onFailure(call: Call<List<Int>>, t: Throwable) {
				throw t
			}
		})
	}
}