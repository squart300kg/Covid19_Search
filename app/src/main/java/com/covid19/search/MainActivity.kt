package com.covid19.search

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), MapView.OpenAPIKeyAuthenticationResultListener {

    private val TAG = "MainActivity"
    private val REQUEST_CODE_LOCATION: Int = 2
    private lateinit var mapView: MapView
    private val geocoder = Geocoder(this, Locale.getDefault())

    private lateinit var locationManager: LocationManager
    private var latitude: Double? = 37.537229
    private var longtitude: Double? = 127.005515
    private var currentLocation: String? = null
    private var address: String? = null
    private var distance: Double? = null

    fun Context.isGPSEnabled() = (getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
    fun Context.checkLocationPermission(): Boolean =
        this.checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        tedPermission()

        getLocation()


        mapView = MapView(this)
        mapView.setOpenAPIKeyAuthenticationResultListener(this)
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(requireNotNull(latitude), requireNotNull(longtitude)), true)
        map_view.addView(mapView)

        val marker = MapPOIItem()
        marker.itemName = "Default Marker"
        marker.tag = 0
        marker.mapPoint = MapPoint.mapPointWithGeoCoord(requireNotNull(latitude), requireNotNull(longtitude))
        marker.markerType = MapPOIItem.MarkerType.BluePin // 기본으로 제공하는 BluePin 마커 모양.

        marker.selectedMarkerType =
            MapPOIItem.MarkerType.RedPin // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker)

    }

    @SuppressLint("LongLogTag")
    fun getLocation() {

        if (application.isGPSEnabled() && application.checkLocationPermission()) {

            LocationServices.getFusedLocationProviderClient(application)
                ?.lastLocation
                ?.addOnSuccessListener { location: android.location.Location? ->
                    if (location != null)

                    latitude = location?.latitude
                    longtitude = location?.longitude

                    Log.i("$TAG-latitude : ", latitude.toString() )
                    Log.i("$TAG-longtitude : ", longtitude.toString() )

                    address = geocoder.getFromLocation(
                        location?.latitude?: (-1).toDouble(),
                        location?.longitude?: (-1).toDouble(),
                        1
                    )[0].getAddressLine(0)
                    distance = getDistance(location?.latitude!!, location?.longitude!!)

//                    if (distance!! <= 10000000.0) { //목적지까지의 거리가 10000000m이하일 시
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            application.startForegroundService(Intent(application, MyService::class.java))
//                        } else {
//                            application.startService(Intent(application, MyService::class.java))
//                        }
//
//                    }

                    Log.i("$TAG-latitude : ", latitude.toString() )
                    Log.i("$TAG-longtitude : ", longtitude.toString() )
                    Log.i("$TAG-address : ", address.toString())
                    Log.i("$TAG-롯데월드까지 거리 : ", distance.toString() )

                    Toast.makeText(application,
                        "주소 : ${address}\n " +
                                "경도 : ${latitude.toString()}\n " +
                                "위도 : ${longtitude.toString()}\n" +
                                "롯데월드까지 거리 : ${distance.toString() }",
                        Toast.LENGTH_LONG).show()
                }

        }
    }

    private fun getDistance(lat1: Double, lng1: Double, lat2: Double = 37.514056, lng2: Double = 127.105025): Double {
        val distance: Double

        val locationA = android.location.Location("point A")
        locationA.latitude = lat1
        locationA.longitude = lng1

        val locationB = android.location.Location("point B")
        locationB.latitude = lat2
        locationB.longitude = lng2
        distance = locationA.distanceTo(locationB).toDouble()

        return distance
    }

//    private fun getCurrentLoc() {
//        locationManager = (getSystemService(Context.LOCATION_SERVICE) as LocationManager?)!!
//        var userLocation: Location = getLatLng()
//        if (userLocation != null) {
//            latitude = userLocation.latitude
//            longtitude = userLocation.longitude
//            Log.i(TAG, "latitude : $latitude, longtitude : $longtitude")
//
//            var mGeocoder = Geocoder(this, Locale.KOREA)
//            var mResultList: List<Address>? = null
//            try {
//                mResultList = mGeocoder.getFromLocation(requireNotNull(latitude), requireNotNull(longtitude), 1)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//            if (mResultList != null) {
//                Log.d("CheckCurrentLocation", mResultList[0].getAddressLine(0))
//                currentLocation = mResultList[0].getAddressLine(0)
//                currentLocation = currentLocation
//            }
//        }
//    }
//
//    private fun getLatLng(): Location {
//        var currentLatLng: Location? = null
//
//        // 위치를 가져오기 위한 권한 검사
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
//            getLatLng()
//        } else {
//            val locationProvider = LocationManager.GPS_PROVIDER
//            currentLatLng = locationManager?.getLastKnownLocation(locationProvider)
//        }
//        return currentLatLng!!
//    }


    private fun tedPermission() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {}
            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                finish()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setRationaleMessage("서비스 사용을 위해서 몇가지 권한이 필요합니다.")
            .setDeniedMessage("[설정] > [권한] 에서 권한을 설정할 수 있습니다.")
            .setPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .check()
    }

    //	/////////////////////////////////////////////////////////////////////////////////////////////////
    // net.daum.mf.map.api.MapView.OpenAPIKeyAuthenticationResultListener
    override fun onDaumMapOpenAPIKeyAuthenticationResult(mapView: MapView?, resultCode: Int, resultMessage: String?) {
        Log.i(TAG, String.format("Open API Key Authentication Result : code=%d, message=%s", resultCode, resultMessage))
    }


}