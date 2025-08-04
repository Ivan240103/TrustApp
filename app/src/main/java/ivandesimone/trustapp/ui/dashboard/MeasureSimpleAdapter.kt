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
	private var measures: List<Measure>
) : BaseAdapter() {

	override fun getCount(): Int = measures.size

	override fun getItem(p0: Int): Any = measures[p0]

	override fun getItemId(p0: Int): Long = measures[p0].id.toLong()

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val v: View = convertView ?:
			LayoutInflater.from(context).inflate(R.layout.row_simple_measure, parent, false)

		val rowLocation: TextView = v.findViewById(R.id.row_location)
		rowLocation.text = measures[position].location
		val rowHumidity: TextView = v.findViewById(R.id.row_humidity)
		rowHumidity.text = measures[position].humidity.roundToInt().toString() + " %"

		return v
	}

	fun updateMeasures(newMeasures: List<Measure>) {
		measures = newMeasures
		notifyDataSetChanged()
	}
}