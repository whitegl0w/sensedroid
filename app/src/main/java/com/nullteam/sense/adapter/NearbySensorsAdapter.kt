package com.nullteam.sense.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.nullteam.sense.R
import com.nullteam.sense.models.Device
import com.nullteam.sense.models.NearbySensors


class NearbySensorsAdapter(private val context: Context, private val sensorsList: NearbySensors):RecyclerView.Adapter<NearbySensorsAdapter.MyViewHolder>() {
    companion object {
        var clickListener: ClickListener? = null
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val sensorName: TextView = itemView.findViewById(R.id.sensor_name)
        val sensorData: TextView = itemView.findViewById(R.id.sensor_data)
        val sensorType: TextView = itemView.findViewById(R.id.sensor_type)
        val layout: TableLayout = itemView.findViewById(R.id.sensor_table)
        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

//        fun bind(listItem: Device) {
//            sensorName.setOnClickListener {
//                Toast.makeText(it.context, "нажал на $sensorName", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }

        override fun onClick(v: View?) {
            clickListener!!.click!!(adapterPosition, v)
        }

        override fun onLongClick(v: View?): Boolean {
            clickListener!!.longClick!!(adapterPosition, v)
            return false
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
        val listItem = sensorsList.devices!![position]
        // holder.bind(listItem)
        holder.sensorName.text = sensorsList.devices[position].location

        val data = sensorsList.devices[position].sensors!![0].value.toString() +
                sensorsList.devices[position].sensors!![0].unit
        holder.sensorData.text = data

        if (sensorsList.devices[position].sensors!![0].type == 1)
            holder.sensorType.text = "Температура"
        else
            holder.sensorType.text = "Влажность"

        for (i in 1 until sensorsList.devices[position].sensors!!.size)
        {
            val row = TableRow(context)
            val newType = TextView(context)
            newType.layoutParams = holder.sensorType.layoutParams
            if (sensorsList.devices[position].sensors!![i].type == 1)
                newType.text = "Температура"
            else
                newType.text = "Влажность"
            row.addView(newType)
            val newData = TextView(context)
            newData.layoutParams = holder.sensorData.layoutParams
            val dat = sensorsList.devices[position].sensors!![i].value.toString() +
                    sensorsList.devices[position].sensors!![i].unit
            newData.text = dat
            row.addView(newData)
            holder.layout.addView(row)
        }
    }
    fun setOnItemClickListener(clickListener: ClickListener) {
        NearbySensorsAdapter.clickListener = clickListener
    }

    class ClickListener(click: (position: Int, v: View?) -> Unit, longClick: (position: Int, v: View?) -> Unit) {
        var click: ((position: Int, v: View?) -> Unit)? = click
        var longClick: ((position: Int, v: View?) -> Unit)? = longClick
    }
}