package com.covid19.search

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class MainActivity : AppCompatActivity(), MapView.OpenAPIKeyAuthenticationResultListener, MapView.CurrentLocationEventListener {

    private val TAG = "MainActivity"
    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS =
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private lateinit var mapView: MapView

    private lateinit var locationManager: LocationManager
    private var latitude: Double? = null
    private var longtitude: Double? = null
    private var currentLocation: String? = null
    private var address: String? = null
    private var distance: Double? = null

    // LG Gram 키해시 : /2q855oUoLt8Gt5SURw/Zeo4H8M=
    // Mac 키해시 : S4hP67qeLpxOpFdOvSnjeDaJawc=

    // TODO 1. 안드로이드 최저버전 26으로 설정함 좀 더 낮출 수 없는지 알아볼 것
    // TODO 2. 프로가드 적용할 것

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting()
        } else {
            checkRunTimePermission()
        }

        mapView = MapView(this)
        mapView.setOpenAPIKeyAuthenticationResultListener(this)
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        mapView.setZoomLevel(11, true)
//        mapView.setCalloutBalloonAdapter(CustomCalloutBalloonAdapter(this))
        map_view.addView(mapView)
        //그렇다면 이제 다음에 무엇을 해야하지? 일단 현재 위치가 지도에 뜨는것은 확인했다. 다음에 할 것은
        // 현재 나의 위치에 따라서 지도 중심점이 움직이도록 설정하는 것이다. - 성공!

        // 나의 현재 위치에 따라서 중심점이 변하도록 구현 성공했다. 그 다음 해야할 것은?
        // 주변에 복수개로 마커가 찍힐 수 있도록 구현하자!
        // 단순히 마커 객체를 여러개를 만들면 됨- 성공!

        // 그 다음엔 뭘하지? RESTFULAPI를 연동해서 값을 받아오기! - 성공
        // 그 이전엔 어떻게 해야할까? 우선, 스플레시 이미지를 띄울때부터 rest api통신을 시작한다. 그러므로 스플래시 이미지를 먼저 넣어야 할것이다 - 성공!

        // api통신에 성공했다. 받아온 녀석들의 위도와 경도를 이용해 마커를 찍자
        // 그렇다면 splash에서 받아온 값을 메인액티비티로 가져와야 하는데.... 어떻게 가져오지?
        // 1. sharedPreference를 이용해 가져온다
        //    하지만 단점이 있다. 데이터를 추가적으로 넣을 시, 한번에 다 가져오고 갱신해야만 한다.
        //    하지만 아니면 키값에 인덱싱을 줘서 한다면?
        // 2. sqllite를 이용해 가져온다.

        // 이제 처음 시작할 때 zoom out한 상태로 시작해야한다.

        val prefs: SharedPreferences = getSharedPreferences("COVID19INFO_LIST", Context.MODE_PRIVATE)
        val pref_result = prefs.getString("covid19info_list", null)

        Log.i(TAG + "pref", pref_result.toString())

        try {

            var jsonArray = JSONArray(pref_result)
//            var marker = arrayListOf<MapPOIItem>(jsonArray.length())
            var markerList = ArrayList<MapPOIItem>()
//            var marker[3] = MapPOIItem()
            for (i in 0 until jsonArray.length()) {
                var obj = jsonArray.getJSONObject(i)
                Log.i("jsonTest[$i]", obj.get("centerName").toString())
                Log.i("jsonTest[$i]", obj.get("address").toString())
                Log.i("jsonTest[$i]", obj.get("centerType").toString())
                Log.i("jsonTest[$i]", obj.get("facilityName").toString())
                Log.i("jsonTest[$i]", obj.get("lat").toString())
                Log.i("jsonTest[$i]", obj.get("lng").toString())
                Log.i("jsonTest[$i]", obj.get("org").toString())
                Log.i("jsonTest[$i]", obj.get("sido").toString())
                Log.i("jsonTest[$i]", obj.get("sigungu").toString())
//                Log.i("jsonTest[$i]", obj.get("zipCode").toString())

                var marker = MapPOIItem()
                marker.itemName = obj.get("centerName").toString()
                marker.tag = 0 // TODO 추후 무슨뜻인지 알아낼것
                marker.mapPoint = MapPoint.mapPointWithGeoCoord(obj.get("lng").toString().toDouble(), obj.get("lat").toString().toDouble())
                marker.markerType = MapPOIItem.MarkerType.BluePin
                marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
                markerList.add(marker)

                mapView.addPOIItem(markerList[i])
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }




    }

    // 다음과 같이 자료구조를 재정리하는 메소드
    // string(HashMap(), HashMap(), HashMap() ... ) => List(HashMap(), HashMap(), HashMap(), ...)
    private fun stringToWords(s: String) = s.trim().splitToSequence('[')
        .filter() {it.isNotEmpty() }
        .toList()

    private fun checkRunTimePermission() {
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    REQUIRED_PERMISSIONS.get(0)
                )
            ) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(this@MainActivity, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG)
                    .show()
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    private fun showDialogForLocationServiceSetting() {
        var builder = AlertDialog.Builder(this)
        builder.setTitle("위치 서비스 활성화")
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n위치 설정을 수정하시겠습니까?")
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { dialog, id ->
            val callGPSSettingIntent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        }
        builder.setNegativeButton("취소") { dialog, id -> dialog.cancel() }
        builder.create().show()

    }

    private fun checkLocationServicesStatus(): Boolean {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->                 //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음")
                        checkRunTimePermission()
                        return
                    }
                }
        }
    }

//    private fun tedPermission() {
//        val permissionListener = object : PermissionListener {
//            override fun onPermissionGranted() {}
//            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
//                finish()
//            }
//        }
//
//        TedPermission.with(this)
//            .setPermissionListener(permissionListener)
//            .setRationaleMessage("서비스 사용을 위해서 몇가지 권한이 필요합니다.")
//            .setDeniedMessage("[설정] > [권한] 에서 권한을 설정할 수 있습니다.")
//            .setPermissions(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//            .check()
//    }

    //	/////////////////////////////////////////////////////////////////////////////////////////////////
    // net.daum.mf.map.api.MapView.OpenAPIKeyAuthenticationResultListener
    override fun onDaumMapOpenAPIKeyAuthenticationResult(mapView: MapView?, resultCode: Int, resultMessage: String?) {
        Log.i(TAG, String.format("Open API Key Authentication Result : code=%d, message=%s", resultCode, resultMessage))
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
        TODO("Not yet implemented")
    }

    override fun onCurrentLocationUpdate(mapView: MapView?, currentLocation: MapPoint?, float: Float) {
//        Log.i(TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters))
        val mapPointGeo = currentLocation!!.mapPointGeoCoord
        Log.i(TAG + " 현재위치", "위도 : $mapPointGeo.latitude, 경도 : $mapPointGeo.longitude")
        Toast.makeText(this@MainActivity, "위도 : $mapPointGeo.latitude, 경도 : $mapPointGeo.longitude", Toast.LENGTH_LONG)
            .show()
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
        TODO("Not yet implemented")
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
        TODO("Not yet implemented")
    }

    override fun onRequestPermissionsResult(
        permsRequestCode: Int,
        permissions: Array<String?>,
        grandResults: IntArray
    ) {
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.size == REQUIRED_PERMISSIONS.size) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            var check_result = true

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (result in grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }
            if (check_result) {
                Log.d("@@@", "start")
                //위치 값을 가져올 수 있음
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있다
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[0]
                    )
                ) {
                    Toast.makeText(
                        this@MainActivity,
                        "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}