package com.nullteam.sense.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.nullteam.sense.R
import com.nullteam.sense.models.NearbySensors
import com.nullteam.sense.models.Sensor


class NearbySensorsAdapter(private val context: Context, private val sensorsList: NearbySensors, private val sensTypes: Map<Int, String>)
    :RecyclerView.Adapter<NearbySensorsAdapter.MyViewHolder>() {

    companion object {
        var clickListener: ClickListener? = null
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val sensorName: TextView = itemView.findViewById(R.id.sensor_name)
        val sensorData: TextView = itemView.findViewById(R.id.sensor_data)
        val sensorType: TextView = itemView.findViewById(R.id.sensor_type)
        val layout: TableLayout = itemView.findViewById(R.id.sensor_table)

        fun bind(row: TableRow, position: Int, sensor: Sensor) {
            row.setOnClickListener {
                clickListener!!.click!!(sensor.id!!, position, it)
            }
            row.setOnLongClickListener {
                clickListener!!.longClick!!(sensor.id!!, position, it)
                false
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.sensor_layout,
            parent,
            false
        )
        return MyViewHolder(itemView)
    }

    override fun getItemCount() = sensorsList.devices!!.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val location = sensorsList.devices!![position].location +
                " (${sensorsList.devices[position].distance}км.)"
        holder.sensorName.text = location
        holder.layout.removeViews(2,holder.layout.childCount - 2)
        var sensor = sensorsList.devices[position].sensors!![0]
        holder.bind(holder.layout[1] as TableRow, position, sensor)
        var data = "${sensor.value.toString()}${sensor.unit}"
        holder.sensorData.text = data
        holder.sensorType.text = sensTypes[sensor.type]
        for (i in 1 until sensorsList.devices[position].sensors!!.size)
        {
            val row = TableRow(context)
            val newType = TextView(context)
            newType.layoutParams = holder.sensorType.layoutParams
            sensor = sensorsList.devices[position].sensors!![i]
            holder.bind(row, position, sensor)
            newType.text = sensTypes[sensor.type]
            row.addView(newType)
            val newData = TextView(context)
            newData.layoutParams = holder.sensorData.layoutParams
            data = "${sensor.value.toString()}${sensor.unit}"
            newData.text = data
            row.addView(newData)
            holder.layout.addView(row)
        }
    }
    fun setOnItemClickListener(clickListener: ClickListener) {
        NearbySensorsAdapter.clickListener = clickListener
    }

    class ClickListener(
        click: (sensorId: Int, position: Int, v: View?) -> Unit,
        longClick: (sensorId: Int, position: Int, v: View?) -> Unit
    ) {
        var click: ((sensorId: Int, position: Int, v: View?) -> Unit)? = click
        var longClick: ((sensorId: Int, position: Int, v: View?) -> Unit)? = longClick
    }
}