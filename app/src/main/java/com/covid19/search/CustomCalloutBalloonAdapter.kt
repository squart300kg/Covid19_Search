package com.covid19.search


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import org.json.JSONObject

class CustomCalloutBalloonAdapter(private val context: Context) : CalloutBalloonAdapter{

    private val mCalloutBalloon: View =
        LayoutInflater.from(context).inflate(R.layout.custom_callout_balloon, null)

    override fun getCalloutBalloon(poiItem: MapPOIItem): View {

        var jsonObject = poiItem.userObject as JSONObject

        (mCalloutBalloon.findViewById(R.id.balloon_title) as TextView).text = jsonObject.get("centerName").toString()
        (mCalloutBalloon.findViewById(R.id.balloon_content) as TextView).text = jsonObject.get("address").toString()

        return mCalloutBalloon
    }

    override fun getPressedCalloutBalloon(poiItem: MapPOIItem): View? {
        Log.i("getPressedCalloutBalloon", "onClick")


        return mCalloutBalloon
    }

}