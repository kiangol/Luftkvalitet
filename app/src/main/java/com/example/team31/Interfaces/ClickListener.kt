package com.example.team31.Interfaces

import android.view.View
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

interface ClickListener {
    fun itemClicked(view : View,  stationRecycler : RecyclerView, arrowButton : Button)
    fun itemClicked(view : View, expandableView : ConstraintLayout, cardview : CardView, arrowButton : Button)
}
