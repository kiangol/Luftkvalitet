
package com.example.team31.ObjectClasses

import android.util.Log
import com.example.team31.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.io.Serializable
import kotlin.math.roundToInt

class Station (val station : String?, val lat : Double, val long : Double, val area : String?) : Serializable {

    //TAG
    private val TAG = "Station"

    //Values of all components from a station
    private val components : MutableList<Component> = mutableListOf()

    //Image view
    private var img : Int = 0
    private lateinit var desc : String

    fun stationColor() : Float{
        Log.i(TAG, "stationMarkerColor")
        val highest = getComponentWithHighestValue()

        when {
            highest.high -> {
                img = R.drawable.high_icon
                desc = "Høy forurensing"
                return BitmapDescriptorFactory.HUE_RED
            }
            highest.moderate -> {
                img = R.drawable.moderate_icon
                desc = "Moderat forurensing"
                return BitmapDescriptorFactory.HUE_ORANGE
            }
            highest.little -> {
                img = R.drawable.good_icon
                desc = "Lite eller ingen forurensing"
                return BitmapDescriptorFactory.HUE_GREEN
            }

        }

        img = R.drawable.extreme_icon
        desc = "Veldig høy forurensing"
        return BitmapDescriptorFactory.HUE_VIOLET
    }

    /* To get the component with the
     * highest value which will determine
     * the description, img, and color of station
     * with green being little, orange being moderate,
     * red being high, and violate being being very high
     */
    private fun getComponentWithHighestValue() : Component{
        var com = components[0]
        for (component in components) {
            if(com.compareTo(component) == -1) com = component
        }
        return com
    }

    fun addComponent(com : Component){
        components.add(com)
    }

    /* Gathering all values and names
     * from components to a string
     */
    fun getAllComponentValues() : String{
        var info = ""
        components.forEach { com ->
            val roundedValue = (com.value!! * 100.0).roundToInt() / 100.0
            info += "${com.component} : $roundedValue µg/m³\n"
        }
        info = info.trimEnd()
        return info
    }

    fun getImage() : Int{
        return  img
    }

    fun getDesc() : String{
        return desc
    }

}