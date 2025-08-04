package ivandesimone.trustapp.db

import androidx.lifecycle.LiveData

class MeasureRepository(private val dao: MeasureDao) {

	fun addMockData(loc: String) {
		MeasureDatabase.databaseExecutor.execute {
			val randomMeasure = Measure(0, loc, Math.random() * 100)
			dao.insertMeasure(randomMeasure)
		}
	}

	fun getAllMeasures(): LiveData<List<Measure>> = dao.getAllMeasures()

	fun getLastMeasure(): LiveData<Measure> = dao.getLastMeasure()

	fun getLastTenMeasures(): LiveData<List<Measure>> = dao.getLastTenMeasures()

	suspend fun getMeasureById(id: Int): Measure {
		return dao.getMeasureById(id)
	}

}