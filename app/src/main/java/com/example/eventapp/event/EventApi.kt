package com.example.eventapp.event

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface EventApi {

    @GET("api.php")
    suspend fun getAllEvents(): ApiResponse<List<Event>>

    @GET("api.php")
    suspend fun getEventById(@Query("id") id: String): ApiResponse<Event>

    @GET("api.php")
    suspend fun getEventsByDate(@Query("date") date: String): ApiResponse<List<Event>>

    @GET("api.php")
    suspend fun getEventsByDateRange(
        @Query("date_from") from: String,
        @Query("date_to") to: String
    ): ApiResponse<List<Event>>

    @GET("api.php")
    suspend fun getEventsByStatus(@Query("status") status: String): ApiResponse<List<Event>>

    @GET("api.php")
    suspend fun getStatistics(@Query("stats") stats: Int = 1): ApiResponse<Stats>

    @POST("api.php")
    suspend fun createEvent(@Body event: Event): ApiResponse<Event>

    @PUT("api.php")
    suspend fun updateEvent(@Query("id") id: String, @Body event: Event): ApiResponse<Event>

    @DELETE("api.php")
    suspend fun deleteEvent(@Query("id") id: String): ApiResponse<Any?>

    companion object {
        private const val BASE_URL = "http://104.248.153.158/event-api/"

        fun create(): EventApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(EventApi::class.java)
        }
    }
}