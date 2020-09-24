package com.example.team31.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.team31.ObjectClasses.AirQualityApiModel
import com.example.team31.ObjectClasses.ConvertApiModelToStation
import com.example.team31.ObjectClasses.Station
import com.example.team31.R
import com.example.team31.ViewModels.AirQualityViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt


class HomeFragment : Fragment(){

    //TAG
    private val TAG = "HomeFragment"

    //Default radius
    private var radius = 20

    //ViewModels
    private lateinit var airQualityViewModel: AirQualityViewModel

    //Parameters
    private val param1 = "url"
    private val param2 = "currentLat"
    private val param3 = "currentLong"

    //Variables
    private lateinit var baseUrl : String
    private lateinit var currentLocationUrl : String
    private var currentLat : Double = 0.0
    private var currentLong : Double = 0.0
    private lateinit var stationList : List<AirQualityApiModel>

    //Views
    private lateinit var searchView: SearchView
    private lateinit var progressBar : ProgressBar

    //CardView Components
    private lateinit var cardview : CardView
    private lateinit var greeting : TextView
    private lateinit var currentCity : TextView
    private lateinit var currentPlaceText : TextView
    private lateinit var description : TextView
    private lateinit var stationValue : TextView
    private lateinit var distanceView : TextView
    private lateinit var icon : ImageView
    private lateinit var arrowButton : Button
    private lateinit var expandedCardView : ConstraintLayout
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var infoButton : ImageButton

    //Landscape
    private var landscape : Boolean = false

    //View inflater
    private lateinit var viewInflate : View

    //Frame layout
    private lateinit var frameLayout: FrameLayout

