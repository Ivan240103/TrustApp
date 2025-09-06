package ivandesimone.trustapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ivandesimone.trustapp.db.Measurement
import ivandesimone.trustapp.db.MeasurementRepository

/**
 * ViewModel to provide data retrieved from db and mock source.
 * @param repo repository to interact with data sources
 */
class MeasurementsViewModel(private val repo: MeasurementRepository) : ViewModel() {

	val allMeasurements = repo.getAllMeasurements()
	val lastMeasurement = repo.getLastMeasurement()
	val latestMeasurements = repo.getLatestMeasurements()

	// current input to search
	private val searchQuery = MutableLiveData("")

	// filtered list exposed tu ui
	val filteredMeasurements: LiveData<List<Measurement>> =
		MediatorLiveData<List<Measurement>>().apply {
			var source: List<Measurement>? = null
			var query = ""

			fun update() {
				value = if (source == null) {
					emptyList()
				} else if (query.isBlank()) {
					source
				} else {
					source!!.filter { it.location.contains(query, ignoreCase = true) }
				}
			}

			addSource(allMeasurements) {
				source = it
				update()
			}
			addSource(searchQuery) {
				query = it
				update()
			}
		}

	/**
	 * Set the search query value.
	 * @param search location name to search
	 */
	fun setSearchQuery(search: String) {
		searchQuery.value = search
	}

	/**
	 * Retrieve the measurement associated at one specific id.
	 * @param id unique identifier of measurement
	 * @return plain measurement object corresponding
	 */
	suspend fun getMeasurementById(id: Int): Measurement {
		return repo.getMeasurementById(id)
	}

	/**
	 * Request measurements from a mock source of data to be inserted in database.
	 * @param coord coordinates of the point
	 * @param location name of the point location
	 * @param radius radius of the area of interest
	 * @param count number of elements to request
	 */
	fun requestMockMeasurements(coord: String, location: String, radius: Int, count: Byte) {
		repo.requestMockMeasurements(coord, location, radius, count)
	}

	/**
	 * Delete a specific measurement.
	 * @param measurement measurement to remove
	 */
	fun deleteMeasurement(measurement: Measurement) {
		repo.deleteMeasurement(measurement)
	}

}

/**
 * Factory to create MeasurementViewModel
 * @param repo repository to interact with data sources
 */
@Suppress("UNCHECKED_CAST")
class MeasuresViewModelFactory(private val repo: MeasurementRepository) :
	ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return MeasurementsViewModel(repo) as T
	}
}