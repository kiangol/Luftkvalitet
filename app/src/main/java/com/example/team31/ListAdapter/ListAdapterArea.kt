package com.example.team31.ListAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.team31.Interfaces.ClickListener
import com.example.team31.ObjectClasses.Area
import com.example.team31.R


class ListAdapterArea(private val areas: MutableList<Area>): RecyclerView.Adapter<ListAdapterArea.ViewHolder>() {

    private val TAG = "ListAdapterArea"

    private var clickListener : ClickListener? = null

    private val viewPool = RecyclerView.RecycledViewPool()

    inner class ViewHolder(view: View) :  RecyclerView.ViewHolder(view){
        private var arrowButton : Button = view.findViewById(R.id.area_button)
        private val areaCardView : CardView = view.findViewById(R.id.areaCardView)
        var area: TextView = view.findViewById(R.id.area)
        var stationRecycler : RecyclerView = view.findViewById(R.id.stationRecycler)

        init {
            areaCardView.setOnClickListener { clickView ->
                clickListener!!.itemClicked(clickView, stationRecycler, arrowButton) /*When an area cardView is clicked
                then itemClicked is called*/
            }
        }
    }

    fun setClickListener(listener: ClickListener){ //Initializing the clickListener variable from ExploreFragment
        Log.i(TAG, "setClickListener")
        clickListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.i(TAG, "onCreateViewHolder")
        val v = LayoutInflater.from(parent.context).inflate(R.layout.area_element, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolder ${areas[position].area} : ${areas[position].getStations().size}")
        val area = areas[position]
        holder.area.text = area.area

        val childLayoutManager = LinearLayoutManager(holder.stationRecycler.context, RecyclerView.VERTICAL, false)
        childLayoutManager.initialPrefetchItemCount = area.getStations().size

        /*Creating the stations listAdapter with the stations in each area*/
        holder.stationRecycler.apply {
            layoutManager = childLayoutManager
            val listAdapter =
                ListAdapterStation(area.getStations())
            listAdapter.setClickListener(clickListener!!)
            adapter = listAdapter
            setRecycledViewPool(viewPool)
        }
    }

    override fun getItemCount() = areas.size
}
