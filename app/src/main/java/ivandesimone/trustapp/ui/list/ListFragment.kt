package ivandesimone.trustapp.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ivandesimone.trustapp.R
import ivandesimone.trustapp.ui.details.DetailsFragment
import ivandesimone.trustapp.viewmodels.MeasurementsViewModel

/**
 * Measurements list screen
 */
class ListFragment : Fragment() {

	private lateinit var measurementsViewModel: MeasurementsViewModel
	private lateinit var navController: NavController

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_list, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		measurementsViewModel = ViewModelProvider(requireActivity())[MeasurementsViewModel::class.java]
		navController = findNavController()

		initRecyclerView(view)
	}

	/**
	 * Initialize RecyclerView list.
	 * @param view fragment view
	 */
	private fun initRecyclerView(view: View) {
		val completeListSearch: SearchView = view.findViewById(R.id.complete_list_search)
		val completeList: RecyclerView = view.findViewById(R.id.complete_list)
		val adapter = MeasurementCompleteAdapter(emptyList()) { measurementId: Int ->
			navigateToDetails(measurementId)
		}
		completeList.adapter = adapter
		completeList.layoutManager = LinearLayoutManager(context)

		measurementsViewModel.filteredMeasurements.observe(viewLifecycleOwner) { newMeasurements ->
			adapter.updateMeasurements(newMeasurements)
		}

		completeListSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextChange(p0: String?): Boolean {
				measurementsViewModel.setSearchQuery(p0 ?: "")
				return true
			}

			override fun onQueryTextSubmit(p0: String?): Boolean {
				return false
			}
		})
	}

	/**
	 * Navigate to details fragment.
	 * @param id measurement to see in details id
	 */
	private fun navigateToDetails(id: Int) {
		navController.navigate(
			R.id.action_listFragment_to_detailsFragment,
			bundleOf(DetailsFragment.DETAILS_ID to id)
		)
	}

}