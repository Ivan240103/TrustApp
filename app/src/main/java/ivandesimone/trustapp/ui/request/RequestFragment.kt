package ivandesimone.trustapp.ui.request

import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import ivandesimone.trustapp.R
import ivandesimone.trustapp.viewmodels.MeasurementsViewModel
import ivandesimone.trustapp.viewmodels.Web3ViewModel
import java.math.RoundingMode

/**
 * Request data screen
 */
class RequestFragment : Fragment(), OnMapReadyCallback {

	/**
	 * Logger to display info on UI.
	 * @param logs TextView to display data
	 */
	inner class Logger(val logs: TextView) {
		/**
		 * Display data on screen.
		 * @param str string to display
		 */
		fun log(str: String) {
			logs.text = logs.text.toString() + "\n" + str
		}
	}

	private lateinit var web3ViewModel: Web3ViewModel
	private lateinit var measurementsViewModel: MeasurementsViewModel
	private lateinit var geocoder: Geocoder
	private var marker: Marker? = null

	private lateinit var latEditText: EditText
	private lateinit var longEditText: EditText
	private lateinit var locationEditText: EditText
	private lateinit var radiusEditText: EditText
	private lateinit var countEditText: EditText
	private lateinit var logs: TextView

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_request, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		web3ViewModel = ViewModelProvider(requireActivity())[Web3ViewModel::class.java]
		measurementsViewModel = ViewModelProvider(requireActivity())[MeasurementsViewModel::class.java]

		val mapFragment =
			childFragmentManager.findFragmentById(R.id.request_map_fragment) as SupportMapFragment
		mapFragment.getMapAsync(this)

		geocoder = Geocoder(requireContext())

		latEditText = view.findViewById(R.id.lat_edittext)
		longEditText = view.findViewById(R.id.long_edittext)
		locationEditText = view.findViewById(R.id.location_edittext)
		radiusEditText = view.findViewById(R.id.radius_edittext)
		countEditText = view.findViewById(R.id.count_edittext)
		logs = view.findViewById(R.id.request_log)

		val requestZoniaButton: Button = view.findViewById(R.id.request_zonia_button)
		requestZoniaButton.isEnabled = web3ViewModel.connection.value.second != null
		requestZoniaButton.setOnClickListener {
			logs.text = ""
			val logger = Logger(logs)
			requestZoniaMeasurements(logger)
		}

		val requestMockButton: Button = view.findViewById(R.id.request_mock_button)
		requestMockButton.setOnClickListener {
			logs.text = ""
			val logger = Logger(logs)
			requestMockMeasurements(logger)
		}
	}

	override fun onMapReady(p0: GoogleMap) {
		// satellite + locations map
		p0.mapType = GoogleMap.MAP_TYPE_HYBRID
		val position = LatLng(44.4896263748,11.3389703108)
		p0.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 8f))
		marker = p0.addMarker(MarkerOptions().position(position))
		p0.setOnMapClickListener { pos ->
			marker?.let {
				it.position = pos
			} ?: {
				marker = p0.addMarker(MarkerOptions().position(pos))
			}
			latEditText.text = Editable.Factory().newEditable(
				pos.latitude.toBigDecimal().setScale(6, RoundingMode.HALF_EVEN).toString()
			)
			longEditText.text = Editable.Factory().newEditable(
				pos.longitude.toBigDecimal().setScale(6, RoundingMode.HALF_EVEN).toString()
			)
			try {
				val locationResult = geocoder.getFromLocation(pos.latitude, pos.longitude, 1)
				locationEditText.text =
					Editable.Factory().newEditable(locationResult?.get(0)?.locality ?: "Unknown")
			} catch (exc: Exception) {
				Toast.makeText(
					requireContext(),
					"Impossible to find location name",
					Toast.LENGTH_SHORT
				).show()
			}
		}
	}

	/**
	 * Request data from ZONIA.
	 * @param logger logger to display info on UI
	 */
	private fun requestZoniaMeasurements(logger: Logger) {
		val query = Query(
			"s4agri:AmbientHumidity",
			Geo(
				"Feature",
				Geometry(
					"Point",
					listOf(
						latEditText.text?.toString()?.toDouble() ?: 44.0,
						longEditText.text?.toString()?.toDouble() ?: 11.0
					)
				),
				Properties(radiusEditText.text?.toString()?.toInt() ?: 100)
			)
		)
		val queryJson = Gson().toJson(query)
		web3ViewModel.requestZoniaMeasures(queryJson, logger)
	}

	/**
	 * Request data from the mock data source.
	 * @param logger logger to display info on UI
	 */
	private fun requestMockMeasurements(logger: Logger) {
		try {
			measurementsViewModel.requestMockMeasurements(
				"${latEditText.text}:${longEditText.text}",
				locationEditText.text?.toString() ?: "Unknown",
				radiusEditText.text?.toString()?.toInt() ?: 100,
				countEditText.text?.toString()?.toByte() ?: 1
			)
		} catch (exc: Throwable) {
			logger.log("Mock data retrieval failed")
		}
	}

}