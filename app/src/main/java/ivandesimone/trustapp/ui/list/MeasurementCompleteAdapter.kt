package ivandesimone.trustapp.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import ivandesimone.trustapp.R
import ivandesimone.trustapp.db.Measurement
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter for RecyclerView
 * @param measurements measurements to display
 * @param onItemClick callback when an item is clicked
 */
class MeasurementCompleteAdapter(
	private var measurements: List<Measurement>,
	private val onItemClick: (Int) -> Unit
) :	Adapter<MeasurementCompleteViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementCompleteViewHolder {
		return MeasurementCompleteViewHolder(
			LayoutInflater.from(parent.context).inflate(
				R.layout.row_complete_measure,
				parent,
				false
			)
		)
	}

	override fun getItemCount(): Int {
		return measurements.size
	}

	override fun onBindViewHolder(holder: MeasurementCompleteViewHolder, position: Int) {
		val measurement = measurements[position]
		val formatter = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.ITALY)
		holder.apply {
			rowLocation.text = measurement.location
			rowTimestamp.text = formatter.format(measurement.timestamp)
			rowHumidity.text = "${measurement.humidity} %"

			itemView.setOnClickListener {
				onItemClick(measurement.id)
			}
		}
	}

	/**
	 * Update measurements to display
	 * @param newMeasurements new measurements to display
	 */
	fun updateMeasurements(newMeasurements: List<Measurement>) {
		measurements = newMeasurements
		notifyDataSetChanged()
	}

}