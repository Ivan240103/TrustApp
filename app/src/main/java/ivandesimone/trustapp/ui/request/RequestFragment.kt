package ivandesimone.trustapp.ui.request

import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import ivandesimone.trustapp.viewmodels.EthViewModel
import ivandesimone.trustapp.viewmodels.MeasuresViewModel

class RequestFragment : Fragment(), OnMapReadyCallback {

	private lateinit var ethViewModel: EthViewModel
	private lateinit var measuresViewModel: MeasuresViewModel
	private lateinit var geocoder: Geocoder

	private lateinit var latEditText: EditText
	private lateinit var longEditText: EditText
	private lateinit var locationEditText: EditText
	private lateinit var radiusEditText: EditText
	private lateinit var countEditText: EditText
	private var marker: Marker? = null

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_request, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		ethViewModel = ViewModelProvider(requireActivity())[EthViewModel::class.java]
		measuresViewModel = ViewModelProvider(requireActivity())[MeasuresViewModel::class.java]

		val mapFragment =
			childFragmentManager.findFragmentById(R.id.request_map_fragment) as SupportMapFragment
		mapFragment.getMapAsync(this)

		geocoder = Geocoder(requireContext())

		latEditText = view.findViewById(R.id.lat_edittext)
		longEditText = view.findViewById(R.id.long_edittext)
		locationEditText = view.findViewById(R.id.location_edittext)
		radiusEditText = view.findViewById(R.id.radius_edittext)
		countEditText = view.findViewById(R.id.count_edittext)

		val requestZoniaButton: Button = view.findViewById(R.id.request_zonia_button)
		requestZoniaButton.isEnabled = ethViewModel.uiState.value.second != null
		requestZoniaButton.setOnClickListener {
			requestZoniaMeasures()
		}

		val requestMockButton: Button = view.findViewById(R.id.request_mock_button)
		requestMockButton.setOnClickListener {
			requestMockMeasures()
		}
	}

	override fun onMapReady(p0: GoogleMap) {
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
			latEditText.text = Editable.Factory().newEditable(pos.latitude.toString())
			longEditText.text = Editable.Factory().newEditable(pos.longitude.toString())
			try {
				val locationResult = geocoder.getFromLocation(pos.latitude, pos.longitude, 1)
				locationEditText.text =
					Editable.Factory().newEditable(locationResult?.get(0)?.locality ?: "Unknown")
			} catch (exc: Exception) {
				Toast.makeText(requireContext(), "Impossible to find location name", Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun requestZoniaMeasures() {
		// define the query JSON as a raw string literal
		val query = Query(
			"s4agri:AmbientHumidity",
			Geo(
				"Feature",
				Geometry(
					"Point",
					listOf(latEditText.text.toString().toDouble(), longEditText.text.toString().toDouble())
				),
				Properties(radiusEditText.text.toString().toInt())
			)
		)
		val queryJson = Gson().toJson(query)
		ethViewModel.requestZoniaMeasures(queryJson)
	}

	private fun requestMockMeasures() {
		try {
			measuresViewModel.requestMockMeasures(
				"${latEditText.text}:${longEditText.text}",
				locationEditText.text.toString(),
				radiusEditText.text.toString().toInt(),
				countEditText.text.toString().toByte()
			)
			Toast.makeText(requireContext(), "Data obtained!", Toast.LENGTH_SHORT).show()
		} catch (exc: Throwable) {
			Toast.makeText(requireContext(), "Mock data retrieval failed", Toast.LENGTH_SHORT).show()
		}
	}

}