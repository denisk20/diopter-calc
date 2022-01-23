package org.endmyopia.calc.progress

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.endmyopia.calc.R
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.databinding.FragmentTableBinding
import org.endmyopia.calc.measure.MeasureStateHolder
import org.endmyopia.calc.util.dpt
import java.text.DateFormat

/**
 * @author denisk
 * @since 02.01.2022.
 */

class TableFragment : Fragment() {
    private lateinit var tableBinding: FragmentTableBinding
    private lateinit var adapter: MeasurementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_table, container, false)
        tableBinding = FragmentTableBinding.bind(view)
        tableBinding.lifecycleOwner = this

        val holder: ProgressStateHolder =
            ViewModelProvider(requireActivity()).get(ProgressStateHolder::class.java)

        adapter = MeasurementAdapter(
            android.text.format.DateFormat.getDateFormat(context),
            android.text.format.DateFormat.getTimeFormat(context),
            holder
        )
        holder.data.observe(viewLifecycleOwner, { measurements ->
            adapter.dataSet = measurements
            adapter.notifyDataSetChanged()
        })

        tableBinding.table.adapter = adapter
        tableBinding.table.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        return view
    }
}

class MeasurementAdapter(
    private val dateFormat: DateFormat,
    private val timeFormat: DateFormat,
    private val holder: ProgressStateHolder
) :
    RecyclerView.Adapter<MeasurementAdapter.ViewHolder>() {

    var dataSet: List<Measurement> = listOf()

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.date)
        val type: TextView = view.findViewById(R.id.type)
        val value: TextView = view.findViewById(R.id.value)
        val delete: ImageButton = view.findViewById(R.id.item_delete)
        val context: Context = view.context
        val root: View = view
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fragment_progress_item, viewGroup, false)

        val viewHolder = ViewHolder(view)
        viewHolder.delete.setOnClickListener { v ->
            run {
                holder.selectedValue.postValue(v.tag as Measurement)
                holder.showDeleteDialog(viewGroup.context)
            }
        }
        return viewHolder
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val measurement = dataSet[position]
        val date = java.util.Date(measurement.date)
        viewHolder.date.text = "${dateFormat.format(date)} ${timeFormat.format(date)}"
        viewHolder.type.text = viewHolder.context.getString(
            when (measurement.mode) {
                MeasurementMode.LEFT -> R.string.left_eye_short
                MeasurementMode.RIGHT -> R.string.right_eye_short
                MeasurementMode.BOTH -> R.string.both_eyes_short
            }
        )
        viewHolder.value.text =
            MeasureStateHolder.formatDiopt.format(dpt(measurement.distanceMeters))
        viewHolder.delete.tag = measurement
        viewHolder.root.setBackgroundColor(viewHolder.context.getColor(if (position % 2 == 0) R.color.white else R.color.gray))
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
