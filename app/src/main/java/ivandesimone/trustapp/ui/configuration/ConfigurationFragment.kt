package ivandesimone.trustapp.ui.configuration

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ivandesimone.trustapp.R
import ivandesimone.trustapp.viewmodels.Web3ViewModel
import kotlinx.coroutines.launch

/**
 * Configuration and settings screen
 */
class ConfigurationFragment : Fragment() {

	private lateinit var web3ViewModel: Web3ViewModel
	private lateinit var profileStateInfo: TextView
	private lateinit var connectWalletButton: Button
	private lateinit var metamaskInfoContainer: ConstraintLayout
	private lateinit var sessionTopicValue: TextView
	private lateinit var addressValue: TextView

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_configuration, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		web3ViewModel = ViewModelProvider(requireActivity())[Web3ViewModel::class.java]

		profileStateInfo = view.findViewById(R.id.profile_state_info)
		connectWalletButton = view.findViewById(R.id.connect_wallet_button)
		metamaskInfoContainer = view.findViewById(R.id.metamask_info_container)
		sessionTopicValue = view.findViewById(R.id.session_topic_value)
		addressValue = view.findViewById(R.id.address_value)

		viewLifecycleOwner.lifecycleScope.launch {
			// start a new coroutine on started, until stopped
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				// observe the wallet connection state data
				web3ViewModel.connection.collect { displayProfileState(it)	}
			}
		}
	}

	/**
	 * Change visibility of wallet connection state.
	 * @param value connection state
	 */
	private fun displayProfileState(value: Pair<String?, String?>) {
		if (value.first == null || value.second == null) {
			metamaskInfoContainer.visibility = View.GONE
			profileStateInfo.visibility = View.VISIBLE
			connectWalletButton.visibility = View.VISIBLE
			setConnectWalletListener()
		} else {
			profileStateInfo.visibility = View.GONE
			connectWalletButton.visibility = View.GONE
			metamaskInfoContainer.visibility = View.VISIBLE
			sessionTopicValue.text = value.first
			addressValue.text = value.second
		}
	}

	/**
	 * Set listener to connect MetaMask wallet.
	 */
	private fun setConnectWalletListener() {
		connectWalletButton.setOnClickListener {
			web3ViewModel.connectWallet { uri ->
				// create deeplink to MetaMask
				val deepLink = "metamask://wc?uri=${Uri.encode(uri)}"
				val intent = Intent(Intent.ACTION_VIEW, deepLink.toUri())
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(intent)
			}
		}
	}

}