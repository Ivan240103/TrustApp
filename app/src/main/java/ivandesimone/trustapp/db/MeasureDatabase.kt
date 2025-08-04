package ivandesimone.trustapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [Measure::class], version = 1, exportSchema = false)
abstract class MeasureDatabase : RoomDatabase() {

	abstract fun measureDao(): MeasureDao

	companion object {
		@Volatile
		private var INSTANCE: MeasureDatabase? = null

		fun getDatabase(context: Context): MeasureDatabase {
			return INSTANCE ?: synchronized(this) {
				INSTANCE = Room.databaseBuilder(
					context.applicationContext,
					MeasureDatabase::class.java,
					"measure_db"
				).build()
				INSTANCE!!
			}
		}

		// max number of threads to use for db
		private const val N_THREADS = 8
		val databaseExecutor: ExecutorService = Executors.newFixedThreadPool(N_THREADS)
	}
}