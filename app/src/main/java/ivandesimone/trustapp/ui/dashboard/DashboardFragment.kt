package ivandesimone.trustapp.ui.dashboard

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import ivandesimone.trustapp.R
import ivandesimone.trustapp.ui.details.DetailsFragment
import ivandesimone.trustapp.viewmodels.MeasurementsViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dashboard screen
 */
class DashboardFragment : Fragment() {

	private lateinit var measurementsViewModel: MeasurementsViewModel
	private lateinit var navController: NavController

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_dashboard, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		measurementsViewModel = ViewModelProvider(requireActivity())[MeasurementsViewModel::class.java]
		navController = findNavController()

		initLastHumidity(view)
		initScatterChart(view)
		initMeasureList(view)
	}

	/**
	 * Initialize last humidity UI.
	 * @param view fragment view
	 */
	private fun initLastHumidity(view: View) {
		val lastHumidityLocation: TextView = view.findViewById(R.id.last_humidity_location)
		val lastHumidityTimestamp: TextView = view.findViewById(R.id.last_humidity_timestamp)
		val lastHumidityIcon: ImageView = view.findViewById(R.id.last_humidity_icon)
		val lastHumidityValue: TextView = view.findViewById(R.id.last_humidity_value)
		val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

		measurementsViewModel.lastMeasurement.observe(viewLifecycleOwner) { m ->
			m?.let {
				lastHumidityLocation.text = it.location
				lastHumidityTimestamp.text = formatter.format(it.timestamp)
				lastHumidityValue.text = "${it.humidity} %"

				// linear interpolation to scale image from 1x to 2.4x
				val scale = 1f + (it.humidity - 1) / 99f * 1.4f
				val newSize = (120 * scale).toInt()
				val params = lastHumidityIcon.layoutParams
				params.width = newSize
				params.height = newSize
				lastHumidityIcon.layoutParams = params
			}
		}
	}

	/**
	 * Initialize scatter chart.
	 * @param view fragment view
	 */
	private fun initScatterChart(view: View) {
		val chart: ScatterChart = view.findViewById(R.id.scatter_chart)

		measurementsViewModel.allMeasurements.observe(viewLifecycleOwner) { newMeasurements ->
			val dataSets = mutableListOf<ScatterDataSet>()
			val grouped = newMeasurements.groupBy { it.location }
			var colorIndex = 0

			for((location, measurementsAtLocation) in grouped) {
				val entries = measurementsAtLocation.map { m ->
					Entry(m.timestamp.time.toFloat(), m.humidity)
				}
				val dataSet = ScatterDataSet(entries, location)
				dataSet.apply {
					color = generateColor(colorIndex)
					setScatterShape(ScatterChart.ScatterShape.CIRCLE)
					scatterShapeSize = 10f
				}
				dataSets.add(dataSet)
				colorIndex++
			}

			val scatterData = ScatterData(dataSets as List<ScatterDataSet>?)
			chart.data = scatterData

			val formatter = SimpleDateFormat("dd/MM/yy", Locale.ITALY)
			chart.apply {
				xAxis.apply {
					valueFormatter = object: ValueFormatter() {
						override fun getAxisLabel(value: Float, axis: AxisBase?): String {
							return formatter.format(Date(value.toLong()))
						}
					}
					setLabelCount(5, true)
				}

				axisLeft.axisMinimum = 0f
				axisLeft.axisMaximum = 100f
				axisRight.isEnabled = false

				legend.apply {
					verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
					horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
					orientation = Legend.LegendOrientation.HORIZONTAL
					isWordWrapEnabled = true
				}

				description.isEnabled = false
				invalidate()
			}
		}
	}

	/**
	 * Generate colors for chart.
	 * @param index index of color
	 * @return color representation
	 */
	private fun generateColor(index: Int): Int {
		// generate distinct colors in HSV space, rotating hue for each location
		val hue = (index * 40f) % 360
		return Color.HSVToColor(floatArrayOf(hue, 0.8f, 0.9f))
	}

	/**
	 * Init ListView.
	 * @param view fragment view
	 */
	@SuppressLint("ClickableViewAccessibility")
	private fun initMeasureList(view: View) {
		val cardListHumidity: CardView = view.findViewById(R.id.card_list_humidity)
		cardListHumidity.setOnClickListener {
			navController.navigate(R.id.action_dashboardFragment_to_listFragment)
		}

		val listHumidity: ListView = view.findViewById(R.id.list_humidity)
		val adapter = MeasurementSimpleAdapter(
			requireContext(),
			measurementsViewModel.latestMeasurements.value ?: listOf()
		)
		listHumidity.adapter = adapter
		listHumidity.setOnItemClickListener { _, _, i, _ ->
			measurementsViewModel.latestMeasurements.value?.get(i)?.let {
				navController.navigate(
					R.id.action_dashboardFragment_to_detailsFragment,
					bundleOf(DetailsFragment.DETAILS_ID to it.id)
				)
			}
		}
		// to intercept the movement so the page doesn't scroll
		listHumidity.setOnTouchListener { v, event ->
			when (event.action) {
				MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
					v.parent.requestDisallowInterceptTouchEvent(true)
				}
				MotionEvent.ACTION_UP -> {
					v.parent.requestDisallowInterceptTouchEvent(false)
				}
			}
			false
		}

		measurementsViewModel.latestMeasurements.observe(viewLifecycleOwner) { newMeasures ->
			adapter.updateMeasurements(newMeasures)
		}
	}

}