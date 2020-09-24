package com.example.team31.ViewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.team31.ObjectClasses.AirQualityApiModel
import com.example.team31.Interfaces.AirQualityService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AirQualityViewModel() : ViewModel(){

    private val TAG = "AirQualityViewModel"

    //Live list of List<AirQualityApiModel>
    val airQualityData = MutableLiveData<List<AirQualityApiModel>>()

    private lateinit var airQualityService: AirQualityService
    private lateinit var retrofit: Retrofit


    /* Building retrofit with the given baseUrl
     * and initializing airQualityService with AirQualityService
     */
    fun buildRetrofit(baseUrl : String){
        Log.i(TAG, "buildRetrofit")
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        airQualityService = retrofit.create(AirQualityService::class.java)
    }

    /* Fetching the nearest station
     * and setting it as value
     * in airQualityData
     */
    private fun fetchCurrentPositionDataNearestStation(radius : String){
        Log.i(TAG, "fetchCurrentPositionData")
        var apiResponse : List<AirQualityApiModel>
        viewModelScope.launch {
            apiResponse = airQualityService.fetchAirQualityCurrentPositionNearest(radius)
            airQualityData.value = apiResponse
        }
    }

    /* Fetching all stations and
     * inserting them as value in
     * airQualityData
     */
    private fun fetchAllAirQualityStationsData(){
        Log.i(TAG, "fetchAllAirQualityStationsData")
        var apiResponse : List<AirQualityApiModel>
        viewModelScope.launch {
            apiResponse = airQualityService.fetchAllAirQualityStations()
            airQualityData.value = apiResponse
        }
    }

    /* Calls fetchCurrentPositionDataNearestStation
     * with given radius(20km)
     */
    fun currentPositionDataNearestStation(radius: String){
        Log.i(TAG, "currentPositionDataNearestStation")
        fetchCurrentPositionDataNearestStation(radius)
    }

    /* Calls fetchAllAirQualityStationsData*/
    fun allStationsData(){
        Log.i(TAG, "allStaionsData")
        fetchAllAirQualityStationsData()
    }
}