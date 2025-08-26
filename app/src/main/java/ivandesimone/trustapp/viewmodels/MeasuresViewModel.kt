package ivandesimone.trustapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ivandesimone.trustapp.db.Measure
import ivandesimone.trustapp.db.MeasureRepository

class MeasuresViewModel(private val repo: MeasureRepository): ViewModel() {

	val allMeasures = repo.getAllMeasures()
	val lastMeasure = repo.getLastMeasure()
	val lastTenMeasures = repo.getLastTenMeasures()

	suspend fun getMeasureById(id: Int): Measure {
		return repo.getMeasureById(id)
	}

	fun requestMockMeasures(coord: String, location: String, radius: Int, count: Byte) {
		repo.requestMockMeasures(coord, location, radius, count)
	}

	fun deleteMeasure(measure: Measure) {
		repo.deleteMeasure(measure)
	}
}

@Suppress("UNCHECKED_CAST")
class MeasuresViewModelFactory(private val repo: MeasureRepository) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return MeasuresViewModel(repo) as T
	}
}