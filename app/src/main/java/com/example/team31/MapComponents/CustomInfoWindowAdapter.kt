package com.example.team31.MapComponents

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.team31.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(context : Context) : GoogleMap.InfoWindowAdapter{

    private var TAG = "CustomInfoWindowAdapter"

    private val mWindow = LayoutInflater.from(context).inflate(R.layout.info_window, null)

    private fun fillWindowText(mMarker : Marker, view : View){
        Log.i(TAG, "fillWindowText")
        val title = mMarker.title
        val info = mMarker.snippet

        val textViewTitle: TextView = view.findViewById(R.id.title)
        val textViewInfo: TextView = view.findViewById(R.id.info)

        if(title != "") textViewTitle.text = title
        if(info != "") textViewInfo.text = info
    }

    override fun getInfoContents(mMarker: Marker): View {
        Log.i(TAG, "getInfoContents")
        fillWindowText(mMarker, mWindow)
        return mWindow
    }

    override fun getInfoWindow(mMarker: Marker): View {
        Log.i(TAG, "getInfoWindow")
        fillWindowText(mMarker, mWindow)
        return mWindow
    }
}