package com.example.eventapp.event

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id")          val id: String? = null,        // HARUS BISA NULL!
    @SerializedName("title")       val title: String,
    @SerializedName("date")         val date: String,
    @SerializedName("time")         val time: String,
    @SerializedName("location")     val location: String,
    @SerializedName("description")  val description: String? = null,
    @SerializedName("capacity")     val capacity: Int? = null,
    @SerializedName("status")       val status: String = "upcoming"
)