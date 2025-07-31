package ivandesimone.trustapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MeasureDao {
	@Insert()
	fun insertMeasure(measure: Measure)

	@Query("SELECT * FROM Measure")
	fun getAllMeasures(): LiveData<List<Measure>>

	@Query("SELECT * FROM Measure ORDER BY id DESC LIMIT 1")
	fun getLastMeasure(): LiveData<Measure>

	@Query("SELECT * FROM Measure ORDER BY id DESC LIMIT 10")
	fun getLastTenMeasures(): LiveData<List<Measure>>
}