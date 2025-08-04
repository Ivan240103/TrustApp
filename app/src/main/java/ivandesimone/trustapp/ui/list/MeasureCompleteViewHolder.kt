package ivandesimone.trustapp.ui.list

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ivandesimone.trustapp.R

class MeasureCompleteViewHolder(itemView: View) : ViewHolder(itemView) {
	val rowLocation: TextView = itemView.findViewById(R.id.complete_row_location)
	val rowTimestamp: TextView = itemView.findViewById(R.id.complete_row_timestamp)
	val rowHumidity: TextView = itemView.findViewById(R.id.complete_row_humidity)
}