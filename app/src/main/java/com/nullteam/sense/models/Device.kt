package com.nullteam.sense.models

data class Device(
    val cmd: Int? = null,
    val distance: Double? = null,
    val id: Int? = null,
    val lat: Double? = null,
    val liked: Int? = null,
    val location: String? = null,
    val lon: Double? = null,
    val mac: String? = null,
    val my: Int? = null,
    val name: String? = null,
    val owner: String? = null,
    val sensors: List<Sensor>? = null,
    val time: Int? = null,
    val uptime: Int? = null
)