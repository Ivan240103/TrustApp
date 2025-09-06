package ivandesimone.trustapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Operations against the measurement database.
 */
@Dao
interface MeasurementDao {

	/**
	 * Insert one or more measurements, on conflict the one already existing is kept.
	 * @param measurements list of measurements to insert
	 */
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	fun insertMultipleMeasurements(measurements: List<Measurement>)

	/**
	 * Retrieve all measurements ordered by timestamp descending.
	 * @return observable list of measurements
	 */
	@Query("SELECT * FROM Measurement ORDER BY timestamp DESC")
	fun getAllMeasurements(): LiveData<List<Measurement>>

	/**
	 * Retrieve the last measurement inserted.
	 * @return observable single measurement
	 */
	@Query("SELECT * FROM Measurement ORDER BY timestamp DESC LIMIT 1")
	fun getLastMeasurement(): LiveData<Measurement>

	/**
	 * Retrieve the last ten measurements ordered by timestamp descending.
	 * @return observable list of, at most, ten elements
	 */
	@Query("SELECT * FROM Measurement ORDER BY timestamp DESC LIMIT 10")
	fun getLatestMeasurements(): LiveData<List<Measurement>>

	/**
	 * Retrieve the measurement associated at one specific id.
	 * @param id unique identifier of measurement
	 * @return plain measurement object corresponding
	 */
	@Query("SELECT * FROM Measurement WHERE id = :id")
	suspend fun getMeasurementById(id: Int): Measurement

	/**
	 * Delete a specific measurement.
	 * @param measurement measurement to remove
	 */
	@Delete
	fun deleteMeasurement(measurement: Measurement)
}