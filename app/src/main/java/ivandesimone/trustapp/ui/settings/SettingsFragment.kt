package ivandesimone.trustapp.ui.settings

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import ivandesimone.trustapp.R

class SettingsFragment : PreferenceFragmentCompat() {

	private var notificationSwitch: SwitchPreferenceCompat? = null

	// permission to send notifications
	private val notificationPermissionLauncher = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted: Boolean ->
		notificationSwitch?.let { it.isChecked = isGranted }
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey)

		notificationSwitch = findPreference("notification")

		// this listener operates before the state of the preference is changed
		notificationSwitch?.onPreferenceChangeListener =
			Preference.OnPreferenceChangeListener { _, newValue ->
				val isEnabled = newValue as Boolean
				if (!isEnabled) {
					// always allow to disable the switch
					return@OnPreferenceChangeListener true
				}

				if (ContextCompat.checkSelfPermission(
						requireContext(),
						android.Manifest.permission.POST_NOTIFICATIONS
					) == PackageManager.PERMISSION_GRANTED
				) {
					return@OnPreferenceChangeListener true
				} else {
					// permission is not granted, launch the request
					notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
					// it will be the launcher to change the state of the preference eventually
					return@OnPreferenceChangeListener false
				}
			}
	}

}