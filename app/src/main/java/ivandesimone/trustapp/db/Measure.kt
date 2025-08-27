package ivandesimone.trustapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Measure(
	@PrimaryKey(autoGenerate = true)
	val id: Int,
	// coordinates formatted "lat:long"
	val coord: String,
	val location: String,
	val radius: Int,
	val timestamp: Date,
	val humidity: Float
)
