package ivandesimone.trustapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
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
import ivandesimone.trustapp.db.MeasureDatabase
import ivandesimone.trustapp.db.MeasureRepository
import ivandesimone.trustapp.utils.notifications.Notificator
import ivandesimone.trustapp.utils.notifications.RequestNotificator
import ivandesimone.trustapp.viewmodels.EthViewModel
import ivandesimone.trustapp.viewmodels.EthViewModelFactory
import ivandesimone.trustapp.viewmodels.MeasuresViewModel
import ivandesimone.trustapp.viewmodels.MeasuresViewModelFactory

class MainActivity : AppCompatActivity() {

	private lateinit var navController: NavController
	private lateinit var measuresViewModel: MeasuresViewModel
	private lateinit var ethViewModel: EthViewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// initialization
		setupViewModels()
		setupNavigation()
	}

	override fun onSupportNavigateUp(): Boolean {
		return navController.navigateUp() || super.onSupportNavigateUp()
	}

	private fun setupViewModels() {
		val db = MeasureDatabase.getDatabase(this)
		val preferences = PreferenceManager.getDefaultSharedPreferences(this)
		val requestNotificator = getNotifier()
		val repo = MeasureRepository(db.measureDao(), preferences, requestNotificator)
		measuresViewModel =
			ViewModelProvider(this, MeasuresViewModelFactory(repo))[MeasuresViewModel::class.java]
		ethViewModel = ViewModelProvider(
			this,
			EthViewModelFactory(repo, requestNotificator)
		)[EthViewModel::class.java]
	}

	private fun setupNavigation() {
		// find navController
		val navHostFragment =
			supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
		navController = navHostFragment.navController

		// setup bottom navigation
		val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
		bottomNavigation.setupWithNavController(navController)

		// setup toolbar
		val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
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

		navController.addOnDestinationChangedListener { _, destination, _ ->
			val parent = destination.parent
			val menuItem = parent?.let {
				bottomNavigation.menu.findItem(it.id)
			}
			menuItem?.let {
				if (!it.isChecked) it.isChecked = true
			}
		}
	}

	private fun getNotifier(): RequestNotificator {
		val notificator = Notificator(this, getMetamaskSnack())
		val requestNotificator = RequestNotificator(notificator)
		return requestNotificator
	}

	private fun getMetamaskSnack(): Snackbar {
		val intent = Intent(Intent.ACTION_VIEW, "metamask://".toUri())
		Debug.d("Intent: $intent")
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
