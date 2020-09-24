package com.example.team31.Interfaces

import com.example.team31.ObjectClasses.AirQualityApiModel
import retrofit2.http.GET
import retrofit2.http.Path

interface AirQualityService{
    @GET("?")
    suspend fun fetchAllAirQualityStations() : List<AirQualityApiModel>

    @GET("{rad}?")
    suspend fun fetchAirQualityCurrentPositionNearest(
        @Path("rad") radius : String) : List<AirQualityApiModel>

}