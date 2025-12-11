package com.example.eventapp.event

data class Event(
    val id: String? = null,
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val description: String? = null,
    val capacity: Int? = null,
    val status: String,
    val created_at: String? = null,
    val updated_at: String? = null
)