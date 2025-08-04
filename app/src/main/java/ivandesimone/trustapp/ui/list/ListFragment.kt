package ivandesimone.trustapp.ui.list

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ivandesimone.trustapp.R
import ivandesimone.trustapp.ui.details.DetailsFragment
import ivandesimone.trustapp.viewmodels.MeasuresViewModel
import kotlinx.coroutines.launch

class ListFragment : Fragment() {

	private lateinit var measuresViewModel: MeasuresViewModel
	private lateinit var navController: NavController

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_list, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		measuresViewModel = ViewModelProvider(requireActivity())[MeasuresViewModel::class.java]
		navController = findNavController()

		// immediate navigation if the user wants to see details
		arguments?.getInt(DetailsFragment.DETAILS_ID)?.let { id ->
			navigateToDetails(id)
			// WHEN COMING BACK FROM DETAILS IT AUTOMATICALLY RE-NAVIGATE WITH ID = 0 ---> ERROR !!!
			arguments?.remove(DetailsFragment.DETAILS_ID)
		}

		initRecyclerView(view)
	}

	private fun initRecyclerView(view: View) {
		val completeList: RecyclerView = view.findViewById(R.id.complete_list)
		val adapter = MeasureCompleteAdapter(emptyList()) { measureId: Int ->
			navigateToDetails(measureId)
		}
		completeList.adapter = adapter
		completeList.layoutManager = LinearLayoutManager(context)

		measuresViewModel.allMeasures.observe(viewLifecycleOwner) { newMeasures ->
			adapter.updateMeasures(newMeasures)
		}
	}

	private fun navigateToDetails(id: Int) {
		navController.navigate(
			R.id.action_listFragment_to_detailsFragment,
			bundleOf(DetailsFragment.DETAILS_ID to id)
		)
	}

}