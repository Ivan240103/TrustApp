package ivandesimone.trustapp.db

import androidx.lifecycle.LiveData
import ivandesimone.trustapp.remote.RetroCalls
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date

class MeasureRepository(private val dao: MeasureDao, private val retrofit: RetroCalls) {

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

	// TODO: polling to blockchain and insert data in db
	fun addMultipleMeasures(measures: List<Measure>) = dao.insertMultipleMeasures(measures)

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