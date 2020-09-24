package com.example.team31

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.team31.Fragments.ExploreFragment
import com.example.team31.Fragments.HomeFragment
import com.example.team31.Fragments.MapFragment
import com.example.team31.Interfaces.DialogDismissedListener
import com.example.team31.ObjectClasses.LocationPopupDialog
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView


open class MainActivity : AppCompatActivity(),
    DialogDismissedListener {

    //Tag
    private val TAG = "MainActivity"

    //BaseUrls
    private val currentLocationBaseUrl = "https://api.nilu.no/aq/utd"

    //Current Location
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    // private lateinit var locationRequest: LocationRequest

    //Default Location
     /* used when using the app without location services */
    private val defaultLatitude : Double = 59.91149
    private val defaultLongitude : Double = 10.757933

    //Fragment Manager
    private val fm: FragmentManager? = supportFragmentManager

    //Fragments
    private lateinit var home : HomeFragment
    private lateinit var map : MapFragment
    private lateinit var explore : ExploreFragment
    private lateinit var active : Fragment

    //Navigation Controller
    private lateinit var bottomNavigationView : BottomNavigationView

    //Permission ID
    private val permissionID = 1312

    //Popup Dialog
    private lateinit var popupDialog : LocationPopupDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate called")
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        home  = HomeFragment()
        map = MapFragment()
        explore = ExploreFragment()
        active = home

        home.retainInstance = true
        map.retainInstance = true
        explore.retainInstance = true

        bottomNavigationView = findViewById(R.id.bttm_nav)
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        popupDialog = LocationPopupDialog()
        popupDialog.setDialogDismissed(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    private fun setHomeFragments(lat : Double, long : Double){
        Log.i(TAG, "setHomeFragments")
        val bundle = Bundle()
        bundle.putString("url", currentLocationBaseUrl)
        bundle.putDouble("currentLat", lat)
        bundle.putDouble("currentLong", long)

        home.arguments = bundle
        fm!!.beginTransaction().add(R.id.nav_host_fragment,home, "home").commit()
        active = home
    }

    private fun setMapAndSearchFragment(lat : Double, long : Double){
        Log.i(TAG,"setMapAndSettingsFragment")
        val bundle = Bundle()
        bundle.putDouble("lat", lat)
        bundle.putDouble("long", long)
        bundle.putString("url", currentLocationBaseUrl)
        map.arguments = bundle
        fm!!.beginTransaction()
            .add(R.id.nav_host_fragment, map, "map")
            .hide(map)
            .commit()

        fm.beginTransaction()
            .add(R.id.nav_host_fragment, explore, "explore")
            .hide(explore)
            .commit()
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            Log.i(TAG, "mOnNavigationItemSelectedListener")
            when (item.itemId) {
                R.id.hjemskjerm -> {
                    fm!!.beginTransaction().hide(active).show(home).commit()
                    active = home
                    return@OnNavigationItemSelectedListener true
                }
                R.id.kart -> {
                    fm!!.beginTransaction().hide(active).show(map).commit()
                    active = map
                    return@OnNavigationItemSelectedListener true
                }
                R.id.utforsk -> {
                    fm!!.beginTransaction().hide(active).show(explore).commit()
                    active = explore
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }


    private fun checkPermissions(): Boolean {
        Log.i(TAG, "checkPermissions")
        return (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions(){
        Log.i(TAG, "requestPermissions")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionsResult ")
        if (requestCode == permissionID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                popupDialog.showDialog(this, "Tillat bruk av posisjon for å vise data for der du er nå")
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        Log.i(TAG, "isLocationEnabled")
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /* Run if location has never been recorded
     * Unlikely real life scenario, but will occur
     * when running the app for the first time on a new emulator
     */
    private fun requestNewLocationData() {
        Log.i(TAG, "requestNewLocationData")
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
        Log.i(TAG, "Looper")
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.i(TAG, "onlocationresult")
            var mLastLocation : Location = locationResult.lastLocation
            setHomeFragments(mLastLocation.latitude, mLastLocation.longitude)
            Log.i(TAG, "home set from callback")
            setMapAndSearchFragment(mLastLocation.latitude, mLastLocation.longitude)
            Log.i(TAG, "map search set from callback")
        }
    }

    private fun getLastLocation(){
        Log.i(TAG, "getLastLocation")
        if(checkPermissions()){
            if(isLocationEnabled()){
                /*
                 * Attempt to get last recorded location by device
                 * In the unlikely case that a location has never been recorded,
                 * get new location data with the requestNewLocationData() function
                 */
                mFusedLocationClient.lastLocation.addOnCompleteListener(this){task ->
                    val location = task.result
                    if(location == null){
                        Log.i(TAG, "Location == null")
                        requestNewLocationData()
                    }else{
                        setHomeFragments(location.latitude, location.longitude)
                        setMapAndSearchFragment(location.latitude, location.longitude)
                    }
                }
            }else{
                Log.i(TAG, "Location not granted")
                popupDialog.showDialog(this, "Skru på stedtjenester for å vise data for nåværende posisjon")
            }
        }else{
            requestPermissions()
        }
    }

    override fun dialogDismissed() {
        Log.i(TAG, "dialogDismissed")
        setHomeFragments(defaultLatitude, defaultLongitude)
        setMapAndSearchFragment(defaultLatitude, defaultLongitude)
    }
}