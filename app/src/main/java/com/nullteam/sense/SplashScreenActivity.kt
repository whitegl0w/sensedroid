package com.nullteam.sense

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nullteam.sense.`interface`.RetrofitServices
import com.nullteam.sense.common.Common
import com.nullteam.sense.models.Appinit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class SplashScreenActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var mService: RetrofitServices
    private lateinit var uuid: String
    private lateinit var int: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        int = Intent(this, MainActivity::class.java)
        mService = Common.retrofitService
        prefs = getSharedPreferences("com.nullteam.sense", MODE_PRIVATE)
    }


    override fun onResume() {
        super.onResume()
        if (!prefs.contains("uuid_app_key"))
            prefs.edit().putString("uuid_app_key", getMD5(Date().time.toString())).apply()

        if (!prefs.contains("search_sensor_types"))
            prefs.edit().putStringSet("search_sensor_types", setOf("1", "2")).apply()

        if (!prefs.contains("search_radius"))
            prefs.edit().putInt("search_radius", 10).apply()

        val timeFromLastTypeUpdate = Date().time - prefs.getLong("sens_type_date", 0)

        if (timeFromLastTypeUpdate > 86400000) // more then 1 day
        {
            uuid = prefs.getString("uuid_app_key", "")!!
            appInit()
        }
        else
        {
            startActivity(int)
            finish()
        }
    }

    private fun getMD5(encTarget: String): String {
        var mdEnc: MessageDigest? = null
        try {
            mdEnc = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            println("Exception while encrypting to md5")
            e.printStackTrace()
        } // Encryption algorithm
        mdEnc?.update(encTarget.toByteArray(), 0, encTarget.length)
        var md5: String = BigInteger(1, mdEnc?.digest()).toString(16)
        while (md5.length < 32) {
            md5 = "0$md5"
        }
        return md5
    }

    private fun appInit() {
        mService.appApiInit(
            "1.0",
            "10",
            uuid,
            getString(R.string.api_key),
            "ru",
            3
        ).enqueue(object : Callback<Appinit> {
            override fun onFailure(call: Call<Appinit>, t: Throwable) {
                Log.e("RESPONSE", t.message!!)
            }

            override fun onResponse(call: Call<Appinit>, response: Response<Appinit>) {
                val appInit = response.body() as Appinit
                if (appInit.timestamp == null)
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.api_error),
                        Toast.LENGTH_SHORT
                    ).show()
                else {
                    Log.d("RESPONSE", "Send Request for App Init")
                    prefs.edit()
                        .putString("sensor_type_dict", appInit.types!!.toString())
                        .putLong("sens_type_date", Date().time)
                        .apply()
                    startActivity(int)
                }
                finishAffinity()
            }
        })
    }
}