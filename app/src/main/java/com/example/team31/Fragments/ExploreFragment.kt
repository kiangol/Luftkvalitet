package com.example.team31.Fragments

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.team31.Interfaces.ClickListener
import com.example.team31.ListAdapter.ListAdapterArea
import com.example.team31.ObjectClasses.Area
import com.example.team31.ObjectClasses.Station
import com.example.team31.R
import com.example.team31.ViewModels.AirQualityViewModel
import com.example.team31.ViewModels.StationsViewModel


class ExploreFragment : Fragment() ,
    ClickListener{

    private val TAG = "SearchFragment"

    //View Models
    private lateinit var airQualityViewModel: AirQualityViewModel
    private lateinit var stationsViewModel: StationsViewModel

    //Recycler View
    private lateinit var recyclerView : RecyclerView

    //List Adapter
    private lateinit var areaListAdapter : ListAdapterArea

    //List of Areas
    private lateinit var areas : MutableList<Area>

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        airQualityViewModel = ViewModelProvider(this).get(AirQualityViewModel::class.java)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)

        //CardView animation when expanding
        val animationDrawable =
            recyclerView.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(4000)
        animationDrawable.setExitFadeDuration(8000)
        animationDrawable.start()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated")
        activity?.let {
            stationsViewModel = ViewModelProvider(it).get(StationsViewModel::class.java) //Initialize stationsViewModel with StationsViewModel to list of Areas
            observeViewModel(stationsViewModel)
        }
    }

    private fun observeViewModel(viewModel : StationsViewModel){
        /* Observes the stationsList in StationViewModel
         * for values inserted and displays a recyclerView with ListAdapterArea
         */
        Log.i(TAG, "observeViewModel")
        viewModel.getStationsList().observe(viewLifecycleOwner, Observer { stations ->
            val areas = getAreasFromStations(stations)
            areaListAdapter =
                ListAdapterArea(areas)
            /* Setting the clickListener variable on
             * ListAdapterArea and ListAdapterStation to change the display
             *  the stations cardView and the expandedCardView
             */
            areaListAdapter.setClickListener(this)
            recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            recyclerView.adapter = areaListAdapter
        })
    }

    private fun getAreasFromStations(stations : MutableList<Station>) : MutableList<Area>{
        Log.i(TAG, "getAreasFromStations")
        val stationMap = HashMap<String?, Area>()
        areas = mutableListOf()
        /* Get all areas from all stations and place them in mutableList
         * These are used for arranging and grouping stations
         */
        stations.forEach { station ->
            if(!stationMap.containsKey(station.area)){
                val area = Area(station.area)
                areas.add(area)
                area.addStation(station)
                stationMap[station.area] = area
            }else {
                stationMap[station.area]!!.addStation(station)
            }
        }
        areas = areas.sortedBy { it.area } as MutableList<Area>
        return areas
    }

    override fun itemClicked(view: View, stationRecycler : RecyclerView, arrowButton: Button) {
        /* When an area cardView is clicked
         * the stationRecycler is either
         * displayed or hidden based on
         * its current status
         */
        Log.i(TAG, "itemClicked area")
            /* If stationRecycler is not visible
             * then it is displayed and
             * background on arrowButton is changed to up
             */
        if(stationRecycler.visibility == View.GONE){
            TransitionManager.beginDelayedTransition(stationRecycler, AutoTransition())
            stationRecycler.visibility = View.VISIBLE
            arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
            /* If stationRecycler is visible
             * then it is displayed and
             * background on arrowButton is changed to down
             */
        }else{
            TransitionManager.beginDelayedTransition(stationRecycler, AutoTransition())
            stationRecycler.visibility = View.GONE
            arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
        }
    }

    /* When a station cardView is clicked,
     * the expandableView is either displayed
     * or hidden based on its current status
     */
    override fun itemClicked(view: View, expandableView: ConstraintLayout, cardview: CardView, arrowButton: Button) {
        Log.i(TAG, "itemClicked station")
            /*If the expandedView is hidden then it is displayed and the
             * background on the arrowButton on the station cardView is changed to up
             */
        if(expandableView.visibility == View.GONE){
            TransitionManager.beginDelayedTransition(cardview, AutoTransition())
            expandableView.visibility = View.VISIBLE
            arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
            /* If it is visible then the expandedView is hidden while the background on the arrowButton
             * is changed to down
             */
        }else{
            TransitionManager.beginDelayedTransition(cardview, AutoTransition())
            expandableView.visibility = View.GONE
            arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
        }
    }
}
