package ivandesimone.trustapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ivandesimone.trustapp.db.MeasureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeasuresViewModel(private val repo: MeasureRepository): ViewModel() {

	val allMeasures = repo.getAllMeasures()

	fun addMockData(location: String) {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				repo.addMockData(location)
			}
		}
	}
}

class MeasuresViewModelFactory(private val repo: MeasureRepository) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return MeasuresViewModel(repo) as T
	}
}