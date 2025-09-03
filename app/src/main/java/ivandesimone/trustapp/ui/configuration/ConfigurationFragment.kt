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
import ivandesimone.trustapp.remote.Web3Handler
import ivandesimone.trustapp.viewmodels.EthViewModel
import kotlinx.coroutines.launch

class ConfigurationFragment : Fragment() {

	private lateinit var ethViewModel: EthViewModel
	private lateinit var profileStateInfo: TextView
	private lateinit var connectWalletButton: Button
	private lateinit var metamaskInfoContainer: ConstraintLayout
	private lateinit var sessionTopicValue: TextView
	private lateinit var addressValue: TextView

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_configuration, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		ethViewModel = ViewModelProvider(requireActivity())[EthViewModel::class.java]

		profileStateInfo = view.findViewById(R.id.profile_state_info)
		connectWalletButton = view.findViewById(R.id.connect_wallet_button)
		metamaskInfoContainer = view.findViewById(R.id.metamask_info_container)
		sessionTopicValue = view.findViewById(R.id.session_topic_value)
		addressValue = view.findViewById(R.id.address_value)

		viewLifecycleOwner.lifecycleScope.launch {
			// start a new coroutine on started until stopped
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				// observe the data
				ethViewModel.uiState.collect { displayProfileState(it)	}
			}
		}
	}

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

	private fun setConnectWalletListener() {
		val web3Handler = Web3Handler()
		connectWalletButton.setOnClickListener {
			web3Handler.connectWallet { uri ->
				// create deeplink to MetaMask
				val deepLink = "metamask://wc?uri=${Uri.encode(uri)}"
				val intent = Intent(Intent.ACTION_VIEW, deepLink.toUri())
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(intent)
			}
		}
	}

}