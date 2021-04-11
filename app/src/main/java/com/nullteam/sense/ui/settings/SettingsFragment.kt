package com.nullteam.sense.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonParser
import com.nullteam.sense.R

class SettingsFragment : Fragment() {

    private lateinit var homeViewModel: SettingsViewModel
    private lateinit var sensTypes: Map<Int, String>
    private lateinit var prefs: SharedPreferences
    private lateinit var selectedTypes: HashSet<String>
    private var searchRadius: Int = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val layout: LinearLayout = root.findViewById(R.id.settings_layout)
        val seekRadius: SeekBar = root.findViewById(R.id.settings_radius)
        val searchRadiusText: TextView = root.findViewById(R.id.search_radius_text)

        prefs = requireActivity().getSharedPreferences(
            "com.nullteam.sense",
            AppCompatActivity.MODE_PRIVATE
        )
        val json = prefs.getString("sensor_type_dict", "")
        searchRadius = prefs.getInt("search_radius", 0)
        selectedTypes = if (prefs.contains("search_sensor_types"))
            HashSet(prefs.getStringSet("search_sensor_types", setOf()))
        else
            HashSet()
        Log.d("Settings", "Read $selectedTypes in Settings Fragment")
        sensTypes = JsonParser().parse(json).asJsonArray.associateBy(
            { it.asJsonObject.get("type").asInt }, { it.asJsonObject.get("name").asString }
        )

        seekRadius.progress = searchRadius
        searchRadiusText.text = searchRadius.toString()

        seekRadius.setOnSeekBarChangeListener (object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                searchRadiusText.text = seekBar!!.progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {    }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                searchRadius = seekBar!!.progress
                saveSettings()
            }
        })

        sensTypes.forEach {
            val checkbox = CheckBox(requireContext())
            checkbox.text = it.value
            checkbox.isChecked = selectedTypes.contains(it.key.toString())
            checkbox.setOnClickListener { _ ->
                if (checkbox.isChecked)
                    selectedTypes.add(it.key.toString())
                else
                    selectedTypes.remove(it.key.toString())
                saveSettings()
            }
            layout.addView(checkbox)
        }
        return root
    }

    private fun saveSettings()
    {
        prefs.edit()
            .putStringSet("search_sensor_types", selectedTypes)
            .putInt("search_radius", searchRadius)
            .apply()
        Log.d("Settings", "Saved $selectedTypes in Settings Fragment")
    }
}