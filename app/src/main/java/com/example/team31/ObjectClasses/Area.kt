package com.example.team31.ObjectClasses

import java.io.Serializable

class Area (val area : String?) : Serializable{
    private var stations : MutableList<Station> = mutableListOf()

    fun addStation(station : Station){
        stations.add(station)
    }

    fun getStations() : MutableList<Station>{
        stations = stations.sortedBy { it.station } as MutableList<Station>
        return stations
    }
}