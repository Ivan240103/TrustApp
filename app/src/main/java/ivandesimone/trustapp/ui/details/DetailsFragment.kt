package ivandesimone.trustapp.ui.details

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ivandesimone.trustapp.R
import ivandesimone.trustapp.db.Measure
import ivandesimone.trustapp.viewmodels.MeasuresViewModel
import kotlinx.coroutines.launch

class DetailsFragment : Fragment(), OnMapReadyCallback {

	companion object {
		const val DETAILS_ID = "details-id"
	}

	private lateinit var measuresViewModel: MeasuresViewModel
	private lateinit var detailedMeasure: Measure
	private lateinit var detailsMap: GoogleMap
	private lateinit var deleteMeasureButton: Button

	private var isDataReady = false
	private var isMapReady = false

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_details, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		measuresViewModel = ViewModelProvider(requireActivity())[MeasuresViewModel::class.java]

		val mapFragment =
			childFragmentManager.findFragmentById(R.id.details_map_fragment) as SupportMapFragment
		mapFragment.getMapAsync(this)

		arguments?.getInt(DETAILS_ID)?.let { id ->
			lifecycleScope.launch {
				detailedMeasure = measuresViewModel.getMeasureById(id)
				isDataReady = true
				initDetails(view)
				addMarker()
			}
		}

		deleteMeasureButton = view.findViewById(R.id.delete_measure_button)
		deleteMeasureButton.setOnClickListener {
			measuresViewModel.deleteMeasure(detailedMeasure)
			parentFragmentManager.popBackStack()
		}
	}

	// TODO: make map intercept scroll events
	override fun onMapReady(p0: GoogleMap) {
		detailsMap = p0
		isMapReady = true
		p0.mapType = GoogleMap.MAP_TYPE_HYBRID
		addMarker()
	}

	private fun initDetails(view: View) {
		val detailsLocation: TextView = view.findViewById(R.id.details_location)
		val detailsTimestamp: TextView = view.findViewById(R.id.details_timestamp)
		val detailsHumidity: TextView = view.findViewById(R.id.details_humidity)
		val detailsIcon: ImageView = view.findViewById(R.id.details_icon)

		detailsLocation.text = detailedMeasure.location
		detailsTimestamp.text = detailedMeasure.timestamp.toString()
		detailsHumidity.text = "${detailedMeasure.humidity} %"

		// base size in pixels
		val baseSize = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, detailsIcon.width.toFloat(), resources.displayMetrics
		)
		// linear interpolation to scale image
		val scale = 1f + (detailedMeasure.humidity - 1) / 99f * 0.75f
		val newSize = (baseSize * scale).toInt()
		val params = detailsIcon.layoutParams
		params.width = newSize
		params.height = newSize
		detailsIcon.layoutParams = params
	}

	private fun addMarker() {
		if (isDataReady && isMapReady) {
			val coord = detailedMeasure.coord.split(':').map { it.toDouble() }
			val position = LatLng(coord[0], coord[1])
			detailsMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13f))
			detailsMap.addMarker(MarkerOptions().position(position))
			detailsMap.addCircle(CircleOptions()
				.center(position)
				.radius(detailedMeasure.radius.toDouble())
				.strokeColor(resources.getColor(R.color.blue, null))
				.fillColor(resources.getColor(R.color.blue_transparent, null))
			)
		}
	}

}