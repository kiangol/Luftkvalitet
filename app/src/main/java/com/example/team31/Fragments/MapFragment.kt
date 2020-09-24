package com.example.team31.Fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.team31.MapComponents.CustomInfoWindowAdapter
import com.example.team31.MapComponents.MarkerActivity
import com.example.team31.ObjectClasses.AirQualityApiModel
import com.example.team31.ObjectClasses.ConvertApiModelToStation
import com.example.team31.R
import com.example.team31.ViewModels.AirQualityViewModel
import com.example.team31.ViewModels.StationsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback {
    //Tag
    private val TAG = "MapFragment"

    //For map
    private lateinit var mMap : GoogleMap
    private var darkModeEnabled = false

    //Parameters
    private val param1 = "lat"
    private val param2 = "long"
    private val param3 = "url"

    //Current Location
    private var latitude = 0.0
    private var longitude = 0.0

    //Button
    private lateinit var darkModeButton : ImageButton

    //Shared ViewModel
    private lateinit var viewModel : AirQualityViewModel
    private lateinit var stationsViewModel: StationsViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        val bundle = this.arguments
        if(bundle != null){
            latitude = bundle.getDouble(param1)
            longitude = bundle.getDouble(param2)
            val baseUrl = bundle.getString(param3)!!

            /* Initializing viewModel with AirQualityViewModel*/
            viewModel = ViewModelProvider(this).get(AirQualityViewModel::class.java)
            makeApiRequestForAllStations(baseUrl)
        }

        /* Upon clicking darkModeButton the
         * style/mode of the map is changed
         */
        darkModeButton = view.findViewById(R.id.darkMode)
        darkModeButton.setOnClickListener {
            toggleMapColors()
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onActivityCreated")
        val mapFragment = childFragmentManager.findFragmentById(R.id.map)as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated")
        activity?.let {
            stationsViewModel = ViewModelProvider(it).get(StationsViewModel::class.java)
        }

        /* Observing airQualityData
         * and calling presentAllStations
         * with list of AirQualityApiModels
         */
        viewModel.airQualityData.observe(viewLifecycleOwner, Observer { airQualityModelList ->
            presentAllStations(airQualityModelList)
        })
    }

    private fun toggleMapColors() {
        Log.i(TAG, "toggleMapColors")
        if (darkModeEnabled) {
            disableDarkMode()
        }
        else {
            enableDarkMode()
        }
    }

    private fun enableDarkMode() {
        Log.i(TAG, "enableDarkMode")
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.style_dark))
        darkModeButton.setImageResource(R.drawable.dark_moon)
        darkModeEnabled = true
    }

    private fun disableDarkMode() {
        Log.i(TAG, "disableDarkMode")
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json))
        darkModeButton.setImageResource(R.drawable.light_moon)
        darkModeEnabled = false
    }

    //TODO
    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(TAG, "onMapReady")
        mMap = googleMap
        if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = false
        val currentPosition = LatLng(latitude,longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 11f))
        val dayNow = Calendar.getInstance()
        val hourNow = dayNow.get(Calendar.HOUR_OF_DAY)
        if (hourNow > 19 || hourNow < 6) {
            enableDarkMode()
        } else {
            disableDarkMode()
        }
    }

    private fun displayStations(stationsMarkerList : List<MarkerActivity>){
        Log.i(TAG, "displayStations")
        stationsMarkerList.forEach {marker ->
            marker.onMapReady(mMap)
        }
    }

    private fun presentAllStations(airQualityModelList : List<AirQualityApiModel>){
        Log.i(TAG, "presentAllStations")

        val stationsMarkerList = mutableListOf<MarkerActivity>()

        /* Converting AirQualityApiModel to Station
         * to gather all values from all components
         * under one station
         */
        val stationsList = ConvertApiModelToStation(
            airQualityModelList
        ).convert()

        stationsList.forEach { station ->
            Log.i(TAG, "Station: ${station.area} : values ${station.getAllComponentValues()}")
            /* Creating MarkerActivity with stations
             * adding them to stationsMarkerList to be
             * displayed on map on displayStations()
             */
            val marker = MarkerActivity(station)
            stationsMarkerList.add(marker)
        }
        stationsViewModel.setStations(stationsList)
        displayStations(stationsMarkerList)
        /* Setting info windows to markers*/
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(requireContext()))
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
        makeApiRequestForAllStations("https://api.nilu.no/aq/utd")
    }


    private fun makeApiRequestForAllStations(baseUrl : String){
        Log.i(TAG, "makeApiRequestForAllStations")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                /* Making API request with the baseUrl
                 * which will get all the station in Norway
                 */
                viewModel.buildRetrofit("$baseUrl/")
                viewModel.allStationsData()
            } catch (e: Exception) {
                Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                Log.i(TAG, e.toString())

            }
        }
    }

}

