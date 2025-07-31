package ivandesimone.trustapp.ui.request

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import ivandesimone.trustapp.R
import ivandesimone.trustapp.viewmodels.MeasuresViewModel

class RequestFragment : Fragment() {

	private lateinit var measuresViewModel: MeasuresViewModel

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_request, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		measuresViewModel = ViewModelProvider(requireActivity())[MeasuresViewModel::class.java]

		val addMockButton: Button = view.findViewById(R.id.add_mock_button)
		addMockButton.setOnClickListener {
			measuresViewModel.addMockData("Zola Predosa")
		}
	}

}