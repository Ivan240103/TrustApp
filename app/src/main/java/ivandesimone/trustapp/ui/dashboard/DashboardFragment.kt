package ivandesimone.trustapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
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
import ivandesimone.trustapp.R
import ivandesimone.trustapp.ui.details.DetailsFragment
import ivandesimone.trustapp.viewmodels.MeasuresViewModel
import java.text.DateFormat

class DashboardFragment : Fragment() {

	private lateinit var measuresViewModel: MeasuresViewModel
	private lateinit var navController: NavController

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_dashboard, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		measuresViewModel = ViewModelProvider(requireActivity())[MeasuresViewModel::class.java]
		navController = findNavController()

		initLastHumidity(view)
		initMeasureList(view)
	}

	private fun initLastHumidity(view: View) {
		val lastHumidityLocation: TextView = view.findViewById(R.id.last_humidity_location)
		val lastHumidityTimestamp: TextView = view.findViewById(R.id.last_humidity_timestamp)
		val lastHumidityIcon: ImageView = view.findViewById(R.id.last_humidity_icon)
		val lastHumidityValue: TextView = view.findViewById(R.id.last_humidity_value)
		val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

		measuresViewModel.lastMeasure.observe(viewLifecycleOwner) { measure ->
			measure?.let {
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

	private fun initMeasureList(view: View) {
		val cardListHumidity: CardView = view.findViewById(R.id.card_list_humidity)
		cardListHumidity.setOnClickListener {
			navController.navigate(R.id.action_dashboardFragment_to_listFragment)
		}

		val listHumidity: ListView = view.findViewById(R.id.list_humidity)
		val adapter = MeasureSimpleAdapter(
			requireContext(),
			measuresViewModel.lastTenMeasures.value ?: listOf()
		)
		listHumidity.adapter = adapter
		listHumidity.setOnItemClickListener { _, _, i, _ ->
			measuresViewModel.lastTenMeasures.value?.get(i)?.let {
				navController.navigate(
					R.id.action_dashboardFragment_to_detailsFragment,
					bundleOf(DetailsFragment.DETAILS_ID to it.id)
				)
			}
		}

		measuresViewModel.lastTenMeasures.observe(viewLifecycleOwner) { newMeasures ->
			adapter.updateMeasures(newMeasures)
		}
	}

}