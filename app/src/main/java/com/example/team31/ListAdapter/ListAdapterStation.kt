package com.example.team31.ListAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.team31.Interfaces.ClickListener
import com.example.team31.ObjectClasses.Station
import com.example.team31.R


class ListAdapterStation(private val stations: MutableList<Station>): RecyclerView.Adapter<ListAdapterStation.ViewHolder>() {

    private val TAG = "ListAdapterStation"

    private var clickListener : ClickListener? = null

    inner class ViewHolder(view: View) :  RecyclerView.ViewHolder(view){
        private val arrowButton: Button = view.findViewById(R.id.station_button)
        private val expandableView : ConstraintLayout = view.findViewById(R.id.expandableView)
        private val cardView : CardView = view.findViewById(R.id.cardview)
        val value: TextView = view.findViewById(R.id.values)
        val description : TextView = view.findViewById(R.id.description)
        val icon : ImageView = view.findViewById(R.id.imageView)
        val station : TextView = view.findViewById(R.id.station)

        init {
            cardView.setOnClickListener { clickView ->
                clickListener!!.itemClicked(clickView, expandableView, cardView, arrowButton)
                /* When a station cardView is clicked then
                 * itemClicked is called to display the expandedView
                 */
            }
        }
    }

    fun setClickListener(listener: ClickListener){
        Log.i(TAG, "setClickListener")
        clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.i(TAG, "onCreateViewHolder")
        val v = LayoutInflater.from(parent.context).inflate(R.layout.station_element, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolder")

        /*Setting the values of the station cardView*/
        holder.value.text = stations[position].getAllComponentValues()
        holder.description.text = stations[position].getDesc()
        holder.icon.setImageResource(stations[position].getImage())
        holder.station.text = stations[position].station
    }

    override fun getItemCount() = stations.size
}