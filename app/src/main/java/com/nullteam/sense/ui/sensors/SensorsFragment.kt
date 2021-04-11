package com.nullteam.sense.ui.sensors

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.gson.JsonParser
import com.nullteam.sense.R
import com.nullteam.sense.`interface`.RetrofitServices
import com.nullteam.sense.adapter.NearbySensorsAdapter
import com.nullteam.sense.common.Common
import com.nullteam.sense.models.NearbySensors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SensorsFragment : Fragment() {

    private lateinit var mService: RetrofitServices
    private lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: NearbySensorsAdapter
    lateinit var recycler: RecyclerView
    private var latitude: Double? = null
    private var longitude: Double? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var prefs: SharedPreferences
    private lateinit var uuid: String
    private lateinit var sensTypes: Map<Int, String>
    private val permRequestCode = 1
    private var searchRadius: Int = 0
    private lateinit var searchTypes: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_sensors, container, false)
        recycler = root.findViewById(R.id.recycler_sensors_list)
        prefs = requireActivity().getSharedPreferences(
            "com.nullteam.sense",
            AppCompatActivity.MODE_PRIVATE
        )

        mService = Common.retrofitService
        recycler.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        recycler.layoutManager = layoutManager

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLastKnownLocation()

        return root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permRequestCode)
        {
            if ((grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastKnownLocation()
            } else {
                AlertDialog.Builder(
                    requireContext()
                ).setMessage(getString(R.string.Location_Perm_Explain))
                    .setPositiveButton("Разрешить") { _, _ ->
                        openAppSettings()
                    }.setNegativeButton("Выйти") { _, _ ->
                        requireActivity().finish()
                    }.create().show()
            }
        }
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), permRequestCode
            )
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener(requireActivity()) { location->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    getSensorsList()
                }
                else
                {
                    Log.i("Location", "No Latest Location")
                    val request = LocationRequest.create()
                    request.numUpdates = 1
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            locationResult ?: return
                            if (locationResult.locations.isNotEmpty()) {
                                val locRez =
                                    locationResult.lastLocation
                                latitude = locRez.latitude
                                longitude = locRez.longitude
                                getSensorsList()
                            }
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(
                        request,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
            }
    }

    private fun getSensorsList() {
        Log.i("RESPONSE", "Send Request for Nearby Sensors")
        mService.getNearbySensorsList(
            latitude.toString(),
            longitude.toString(),
            searchRadius,
            uuid,
            getString(R.string.api_key),
            "ru",
            searchTypes
        ).enqueue(object : Callback<NearbySensors> {
            override fun onFailure(call: Call<NearbySensors>, t: Throwable) {
                Log.e("RESPONSE", t.message!!)
            }

            override fun onResponse(call: Call<NearbySensors>, response: Response<NearbySensors>) {
                val sensors = response.body() as NearbySensors
                val sensorsSorted = NearbySensors(devices = sensors.devices!!.sortedBy { it.distance })
                adapter = NearbySensorsAdapter(requireContext(), sensorsSorted, sensTypes)
                adapter.notifyDataSetChanged()
                adapter.setOnItemClickListener(NearbySensorsAdapter.ClickListener(
                    click = fun(sensorId: Int, position: Int, _: View?) {
                        val name = sensorsSorted.devices!![position].sensors!!.find { it.id == sensorId }!!.name
                        Toast.makeText(
                            context,
                            name,
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    longClick = fun(sensorId: Int, position: Int, _: View?) {
                        val bundle = Bundle()
                        bundle.putInt("sensorID", sensorId)
                        bundle.putString("location", sensorsSorted.devices!![position].location)
                        val nav = findNavController(requireActivity(), R.id.nav_host_fragment)
                        nav.navigate(R.id.navigation_graph, bundle)
                    }
                ))
                recycler.adapter = adapter
            }
        })
    }

    private fun openAppSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.parse("package:" + requireContext().packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        startActivityForResult(intent, 1)
    }


    override fun onStart() {
        super.onStart()
        uuid = prefs.getString("uuid_app_key", "")!!
        val json = prefs.getString("sensor_type_dict", "")
        sensTypes = JsonParser().parse(json).asJsonArray.associateBy(
            { it.asJsonObject.get("type").asInt }, { it.asJsonObject.get("name").asString }
        )
        searchRadius = prefs.getInt("search_radius", 0)
        searchTypes = prefs.getStringSet("search_sensor_types", setOf())!!.joinToString(",")
        Log.d("Settings", "Read $searchTypes in Sensors Fragment")
    }
}