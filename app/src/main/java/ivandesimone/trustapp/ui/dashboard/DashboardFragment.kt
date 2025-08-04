package ivandesimone.trustapp.ui.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import ivandesimone.trustapp.R
import ivandesimone.trustapp.ui.details.DetailsFragment
import ivandesimone.trustapp.viewmodels.MeasuresViewModel
import kotlin.math.roundToInt

class DashboardFragment : Fragment() {

	private lateinit var measuresViewModel: MeasuresViewModel
	private lateinit var navController: NavController

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_dashboard, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		measuresViewModel = ViewModelProvider(requireActivity())[MeasuresViewModel::class.java]
		navController = findNavController()

		initCurrentHumidity(view)
		initMeasureList(view)
	}

	private fun initCurrentHumidity(view: View) {
		val currentHumidityData: TextView = view.findViewById(R.id.current_humidity_data)
		measuresViewModel.lastMeasure.observe(viewLifecycleOwner) {
			currentHumidityData.text = "${it.location} - ${it.humidity.roundToInt()} %"
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
			val measureId = measuresViewModel.lastTenMeasures.value?.get(i)?.id
			navController.navigate(
				R.id.action_dashboardFragment_to_listFragment,
				bundleOf(DetailsFragment.DETAILS_ID to measureId)
			)
		}

		measuresViewModel.lastTenMeasures.observe(viewLifecycleOwner) { newMeasures ->
			adapter.updateMeasures(newMeasures)
		}
	}

}