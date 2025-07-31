package ivandesimone.trustapp.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ivandesimone.trustapp.R
import ivandesimone.trustapp.db.Measure
import kotlin.math.roundToInt

class MeasureSimpleAdapter(
	private val context: Context,
	private val measures: MutableList<Measure>
) : BaseAdapter() {

	private data class MeasureSimpleViewHolder (
		val rowLocation: TextView,
		val rowHumidity: TextView
	)

	private val inflater = LayoutInflater.from(context)

	override fun getCount(): Int = measures.size

	override fun getItem(p0: Int): Any = measures[p0]

	override fun getItemId(p0: Int): Long = p0.toLong()

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val v: View = convertView ?: inflater.inflate(R.layout.row_simple_measure, parent, false)
		val holder = MeasureSimpleViewHolder(
			rowLocation = v.findViewById(R.id.row_location),
			rowHumidity = v.findViewById(R.id.row_humidity)
		)

		val measure = measures[position]
		holder.apply {
			rowLocation.text = measure.location
			rowHumidity.text = measure.humidity.roundToInt().toString() + " %"
		}

		return v
	}

	fun updateMeasures(newMeasures: List<Measure>) {
		measures.clear()
		measures.addAll(newMeasures)
		notifyDataSetChanged()
	}
}