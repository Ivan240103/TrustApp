package ivandesimone.trustapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Main entity for database, represents a single measuration of humidity.
 * @param id unique identifier, use 0 to autogenerate it
 * @param coord coordinates formatted "lat:long"
 * @param location name of the coordinates point
 * @param radius radius of the area of measuration
 * @param timestamp exact instant of measuration
 * @param humidity actual value of humidity registered
 */
@Entity
data class Measurement(
	@PrimaryKey(autoGenerate = true)
	val id: Int,
	val coord: String,
	val location: String,
	val radius: Int,
	val timestamp: Date,
	val humidity: Float
)
