package com.nullteam.sense.ui.graph

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.nullteam.sense.R
import com.nullteam.sense.`interface`.RetrofitServices
import com.nullteam.sense.common.Common
import com.nullteam.sense.models.SensorsHistory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class GraphFragment : Fragment() {

    private lateinit var mService: RetrofitServices
    private lateinit var graph: GraphView
    private lateinit var prefs: SharedPreferences
    private lateinit var uuid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_graph, container, false)
        graph = root.findViewById(R.id.graph)
        mService = Common.retrofitService
        prefs = requireActivity().getSharedPreferences("com.nullteam.sense", AppCompatActivity.MODE_PRIVATE)
        uuid = prefs.getString("uuid_app_key", "")!!
        val sensorID = arguments?.getInt("sensorID")
        val location = arguments?.getString("location")
        if (sensorID != null && location != null)
            getSensorHistory(sensorID, location)
        else
            Toast.makeText(
                activity,
                getString(R.string.no_sensor_for_graph),
                Toast.LENGTH_SHORT
            ).show()
        return root
    }

    private fun getSensorHistory(sensorID: Int, location: String) {
        // dialog.show()
        mService.getSensorHistoryList(
            sensorID,
            "year",
            0,
            uuid,
            getString(R.string.api_key),
            "ru",
        ).enqueue(object : Callback<SensorsHistory> {
            override fun onFailure(call: Call<SensorsHistory>, t: Throwable) {
                Log.e("RESPONSE", t.message!!)
            }

            override fun onResponse(
                call: Call<SensorsHistory>,
                response: Response<SensorsHistory>
            ) {
                val history = response.body() as SensorsHistory
                val data = history.data!!.map {
                    DataPoint(it.time!!.toDouble() * 1000, it.value!!)
                }.toTypedArray()

                val series: LineGraphSeries<DataPoint> = LineGraphSeries(data)
                series.isDrawDataPoints = true
                series.dataPointsRadius = 8.0f
                series.setAnimated(true)

                series.setOnDataPointTapListener { _, dataPoint ->
                    val date = Date(dataPoint.x.toLong())
                    val dateFormat = SimpleDateFormat("MM.dd.yyyy hh:mm", Locale.ENGLISH).format(date)
                    Toast.makeText(
                        activity,
                        "$dateFormat\n${dataPoint.y}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                graph.title = location
                graph.gridLabelRenderer.verticalAxisTitle = history.sensors!![0].unit
                graph.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.BOTH
                graph.viewport.isXAxisBoundsManual = true
                val maxData = data.maxOf { it.x }
                graph.viewport.setMaxX(maxData)
                graph.viewport.setMinX(maxData - 86400000)
                graph.viewport.isScalable = true
                graph.addSeries(series)

                graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(activity)
                graph.gridLabelRenderer.numHorizontalLabels = 2
                graph.gridLabelRenderer.numVerticalLabels = 10

                graph.viewport.scrollToEnd()

                Log.d("RESPONSE", "Send Request for History")
            }
        })
    }
}