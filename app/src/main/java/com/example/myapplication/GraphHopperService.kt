package com.example.myapplication

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

data class GHPoints(
    val type: String,
    val coordinates: List<List<Double>>
)
data class GHPath(
    val distance: Double,
    val time: Long,
    val points: GHPoints,
    val instructions: List<GHInstruction>
)

data class GHInstruction(
    val text: String,
    val distance: Double,
    val time: Long
)
data class GHResponse(val paths: List<GHPath>)

interface GraphHopperService {
    @GET("/api/1/route")
    fun route(
        @Query("point") points: List<String>,
        @Query("vehicle") vehicle: String = "car",
        @Query("locale") locale: String = "ru",
        @Query("points_encoded") pointsEncoded: Boolean = false,
        @Query("instructions") instructions: Boolean = true,
        @Query("key") key: String
    ): Call<GHResponse>
}