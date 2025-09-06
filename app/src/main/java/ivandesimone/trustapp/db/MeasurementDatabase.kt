package ivandesimone.trustapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Database class implementing singleton pattern.
 */
@Database(entities = [Measurement::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MeasurementDatabase : RoomDatabase() {

	abstract fun measurementDao(): MeasurementDao

	companion object {
		// volatile so it's not cached
		@Volatile
		private var INSTANCE: MeasurementDatabase? = null

		/**
		 * Get the singleton measurements database.
		 * @param context context
		 * @return database instance
		 */
		fun getDatabase(context: Context): MeasurementDatabase {
			// synchronized so only one thread at the time can access
			return INSTANCE ?: synchronized(this) {
				INSTANCE = Room.databaseBuilder(
					context.applicationContext,
					MeasurementDatabase::class.java,
					"measurement_db"
				).build()
				INSTANCE!!
			}
		}

		// max number of threads to use for db operations
		private const val N_THREADS = 8
		val databaseExecutor: ExecutorService = Executors.newFixedThreadPool(N_THREADS)
	}
}