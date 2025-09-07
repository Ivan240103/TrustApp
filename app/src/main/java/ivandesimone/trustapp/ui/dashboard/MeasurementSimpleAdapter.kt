package ivandesimone.trustapp.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ivandesimone.trustapp.R
import ivandesimone.trustapp.db.Measurement
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter for ListView
 * @param context context
 * @param measurements measurements to display
 */
class MeasurementSimpleAdapter(
	private val context: Context,
	private var measurements: List<Measurement>
) : BaseAdapter() {

	override fun getCount(): Int = measurements.size

	override fun getItem(p0: Int): Any = measurements[p0]

	override fun getItemId(p0: Int): Long = measurements[p0].id.toLong()

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val v: View = convertView ?:
			LayoutInflater.from(context).inflate(R.layout.row_simple_measure, parent, false)

		val rowLocation: TextView = v.findViewById(R.id.row_location)
		val rowDate: TextView = v.findViewById(R.id.row_date)
		val rowHumidity: TextView = v.findViewById(R.id.row_humidity)

		val formatter = SimpleDateFormat("dd/MM", Locale.ITALY)
		rowLocation.text = measurements[position].location
		rowDate.text = formatter.format(measurements[position].timestamp).substring(0, 5)
		rowHumidity.text = "${measurements[position].humidity} %"

		return v
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