package ivandesimone.trustapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import ivandesimone.trustapp.db.MeasurementDatabase
import ivandesimone.trustapp.db.MeasurementRepository
import ivandesimone.trustapp.utils.notifications.Notificator
import ivandesimone.trustapp.utils.notifications.RequestNotificator
import ivandesimone.trustapp.viewmodels.MeasurementsViewModel
import ivandesimone.trustapp.viewmodels.MeasuresViewModelFactory
import ivandesimone.trustapp.viewmodels.Web3ViewModel
import ivandesimone.trustapp.viewmodels.Web3ViewModelFactory

/**
 * Main (and only) activity
 */
class MainActivity : AppCompatActivity() {

	private lateinit var navController: NavController
	private lateinit var measurementsViewModel: MeasurementsViewModel
	private lateinit var web3ViewModel: Web3ViewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		// set status bar's text as dark
		WindowCompat.getInsetsController(window, window.decorView).apply {
			isAppearanceLightStatusBars = true
		}


		// initialization
		setupViewModels()
		setupNavigation()
	}

	override fun onSupportNavigateUp(): Boolean {
		return navController.navigateUp() || super.onSupportNavigateUp()
	}

	/**
	 * Instance the ViewModels with their arguments.
	 */
	private fun setupViewModels() {
		val db = MeasurementDatabase.getDatabase(this)
		val preferences = PreferenceManager.getDefaultSharedPreferences(this)
		val requestNotificator = getNotifier()
		val repo = MeasurementRepository(db.measurementDao(), preferences, requestNotificator)
		measurementsViewModel = ViewModelProvider(
			this,
			MeasuresViewModelFactory(repo)
		)[MeasurementsViewModel::class.java]
		web3ViewModel = ViewModelProvider(
			this,
			Web3ViewModelFactory(repo, requestNotificator)
		)[Web3ViewModel::class.java]
	}

	/**
	 * Prepare navigation elements.
	 */
	private fun setupNavigation() {
		// find navController
		val navHostFragment =
			supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
		navController = navHostFragment.navController

		// setup bottom navigation
		val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
		bottomNavigation.setupWithNavController(navController)

		// setup toolbar
		val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
		setSupportActionBar(toolbar)
		val appBarConfiguration =
			AppBarConfiguration(
				setOf(
					R.id.dashboard_fragment,
					R.id.request_fragment,
					R.id.configuration_fragment
				)
			)
		setupActionBarWithNavController(navController, appBarConfiguration)

		// maintain updated highlight on current main fragment
		navController.addOnDestinationChangedListener { _, destination, _ ->
			val parent = destination.parent
			val menuItem = parent?.let {
				bottomNavigation.menu.findItem(it.id)
			}
			menuItem?.let {
				if (!it.isChecked) {
					it.isChecked = true
				}
			}
		}
	}

	/**
	 * Create notificator for data requests
	 * @return notificator
	 */
	private fun getNotifier(): RequestNotificator {
		val notificator = Notificator(this, getMetamaskSnack())
		val requestNotificator = RequestNotificator(notificator)
		return requestNotificator
	}

	/**
	 * Create SnackBar to open MetaMask
	 * @return SnackBar prepared
	 */
	private fun getMetamaskSnack(): Snackbar {
		// deep link to metamask
		val intent = Intent(Intent.ACTION_VIEW, "metamask://".toUri())
		val snack = Snackbar.make(
			findViewById(R.id.main),
			"Check MetaMask to sign the transaction",
			Snackbar.LENGTH_LONG
		).setAction("OPEN") {
			startActivity(intent)
		}
		return snack
	}
}
