package ivandesimone.trustapp.db

import androidx.lifecycle.LiveData

class MeasureRepository(private val measureDao: MeasureDao) {

	suspend fun addMockData(loc: String) {
		measureDao.insertMeasure(Measure(0, loc, Math.random() * 100))
	}

	fun getAllMeasures(): LiveData<List<Measure>> {
		return measureDao.getAllMeasures()
	}
}