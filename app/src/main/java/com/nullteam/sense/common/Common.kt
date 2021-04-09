package com.nullteam.sense.common

import com.nullteam.sense.`interface`.RetrofitServices
import com.nullteam.sense.retrofit.RetrofitClient

object Common {
    private const val BASE_URL = "https://narodmon.ru/api/"
    val retrofitService: RetrofitServices
        get() = RetrofitClient.getClient(BASE_URL).create(RetrofitServices::class.java)
}