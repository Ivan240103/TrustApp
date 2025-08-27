package ivandesimone.trustapp.ui.request

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ivandesimone.trustapp.R
import ivandesimone.trustapp.viewmodels.EthViewModel
import ivandesimone.trustapp.viewmodels.MeasuresViewModel

class RequestFragment : Fragment() {

	private lateinit var ethVM: EthViewModel
	private lateinit var measuresViewModel: MeasuresViewModel
	private lateinit var connectWalletButton: Button
	private lateinit var requestZoniaButton: Button
	private lateinit var requestMockButton: Button
	private lateinit var latEditText: EditText
	private lateinit var longEditText: EditText
	private lateinit var locationEditText: EditText
	private lateinit var radiusEditText: EditText
	private lateinit var countEditText: EditText

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_request, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		ethVM = ViewModelProvider(this)[EthViewModel::class.java]
		measuresViewModel = ViewModelProvider(requireActivity())[MeasuresViewModel::class.java]

		connectWalletButton = view.findViewById(R.id.connect_wallet_button)
		requestZoniaButton = view.findViewById(R.id.request_zonia_button)
		requestMockButton = view.findViewById(R.id.request_mock_button)
		latEditText = view.findViewById(R.id.lat_edittext)
		longEditText = view.findViewById(R.id.long_edittext)
		locationEditText = view.findViewById(R.id.location_edittext)
		radiusEditText = view.findViewById(R.id.radius_edittext)
		countEditText = view.findViewById(R.id.count_edittext)

		connectWalletButton.setOnClickListener {
			ethVM.connectToWallet { uri ->
				// create deeplink to MetaMask
				val deepLink = "metamask://wc?uri=${Uri.encode(uri)}"
				val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(intent)
			}
		}

		requestZoniaButton.setOnClickListener {
			requestZoniaMeasures()
		}

		requestMockButton.setOnClickListener {
				requestMockMeasures()
		}
	}

	private fun requestZoniaMeasures() {
		// define the query JSON as a raw string literal
		val queryString = """
			{
				"topic": "s4agri:AmbientHumidity",
				"geo": {
					"type": "Feature",
					"geometry": {
						"type": "Point",
						"coordinates": [${latEditText.text}, ${longEditText.text}]
					},
					"properties": {
						"radius": ${radiusEditText.text}
					}
				}
			}
			""".trimIndent()
		ethVM.sendTransaction(queryString)
	}

	private fun requestMockMeasures() {
		try {
			measuresViewModel.requestMockMeasures(
				"${latEditText.text}:${longEditText.text}",
				locationEditText.text.toString(), // TODO: substitute with geocoding
				radiusEditText.text.toString().toInt(),
				countEditText.text.toString().toByte()
			)
		} catch (exc: Throwable) {
			Toast.makeText(requireContext(), "Mock data retrieval failed", Toast.LENGTH_SHORT).show()
		}
	}

}