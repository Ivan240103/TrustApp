package ivandesimone.trustapp.ui.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import ivandesimone.trustapp.R
import ivandesimone.trustapp.viewmodels.MeasuresViewModel
import kotlin.math.roundToInt

class DashboardFragment : Fragment() {

	private lateinit var measuresViewModel: MeasuresViewModel

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
		val listHumidity: ListView = view.findViewById(R.id.list_humidity)
		val adapter = MeasureSimpleAdapter(
			requireContext(),
			measuresViewModel.lastTenMeasures.value?.toMutableList() ?: mutableListOf()
		)
		listHumidity.adapter = adapter

		measuresViewModel.lastTenMeasures.observe(viewLifecycleOwner) { newMeasures ->
			adapter.updateMeasures(newMeasures)
		}
	}

}