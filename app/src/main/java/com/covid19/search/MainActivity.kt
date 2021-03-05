package com.covid19.search

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import net.daum.mf.map.api.MapLayout
import net.daum.mf.map.api.MapView


class MainActivity : AppCompatActivity(), MapView.OpenAPIKeyAuthenticationResultListener {

    private val TAG = "MainActivity"
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        mapView = MapView(this)
        mapView.setOpenAPIKeyAuthenticationResultListener(this)

        map_view.addView(mapView)
    }

    //	/////////////////////////////////////////////////////////////////////////////////////////////////
    // net.daum.mf.map.api.MapView.OpenAPIKeyAuthenticationResultListener
    override fun onDaumMapOpenAPIKeyAuthenticationResult(mapView: MapView?, resultCode: Int, resultMessage: String?) {
        Log.i(TAG, String.format("Open API Key Authentication Result : code=%d, message=%s", resultCode, resultMessage))
    }


}