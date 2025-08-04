package ivandesimone.trustapp.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import ivandesimone.trustapp.R
import ivandesimone.trustapp.db.Measure
import kotlin.math.roundToInt

class MeasureCompleteAdapter(
	private var measures: List<Measure>,
	private val onItemClick: (Int) -> Unit
) :	Adapter<MeasureCompleteViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasureCompleteViewHolder {
		return MeasureCompleteViewHolder(
			LayoutInflater.from(parent.context).inflate(
				R.layout.row_complete_measure,
				parent,
				false
			)
		)
	}

	override fun getItemCount(): Int {
		return measures.size
	}

	override fun onBindViewHolder(holder: MeasureCompleteViewHolder, position: Int) {
		val measure = measures[position]
		holder.apply {
			rowLocation.text = measure.location
			// rowTimestamp.text = measure.
			rowHumidity.text = measure.humidity.roundToInt().toString() + " %"

			itemView.setOnClickListener {
				onItemClick(measure.id)
			}
		}
	}

	fun updateMeasures(newMeasures: List<Measure>) {
		measures = newMeasures
		notifyDataSetChanged()
	}

}