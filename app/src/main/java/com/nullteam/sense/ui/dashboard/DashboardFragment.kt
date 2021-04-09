package com.nullteam.sense.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import com.nullteam.sense.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.nullteam.sense.adapter.NearbySensorsAdapter
import com.nullteam.sense.common.Common
import com.nullteam.sense.`interface`.RetrofitServices
import com.nullteam.sense.models.NearbySensors
import dmax.dialog.SpotsDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    lateinit var mService: RetrofitServices
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: NearbySensorsAdapter
    lateinit var recycler: RecyclerView
    private var latitude: Double? = null
    private var longitude: Double? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        recycler = root.findViewById(R.id.recycler_sensors_list)
        //dashboardViewModel.list.observe(viewLifecycleOwner, Observer {
//            val c: Context?  = context
//            if (c != null) {
//                val adapter = ArrayAdapter(c,android.R.layout.simple_list_item_1, it)
//                listView.adapter = adapter
//            }
        //})

        mService = Common.retrofitService
        recycler.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        recycler.layoutManager = layoutManager
        // dialog = SpotsDialog.Builder().setCancelable(true).setContext(this.context).build()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
//        getLastKnownLocation()
        getLocationUpdates()

        return root
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener(requireActivity()) { location->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    getSensorsList()
                }

            }
    }

    private fun getSensorsList() {
        // dialog.show()
        mService.getNearbySensorsList(
                latitude.toString(),
                longitude.toString(),
                "10",
                "f86cca958214c840e49f3084c67b8559",
                "QzS9pErfYZqQe",
                "ru",
                "1,2"
        ).enqueue(object : Callback<NearbySensors> {
            override fun onFailure(call: Call<NearbySensors>, t: Throwable) {
                Log.e("RESPONSE", t.message!!)
            }

            override fun onResponse(call: Call<NearbySensors>, response: Response<NearbySensors>) {
                val sensors = response.body() as NearbySensors
                adapter = NearbySensorsAdapter(requireContext(), sensors)
                adapter.notifyDataSetChanged()
                adapter.setOnItemClickListener(NearbySensorsAdapter.ClickListener(
                    click = fun(position: Int, v: View?) {
                        val id = sensors.devices!![position].sensors!![0].id
                        Toast.makeText(context, "Cенcор с id=$id", Toast.LENGTH_SHORT)
                            .show()
                    },
                    longClick = fun(position: Int, v: View?) {
                        val nav = findNavController(requireActivity(), R.id.nav_host_fragment)
                        nav.navigate(R.id.navigation_home)
                    }
                ))
                recycler.adapter = adapter
                Log.e("RESPONSE", "Send Request")
            }
        })
    }

    private fun getLocationUpdates()
    {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.create()
        locationRequest.interval = 50000
        locationRequest.fastestInterval = 50000
        locationRequest.smallestDisplacement = 170f // 170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    // get latest location
                    val location =
                        locationResult.lastLocation
                    latitude = location.latitude
                    longitude = location.longitude
                    getSensorsList()
                }


            }
        }
    }


    //start location updates
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() /* Looper */
        )
    }

    // stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // stop receiving location update when activity not visible/foreground
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // start receiving location update when activity  visible/foreground
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
}