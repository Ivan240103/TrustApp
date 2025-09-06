package ivandesimone.trustapp.db

import androidx.room.TypeConverter
import java.util.Date

/**
 * Types converter for Room database.
 */
class Converters {

	/**
	 * Convert a Long to a Date.
	 * @param value Long to convert
	 * @return Date corresponding
	 */
	@TypeConverter
	fun longToDate(value: Long): Date {
		return Date(value)
	}

	/**
	 * Convert a Date to a Long.
	 * @param date Date to convert
	 * @return Long corresponding
	 */
	@TypeConverter
	fun dateToLong(date: Date): Long {
		return date.time
	}
}