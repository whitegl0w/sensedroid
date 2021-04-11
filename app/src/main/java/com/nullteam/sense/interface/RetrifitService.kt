package com.nullteam.sense.`interface`

import com.nullteam.sense.models.Appinit
import com.nullteam.sense.models.NearbySensors
import com.nullteam.sense.models.SensorsHistory
import retrofit2.Call
import retrofit2.http.*

interface RetrofitServices {
    @Headers("User-Agent: SenseDroid", "Content-Type: application/json")
    @GET("sensorsNearby")
    fun getNearbySensorsList(
            @Query("lat") lat: String,
            @Query("lon") lon: String,
            @Query("radius") radius: Int,
            @Query("uuid") uuid: String,
            @Query("api_key") api_key: String,
            @Query("lang") lang: String,
            @Query("types") types: String,
    ): Call<NearbySensors>

    @Headers("User-Agent: SenseDroid", "Content-Type: application/json")
    @GET("sensorsHistory")
    fun getSensorHistoryList(
        @Query("id") id: Int,
        @Query("period") period: String,
        @Query("offset") offset: Int,
        @Query("uuid") uuid: String,
        @Query("api_key") api_key: String,
        @Query("lang") lang: String,
    ): Call<SensorsHistory>

    @Headers("User-Agent: SenseDroid", "Content-Type: application/json")
    @GET("appInit")
    fun appApiInit(
        @Query("version") version: String,
        @Query("platform") platform: String,
        @Query("uuid") uuid: String,
        @Query("api_key") api_key: String,
        @Query("lang") lang: String,
        @Query("utc") utc: Int,
    ): Call<Appinit>
}