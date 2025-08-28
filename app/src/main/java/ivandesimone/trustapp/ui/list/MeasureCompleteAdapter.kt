package ivandesimone.trustapp.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import ivandesimone.trustapp.R
import ivandesimone.trustapp.db.Measure
import java.text.DateFormat

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
		val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
		holder.apply {
			rowLocation.text = measure.location
			rowTimestamp.text = formatter.format(measure.timestamp)
			rowHumidity.text = "${measure.humidity} %"

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