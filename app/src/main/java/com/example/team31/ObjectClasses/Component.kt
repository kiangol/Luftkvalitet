package com.example.team31.ObjectClasses

import android.util.Log
import java.io.Serializable

class Component (val component: String?, val value : Float?) : Serializable, Comparable<Component>{

    //TAG
    private val TAG = "Component"

    //Value Classes
    var little : Boolean = false
    var moderate : Boolean = false
    var high: Boolean = false
    var veryHigh : Boolean = false

    /* Setting the boolean values of a component
     * based on its value */
    fun setValueClass(l : Float, m : Float, h : Float){
        Log.i(TAG, "setValueClass")
        if(value!! < l){
            little = true
        }else if (value >= l && value < m){
            moderate = true
        }else if (value >= m && value < h){
            high = true
        }else{
            veryHigh = true
        }
    }

    /* To compare all the values
     * from all components in one station
     * to find the highest value.
     * eg. if one of the components in a station has
     * moderate value while the rest have little
     * then the station will have the value moderate */
    override fun compareTo(other: Component): Int {
        Log.i(TAG, "compareTo")
        if(veryHigh && !other.veryHigh) return 1
        if(!veryHigh && other.veryHigh) return -1

        if(high && !other.high) return 1
        if(!high && other.high) return -1

        if(moderate && !other.moderate) return 1
        if(!moderate && other.moderate) return -1

        if(little && !other.little) return 1
        if(!little && other.little) return -1

        return 0
    }
}