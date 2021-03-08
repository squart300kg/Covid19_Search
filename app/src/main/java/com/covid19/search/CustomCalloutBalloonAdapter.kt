package com.covid19.search


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem


class CustomCalloutBalloonAdapter(private val context: Context) : CalloutBalloonAdapter {

    private val mCalloutBalloon: View

    override fun getCalloutBalloon(poiItem: MapPOIItem): View {
//        (mCalloutBalloon.findViewById(R.id.badge) as ImageView).setImageResource(R.drawable.ic_launcher)
//        (mCalloutBalloon.findViewById(R.id.title) as TextView).text = poiItem.itemName
//        (mCalloutBalloon.findViewById(R.id.desc) as TextView).text = "Custom CalloutBalloon"
        return mCalloutBalloon
    }

    override fun getPressedCalloutBalloon(poiItem: MapPOIItem): View? {
        return null
    }

    init {
        mCalloutBalloon = LayoutInflater.from(context).inflate(R.layout.custom_callout_balloon, null)
    }
}