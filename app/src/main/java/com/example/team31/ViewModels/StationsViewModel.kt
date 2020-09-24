package com.example.team31.ViewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.team31.ObjectClasses.Station

class StationsViewModel : ViewModel() {

    private val TAG = "StationsViewModel"

    private val stationsList: MutableLiveData<MutableList<Station>>  = MutableLiveData()

    fun getStationsList(): MutableLiveData<MutableList<Station>>{
        Log.i(TAG, "getStationsList")
        return stationsList
    }

    fun setStations(stationList: MutableList<Station>) {
        Log.i(TAG, "setStations")
        stationsList.value = stationList
    }

}