package com.nullteam.sense.models

import com.google.gson.JsonArray

data class Appinit(
    val addr: String? = null,
    val favorites: List<Any>? = null,
    val lat: Double? = null,
    val latest: Any? = null,
    val login: String? = null,
    val lon: Double? = null,
    val timestamp: Int? = null,
    val types: JsonArray? = null,
    val uid: String? = null,
    val url: Any? = null,
    val vip: Int? = null
)