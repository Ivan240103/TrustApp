package ivandesimone.trustapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

		measuresViewModel.allMeasures.observe(viewLifecycleOwner) {
			val currentHumidityData = view.findViewById<TextView>(R.id.current_humidity_data)
			// TODO error when list is empty ???
			if (it.isNotEmpty()) {
				currentHumidityData.text = "${it.last().location} - ${it.last().humidity.roundToInt()} %"
			}
		}

		val addMockButton: Button = view.findViewById(R.id.add_mock_button)
		addMockButton.setOnClickListener {
			measuresViewModel.addMockData("Zola Predosa")
		}
	}
}