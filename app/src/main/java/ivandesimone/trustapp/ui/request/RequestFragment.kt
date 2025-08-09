package ivandesimone.trustapp.ui.request

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ivandesimone.trustapp.R
import ivandesimone.trustapp.viewmodels.EthViewModel

class RequestFragment : Fragment() {

	private lateinit var ethVM: EthViewModel
	private lateinit var connectWalletButton: Button
	private lateinit var sendRequestButton: Button

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_request, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		ethVM = ViewModelProvider(this)[EthViewModel::class.java]

		connectWalletButton = view.findViewById(R.id.connect_wallet_button)
		sendRequestButton = view.findViewById(R.id.send_request_button)

		connectWalletButton.setOnClickListener {
			ethVM.connectToWallet { uri ->
				// create deeplink to MetaMask
				val deepLink = "metamask://wc?uri=${Uri.encode(uri)}"
				val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(intent)
			}
		}

		sendRequestButton.setOnClickListener {
			// Define the query JSON as a raw string literal
			val queryString = """
			{
				"topic": "s4agri:AmbientHumidity",
				"geo": {
					"type": "Feature",
					"geometry": {
						"type": "Point",
						"coordinates": [44.4948, 11.3426]
					},
					"properties": {
						"radius": 500
					}
				}
			}
			""".trimIndent()
			ethVM.sendTransaction(queryString)
		}
	}

}