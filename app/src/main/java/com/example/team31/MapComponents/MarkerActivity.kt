package com.example.team31.MapComponents

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.team31.ObjectClasses.Station
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/*private var latLong: LatLng, private var title: String, private val component: Component, private var snippet: String, private var v: Int*/

class MarkerActivity(private val station : Station) : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener
{

    //TAG
    private var TAG = "MarkerActivity"

    //Info window
    private var isInfoWindowShown = false

    //Map
    private lateinit var mMap : GoogleMap
    private lateinit var myMarker : Marker

    //Station variables
    private var title : String = station.station!!
    private var latLong : LatLng = LatLng(station.lat, station.long)

    companion object {
        private val stationValues : HashMap<String, String> = hashMapOf()
    }

    override fun onMapReady(map: GoogleMap) {
        Log.i(TAG, "onMapReady called")
        mMap = map
        mMap.setOnMarkerClickListener(this)
        mMap.setOnInfoWindowClickListener(this)

        val color = station.stationColor()
        val snippet = station.getDesc()
        Log.i(TAG, "STATION: ${station.station} ${station.getDesc()}")
        val value = station.getAllComponentValues()

        stationValues[title] = value

        val markerOptions : MarkerOptions = MarkerOptions()
            .position(latLong)
            .title(title)
            .icon(BitmapDescriptorFactory.defaultMarker(color))
            .snippet(snippet)
        myMarker = mMap.addMarker(markerOptions)
    }

    override fun onMarkerClick(mMarker: Marker?): Boolean {
        Log.i(TAG, "onMarkerClick")
        if (isInfoWindowShown){
            mMarker!!.hideInfoWindow()
            isInfoWindowShown = false
        }else{
            mMarker!!.showInfoWindow()
            isInfoWindowShown = true
        }
        return false
    }

    override fun onInfoWindowClick(mMarker: Marker?) {
        Log.i(TAG, "onInfoWindowClick")
        val snippet = stationValues[mMarker!!.title]!!
        val currentSnippet = mMarker.snippet
        mMarker.snippet = snippet
        stationValues[mMarker.title] = currentSnippet
        mMarker.showInfoWindow()
    }

}