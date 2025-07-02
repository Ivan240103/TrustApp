package ivandesimone.trustapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

	private lateinit var navController: NavController
	private lateinit var bottomNavigation: BottomNavigationView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val navHostFragment = supportFragmentManager
			.findFragmentById(R.id.navHostFragment) as NavHostFragment
		navController = navHostFragment.navController

		bottomNavigation = findViewById(R.id.bottomNavigation)
		bottomNavigation.setupWithNavController(navController)

		val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)
		val appBarConfiguration = AppBarConfiguration(setOf(R.id.dashboardFragment, R.id.requestFragment, R.id.profileFragment))
		setupActionBarWithNavController(navController, appBarConfiguration)
	}

	override fun onSupportNavigateUp(): Boolean {
		return navController.navigateUp() || super.onSupportNavigateUp()
	}

}