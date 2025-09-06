package ivandesimone.trustapp.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import ivandesimone.trustapp.db.Measurement
import ivandesimone.trustapp.viewmodels.MeasurementsViewModel
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DateFormat

/**
 * Details screen
 */
class DetailsFragment : Fragment(), OnMapReadyCallback {

	companion object {
		const val DETAILS_ID = "details-id"
	}

	private lateinit var measurementsViewModel: MeasurementsViewModel
	private lateinit var detailedMeasurement: Measurement
	private lateinit var detailsMap: GoogleMap
	private lateinit var deleteMeasurementButton: Button

	private var isDataReady = false
	private var isMapReady = false

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_details, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		measurementsViewModel = ViewModelProvider(requireActivity())[MeasurementsViewModel::class.java]

		val mapFragment =
			childFragmentManager.findFragmentById(R.id.details_map_fragment) as SupportMapFragment
		mapFragment.getMapAsync(this)

		arguments?.getInt(DETAILS_ID)?.let { id ->
			lifecycleScope.launch {
				detailedMeasurement = measurementsViewModel.getMeasurementById(id)
				isDataReady = true
				initDetails(view)
				addMarker()
			}
		} ?: {
			Toast.makeText(requireActivity(), "Details retrieval failed", Toast.LENGTH_SHORT).show()
			parentFragmentManager.popBackStack()
		}

		deleteMeasurementButton = view.findViewById(R.id.delete_measurement_button)
		deleteMeasurementButton.setOnClickListener {
			val alert = AlertDialog.Builder(requireContext())
				.setTitle("Delete measurement")
				.setMessage("Are you sure you want to delete this measurement? If you change your mind you will have to request it again...")
				.setCancelable(true)
				.setPositiveButton("YES") { _, _ -> deleteMeasurement() }
				.setNegativeButton("NO") { dialog, _ -> dialog.cancel() }
				.create()

			alert.show()
		}
	}

	override fun onMapReady(p0: GoogleMap) {
		detailsMap = p0
		isMapReady = true
		// satellite + locations name
		p0.mapType = GoogleMap.MAP_TYPE_HYBRID
		addMarker()
	}

	/**
	 * Initialize measurement details UI
	 * @param view fragment view
	 */
	private fun initDetails(view: View) {
		val detailsLocation: TextView = view.findViewById(R.id.details_location)
		val detailsTimestamp: TextView = view.findViewById(R.id.details_timestamp)
		val detailsHumidity: TextView = view.findViewById(R.id.details_humidity)
		val detailsIcon: ImageView = view.findViewById(R.id.details_icon)
		val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

		detailsLocation.text = detailedMeasurement.location
		detailsTimestamp.text = formatter.format(detailedMeasurement.timestamp)
		detailsHumidity.text = "${detailedMeasurement.humidity} %"

		// linear interpolation to scale image from 1x to 2.4x
		val scale = 1f + (detailedMeasurement.humidity - 1) / 99f * 1.4f
		val newSize = (120 * scale).toInt()
		val params = detailsIcon.layoutParams
		params.width = newSize
		params.height = newSize
		detailsIcon.layoutParams = params
	}

	/**
	 * Add marker on map
	 */
	private fun addMarker() {
		// execute only when there are both data and map
		if (isDataReady && isMapReady) {
			val coord = detailedMeasurement.coord.split(':').map { it.toDouble() }
			val position = LatLng(coord[0], coord[1])
			detailsMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13f))
			detailsMap.addMarker(
				MarkerOptions()
					.position(position)
					.title(
						"${
							coord[0].toBigDecimal().setScale(6, RoundingMode.HALF_EVEN)
						},  ${
							coord[1].toBigDecimal().setScale(6, RoundingMode.HALF_EVEN)
						}, r = ${detailedMeasurement.radius}m"
					)
			)
			detailsMap.addCircle(
				CircleOptions()
					.center(position)
					.radius(detailedMeasurement.radius.toDouble())
					.strokeColor(resources.getColor(R.color.blue, null))
					.fillColor(resources.getColor(R.color.blue_transparent, null))
			)
		}
	}

	/**
	 * Delete the measurement from database
	 */
	private fun deleteMeasurement() {
		measurementsViewModel.deleteMeasurement(detailedMeasurement)
		parentFragmentManager.popBackStack()
	}

}