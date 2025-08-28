package ivandesimone.trustapp.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ivandesimone.trustapp.R
import ivandesimone.trustapp.db.Measure
import java.text.DateFormat

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
		val rowDate: TextView = v.findViewById(R.id.row_date)
		val rowHumidity: TextView = v.findViewById(R.id.row_humidity)

		val formatter = DateFormat.getDateInstance(DateFormat.SHORT)
		rowLocation.text = measures[position].location
		rowDate.text = formatter.format(measures[position].timestamp).substring(0, 5)
		rowHumidity.text = "${measures[position].humidity} %"

		return v
	}

	fun updateMeasures(newMeasures: List<Measure>) {
		measures = newMeasures
		notifyDataSetChanged()
	}
}