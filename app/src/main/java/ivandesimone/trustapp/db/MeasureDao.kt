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
}