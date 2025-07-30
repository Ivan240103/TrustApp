package ivandesimone.trustapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Measure(
	@PrimaryKey(autoGenerate = true)
	// add autoincrement
	val id: Int,
	val location: String,
	val humidity: Double
)