    //Variables for the status of the home page
    private var placeText : String = ""
    private var expanded : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate ")
        super.onCreate(savedInstanceState)
        /* get bundle parameters sent from MainActivity */
        val bundle = this.arguments
        if(bundle != null){
            baseUrl = bundle.getString(param1)!! //url for API
            currentLat = bundle.getDouble(param2) //current latitude
            currentLong = bundle.getDouble(param3) //current longitude
            currentLocationUrl = "$baseUrl/$currentLat/$currentLong"

            /*Initialize airQualityViewModel with AirQualityViewModel*/
            airQualityViewModel  = ViewModelProvider(this).get(AirQualityViewModel::class.java)
        }
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) landscape = true
        else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) landscape = false
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.i(TAG, "onCreateView")
        frameLayout = FrameLayout(requireActivity())
        viewInflate = inflater.inflate(R.layout.fragment_home, container, false)
        frameLayout.addView(viewInflate)
        initialize()

        /* Observing airQualityData for input data
        * and upon receiving value, nearestStation is called
        * with the list of AirQualityApiModels
        */
        airQualityViewModel.airQualityData.observe(viewLifecycleOwner, Observer {nearestStation->
            stationList = nearestStation
            nearestStation(nearestStation)
        })
        makeApiRequestForPosition()
        return frameLayout
    }

    private fun initialize(){
        Log.i(TAG, "initialize")
        cardview = viewInflate.findViewById(R.id.cardview)
        progressBar = viewInflate.findViewById(R.id.progressbar)
        searchView = viewInflate.findViewById(R.id.searchView)
        greeting = viewInflate.findViewById(R.id.greeting)
        currentCity = viewInflate.findViewById(R.id.currentCity)
        currentPlaceText = viewInflate.findViewById(R.id.current_loc_text)
        if(placeText == "") currentPlaceText.text = getString(R.string.current_location)
        else currentPlaceText.text = placeText

        description = viewInflate.findViewById(R.id.description)
        stationValue = viewInflate.findViewById(R.id.values)
        distanceView = viewInflate.findViewById(R.id.distanceText)
        arrowButton = viewInflate.findViewById(R.id.station_button)
        icon = viewInflate.findViewById(R.id.imageView)
        expandedCardView = viewInflate.findViewById(R.id.expandedCardView)
        if(expanded) expandedCardView.visibility = View.VISIBLE

        constraintLayout = viewInflate.findViewById(R.id.layout)
        infoButton = viewInflate.findViewById(R.id.infobutton)

        if(expandedCardView.visibility == View.VISIBLE){
            Log.i(TAG, "expandedCardView.visibility == View.VISIBLE")
            if(!landscape) arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
            else arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_left_black_24dp)
        }

        progressBar.visibility = View.VISIBLE
        cardview.visibility = View.INVISIBLE

        landscape = Configuration.ORIENTATION_LANDSCAPE == requireActivity().resources.configuration.orientation
        /* Get current time
         * Greeting message on Home is set depending on time of the day
         */
        val dayNow = Calendar.getInstance()
        when (dayNow.get(Calendar.HOUR_OF_DAY)) {
            in 6..9 -> {
                greeting.text = getString(R.string.good_morning)
            }
            in 9..12 -> {
                greeting.text = getString(R.string.good_morning_2)
            }
            in 12..18 -> {
                greeting.text = getString(R.string.good_afternoon)
            }
            in 18..20 -> {
                greeting.text = getString(R.string.good_evening)
            }
            in 20..23 -> {
                greeting.text = getString(R.string.good_night)
            }
            in 0..6 -> {
                greeting.text = getString(R.string.hei)
            }
        }

        setCurrentCity()

        val animationDrawable =
            constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(4000)
        animationDrawable.setExitFadeDuration(8000)
        animationDrawable.start()

        cardview.setOnClickListener {
            viewExpandedCardView(arrowButton, expandedCardView)
        }

        infoButton.setOnClickListener {
            val uri: Uri =
                Uri.parse("https://www.fhi.no/nettpub/luftkvalitet/svevestov/svevestov/")

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        //Text listener for the SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            // Dont need real time listener, disabled
            override fun onQueryTextChange(newText: String): Boolean  = false

            // Runs when user presses search after entering text
            @SuppressLint("SetTextI18n")
            override fun onQueryTextSubmit(query: String): Boolean {
                val latLng : LatLng
                try {
                    // Use getLocationFromAddress fun to return LatLng of search
                    latLng = getLocationFromAddress(query)!!
                    currentLat = latLng.latitude
                    currentLong = latLng.longitude

                    progressBar.visibility = View.VISIBLE
                    cardview.visibility = View.INVISIBLE

                    //After LatLng is found, make API request
                    makeApiRequestForPosition()
                    Log.i(TAG, latLng.toString())

                    //Catch when user enters a non-existent or invalid query
                } catch (e : NullPointerException){
                    Toast.makeText(context, "Ingen resultater", Toast.LENGTH_SHORT).show()
                    distanceView.text = "Ingen treff på $query"
                }
                return false
            }
        })
    }

    private fun getEmoji(unicode: Int): String {
        return String(Character.toChars(unicode))
    }


    private fun getLocationFromAddress(strAddress: String?): LatLng? {
        Log.i(TAG, "getLocationFromAddress")
        if(strAddress == null) return null
        /* Easter egg :) */
        else if(strAddress == "hell") {
            greeting.text = "${getEmoji(0x1F525)} ${getEmoji(0x1F621)} ${getEmoji(0x1F525)}"
            constraintLayout.setBackgroundResource(R.drawable.hell_bg)
            cardview.setBackgroundColor(Color.RED)
        }
        /* Show progressbar while searching
         * and hide cardview, will later be set visible
         * by setCurrentCity() after search result
         */
        val coder = Geocoder(context)
        val address: List<Address>?
        var latLng: LatLng? = null
        try {
            /* Use Geocoder to find locations based on search query
             * Works with addresses, cities, and even names such as "uio"
             * or "frognerparken"
             */
            address = coder.getFromLocationName(strAddress, 1)

            if (address.isEmpty()) {
                Log.i(TAG, "Fant ingen steder")
                return null
            }

            /* The most relevant result will be the first element
             * in the list of addressses (address)
             * For more functionalit in the future, this can be expanded to
             * show multiple results
             */
            Log.i(TAG, address[0].toString())
            val location = address[0]
            Log.i(TAG, "COUNTRY: ${location.countryName}, ${location.countryCode}")
            // Don't allow search outside of Norway
            if(!(location.countryCode.contains("NO") || location.countryCode.contains("SJ"))) {
                Toast.makeText(context, "Ikke gyldig utenfor Norge", Toast.LENGTH_SHORT).show()
                return null
            }

            setCurrentCity()
            currentPlaceText.text = getString(R.string.search_result_str)
            latLng = LatLng(location.latitude, location.longitude)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return latLng
    }

    private fun makeApiRequestForPosition(){
        Log.i(TAG, "makeApiRequestForPosition")
        //Make asynchronous API request
        CoroutineScope(Main).launch {
            try {
                /* Make API request with retrofit
                 * using given latitude and longitude within 20km radius,
                 * which is the maximum for the API from NILU
                 */
                airQualityViewModel.buildRetrofit("$baseUrl/$currentLat/$currentLong/")

                /* Making API request for the nearest
                 * station within the radius of 2okm
                 */
                airQualityViewModel.currentPositionDataNearestStation(radius.toString())
            } catch (e: Exception) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                Log.e(TAG, e.toString())
            }
        }
    }


    /* nearestStation will find the nearest station given a location,
     * convert an API model to a station object class, get station information
     * and set their colors and describtions from the stationColor() function
     */
    @SuppressLint("SetTextI18n")
    private fun nearestStation(nearest : List<AirQualityApiModel>) {
        Log.i(TAG, "nearestStation")
        setCurrentCity()
        if(nearest.isEmpty()) {
            displayHomeInformation()
            return
        }
        val stationsList = ConvertApiModelToStation(
            nearest
        ).convert()
        val currentStationInfo = stationsList[stationsList.size - 1]

        currentStationInfo.stationColor()

        val results = FloatArray(1)
        Location.distanceBetween(currentStationInfo.lat, currentStationInfo.long, currentLat, currentLong, results)

        val distanceBetween = results[0]

        distanceView.text = "Data fra ${currentStationInfo.station} (${(distanceBetween/100.0).roundToInt()/10.0} km unna)"

        displayHomeInformation(currentStationInfo)
    }

    private fun setCurrentCity() {
        Log.i(TAG, "setCurrentCity")

        /* Use Geocoder
         * to find name of current city from latitude and longitude
         * used both for finding name of current location, and also
         * from search results
         */
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(currentLat, currentLong, 1)

        /* Geocoder will return the full address including street name
         * we strip out unnecessary information, leaving with only name of the city
         */
        val currentAddress = addresses[0].getAddressLine(0)
        val addressArray = currentAddress.split(",").toTypedArray()
        var currentPlace = addressArray[0]

        /* Try to get city name from address
         * Throws exception if location is in a place without full address
         * and will not remove anything
        */
        try {
            currentPlace = addressArray[addressArray.size-2]
        } catch (exception: ArrayIndexOutOfBoundsException) {
            Log.i(TAG, "addressArray index exception")
        }

        // Remove any numbers (postal code) using regex
        currentPlace = currentPlace.replace("[0-9]".toRegex(), "")

        // Redundant space will be left in address string sometimes, remove
        if (currentPlace.startsWith(" ")) {
            currentPlace = currentPlace.replace(" ","")
        }
        currentCity.text = currentPlace
    }

    /* This function is called
     * when no stations are nearby
     */
    private fun displayHomeInformation(){
        Log.i(TAG, "displayHomeInformation (empty list)")
        description.text = getString(R.string.no_stations_near)
        icon.setImageResource(R.drawable.high_icon)
        arrowButton.visibility = View.INVISIBLE
        distanceView.text = ""
        stationValue.text = getString(R.string.within_20)
        progressBar.visibility = View.INVISIBLE
        cardview.visibility = View.VISIBLE
        if(expandedCardView.visibility == View.VISIBLE) expandedCardView.visibility = View.GONE
        if(infoButton.visibility == View.VISIBLE) infoButton.visibility = View.GONE
    }

    /* Called when a station is found
     * Setting information on the home screen,
     * including information from the station and
     * setting necessary views as visible
     */
    private fun displayHomeInformation(currentStationInfo : Station){
        Log.i(TAG, "displayHomeInformation")
        stationValue.text = currentStationInfo.getAllComponentValues()
        icon.setImageResource(currentStationInfo.getImage())
        description.text = currentStationInfo.getDesc()
        if(arrowButton.visibility == View.INVISIBLE){
            arrowButton.visibility = View.VISIBLE
            currentPlaceText.visibility = View.VISIBLE
            infoButton.visibility = View.VISIBLE
            if(currentPlaceText.text == "" || currentPlaceText.text == getString(R.string.within_20) || currentPlaceText.text != getString(R.string.search_result_str) ) {
                currentPlaceText.text = getString(R.string.current_location)
            } else if (currentPlaceText.text == getString(R.string.current_location)) {
                currentPlaceText.text = getString(R.string.search_result_str)
            }
        }
        // ProgressBar is also set invisible here, last function to be called
        progressBar.visibility = View.INVISIBLE
        cardview.visibility = View.VISIBLE
    }


    /* When a cardView is clicked
     * then the expandedCardView is either
     * displayed or hidden based on its current
     * status*/
    private fun viewExpandedCardView(arrowButton : Button, expandedCardView : ConstraintLayout){
        Log.i(TAG, "viewExpandedCardView")
        if(expandedCardView.visibility == View.GONE){
            /* The expandedCardView was hidden
             * but will be visible now and the
             * background on arrowButton is set to
             * either left or up based on the current orientation*/
            TransitionManager.beginDelayedTransition(expandedCardView, AutoTransition())
            expandedCardView.visibility = View.VISIBLE
            if(!landscape)arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
            else arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_left_black_24dp)
        }else{
            /* The expandedCardView was visible
             * but will be hidden now and the
             * background on arrowButton is set to
             * either right or down based on the current orientation*/
            TransitionManager.beginDelayedTransition(expandedCardView, AutoTransition())
            expandedCardView.visibility = View.GONE
            if(!landscape) arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            else arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
        }
    }

    @SuppressLint("InflateParams")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i(TAG, "onConfigurationChanged")
        /* When the configuration is changed then the
         * all the view are removed from the
         * current frameLayout */
        frameLayout.removeAllViews()

        //Setting landscape variable with current orientation
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) landscape = true
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) landscape = false

        val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewInflate = inflater.inflate(R.layout.fragment_home, null)

        placeText = currentPlaceText.text as String
        expanded = expandedCardView.visibility == View.VISIBLE

        /* Adding View to the empty frameLayout
         * and initializing the same views again
         * but with layout fragment_home (landscape) */
        frameLayout.addView(viewInflate)
        initialize()
        nearestStation(stationList)
    }
}

