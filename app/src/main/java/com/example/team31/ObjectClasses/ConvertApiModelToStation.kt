package com.example.team31.ObjectClasses

import android.util.Log

class ConvertApiModelToStation(private val airQualityList : List<AirQualityApiModel>) {

    private val TAG = "ConvertApiModelToStation"

    //PM10 pollution classification
    private val PM10L  = 60.0F //PM10 Little
    private val PM10M  = 120.0F //PM10 Moderate
    private val PM10H  = 400.0F //PM10 High

    //PM2.5 pollution classification
    private val PM25L  = 30.0F //PM2.5 Little
    private val PM25M  = 50.0F //PM2.5 Moderate
    private val PM25H  = 150.0F //PM2.5 High

    //NO2 pollution classification
    private val NO2L  = 100.0F //NO2 Little
    private val NO2M  = 200.0F //NO2 Moderate
    private val NO2H  = 400.0F //NO2 High

    //SO2 pollution classification
    private val SO2L  = 100.0F //SO2 Little
    private val SO2M  = 350.0F //SO2 Moderate
    private val SO2H  = 500.0F //SO2 High

    //O3 pollution classification
    private val O3L  = 100.0F //O3 Little
    private val O3M  = 180.0F //O3 Moderate
    private val O3H  = 240.0F //O3 High


    /* Since there are multiple AirQualityApiModels
     * containing different values for different
     * components but from the same station,
     * all components and value from same station
     * are gathered under in one Station object
     */
    fun convert() : MutableList<Station>{
        Log.i(TAG, "convert")
        /* To avoid making multiple stations of the same station
         * this hashMap is used to hold the name of the station
         * as key and the station object as value,
         * when an AirQualityApiModel has a station name which
         * is present in stationMap, then the station object
         * adds the component to its list
         */
        val stationMap : HashMap<String?, Station> = HashMap()

        airQualityList.forEach { airQualityStation ->
            if(stationMap.containsKey(airQualityStation.station)){
                val s = stationMap[airQualityStation.station]!!
                val c = Component(
                    airQualityStation.component,
                    airQualityStation.value
                )
                classificationOfComponentValue(c)
                s.addComponent(c)
            }else{
                val s = Station(
                    airQualityStation.station,
                    airQualityStation.latitude,
                    airQualityStation.longitude,
                    airQualityStation.area
                )
                val c = Component(
                    airQualityStation.component,
                    airQualityStation.value
                )
                classificationOfComponentValue(c)
                s.addComponent(c)
                stationMap[airQualityStation.station] = s
            }
        }

        val stationsList = mutableListOf<Station>()
        for (station in stationMap.values) {
            stationsList.add(station)
        }
        return stationsList
    }


    /* Classifies the components with
     * the values declared in the beginning
     */
    private fun classificationOfComponentValue(component : Component){
        Log.i(TAG, "classificationOfComponentValue")
        when (component.component) {
            "PM10" -> {
                component.setValueClass(PM10L, PM10M, PM10H)
            }
            "PM2.5" -> {
                component.setValueClass(PM25L, PM25M, PM25H)
            }
            "NO2" -> {
                component.setValueClass(NO2L, NO2M, NO2H)
            }
            "SO2" -> {
                component.setValueClass(SO2L, SO2M, SO2H)
            }
            "O3" -> {
                component.setValueClass(O3L, O3M, O3H)
            }
        }
    }
}