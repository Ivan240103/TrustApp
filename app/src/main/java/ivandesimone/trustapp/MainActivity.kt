package ivandesimone.trustapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import ivandesimone.trustapp.db.MeasureDatabase
import ivandesimone.trustapp.db.MeasureRepository
import ivandesimone.trustapp.remote.RetroCalls
import ivandesimone.trustapp.remote.RetrofitClientInstance
import ivandesimone.trustapp.viewmodels.MeasuresViewModel
import ivandesimone.trustapp.viewmodels.MeasuresViewModelFactory

class MainActivity : AppCompatActivity() {

	private lateinit var navController: NavController
	private lateinit var measuresViewModel: MeasuresViewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// initialization
		setupViewModel()
		setupNavigation()
	}

	override fun onSupportNavigateUp(): Boolean {
		return navController.navigateUp() || super.onSupportNavigateUp()
	}

	private fun setupViewModel() {
		val db = MeasureDatabase.getDatabase(this)
		val api = RetrofitClientInstance.getRetrofitInstance().create(RetroCalls::class.java)
		val repo = MeasureRepository(db.measureDao(), api)
		measuresViewModel =
			ViewModelProvider(this, MeasuresViewModelFactory(repo))[MeasuresViewModel::class.java]
	}

	private fun setupNavigation() {
		// find navController
		val navHostFragment = supportFragmentManager
			.findFragmentById(R.id.navHostFragment) as NavHostFragment
		navController = navHostFragment.navController

		// setup bottom navigation
		val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
		bottomNavigation.setupWithNavController(navController)

		// setup toolbar
		val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)
		val appBarConfiguration =
			AppBarConfiguration(setOf(R.id.dashboardFragment, R.id.requestFragment, R.id.profileFragment))
		setupActionBarWithNavController(navController, appBarConfiguration)
	}

}