package ivandesimone.trustapp.ui.details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import ivandesimone.trustapp.R
import ivandesimone.trustapp.db.Measure
import ivandesimone.trustapp.viewmodels.MeasuresViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DetailsFragment : Fragment() {

	companion object {
		const val DETAILS_ID = "details-id"
	}

	private lateinit var measuresViewModel: MeasuresViewModel
	private lateinit var detailedMeasure: Measure

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

		arguments?.getInt(DETAILS_ID)?.let { id ->
			lifecycleScope.launch {
				Log.i("debuggao", "Tornati con valore: $id")
				detailedMeasure = measuresViewModel.getMeasureById(id)
				initDetails(view)
			}
		}
	}

	private fun initDetails(view: View) {
		val detailsLocation: TextView = view.findViewById(R.id.details_location)
		val detailsTimestamp: TextView = view.findViewById(R.id.details_timestamp)
		val detailsHumidity: TextView = view.findViewById(R.id.details_humidity)

		detailsLocation.text = detailedMeasure.location
		// detailsTimestamp.text =
		detailsHumidity.text = detailedMeasure.humidity.roundToInt().toString() + " %"
	}

}