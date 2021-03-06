package com.covid19.search

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class MainActivity : AppCompatActivity(),
    MapView.OpenAPIKeyAuthenticationResultListener,
    MapView.CurrentLocationEventListener,
    MapView.POIItemEventListener {

    private val TAG = "MainActivity"
    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val PERMISSIONS_REQUEST_CODE = 100

    private val COVID19INFO_LIST = "0"
    private val ALL_AD_COUNT = "1"
    private var MAX_AD_COUNT = 5

    private val REQUIRED_PERMISSIONS = arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION)
    private val ALL_AD_BANNER_TEST = "ca-app-pub-6562154197787185/7416264862"
    private val ALL_AD_BANNER = "ca-app-pub-3940256099942544/1033173712"

    private lateinit var mapView: MapView
    private var mInterstitialAd: InterstitialAd? = null

    private lateinit var locationManager: LocationManager
    private var latitude: Double? = null
    private var longtitude: Double? = null

    // ?????? ???????????? ????????? : S4hP67qeLpxOpFdOvSnjeDaJawc=
    //

    // TODO 1. ??????????????? ???????????? 26?????? ????????? ??? ??? ?????? ??? ????????? ????????? ??? - ??????
    // TODO 3. ?????? ?????????, ??????????????? ?????? ?????????????????? ?????? - ??????
    // TODO 4. ???????????? ??????????????? 0.1????????? API????????? ?????? ??????????????? ?????? ????????? ?????? ????????? - ?????? ??? - ??????
    // TODO 5. ??????????????? ???????????? - ??????
    // TODO 6. ???????????? ????????? ??? - ??????
    // TODO 9. ???????????? - ??????

    // TODO 2. ???????????? ????????? ??? - ??????
    // TODO 7. ??????????????? ?????? ??? - ??????
    // TODO 8. ????????????????????? ?????? ??? - ??????
    // TODO 9. ????????? ????????? ??? ??? ????????? ???????????? - ??????
    // TODO 10. ???????????? ??????! - ??????
    // TODO 11. ????????? ????????? ?????? ??????, 5?????? ??? ?????? ????????? ????????????. sharedpreference??? ?????? - ??????


    // TODO ????????????!!!!!!
    // TODO 1. JKS?????? ?????? ????????????,
    // TODO 2. ???????????? ?????? ????????????
    // TODO 3. ??????ID?????????(??????), - ??????
    // TODO 4. ??????ID?????????(??????), - ??????
    // TODO 5. covid19 API??? ????????? - ??????
    // TODO 6. splash_activity?????? ?????? ????????? ????????? api????????? ?????? ?????????????????? ???????????????? ????????? ???
    // TODO 7. jks????????? apk????????? ??????????????? ??? - ??????
    // TODO 8. ????????????????????? ????????? ???
    // ??????????????? ?????? ??? : ????????? ???????????? ???????????? apk????????? ?????? ?????? ??? ??????????
    // ??????1. ?????? release?????? api?????? ?????? apk ?????? : ?????? ??????
    // ??????2. test api?????? ?????? apk??? ??????????????????.   : ?????? ???
    // ?????? : ????????? jks????????? ????????? apk??? ?????? ????????? ??? ??????

    // TODO ?????? ?????? ???.
    // TODO 1. api?????? ???????????? ?????? ?????? ????????? ?????? ??? ?????? ??????
    // TODO 2.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (!checkLocationServicesStatus()) {
            Log.i(TAG, "checkLocationServicesStatus - false")
            showDialogForLocationServiceSetting()

        } else {

            Log.i(TAG, "checkLocationServicesStatus - true")
            checkRunTimePermission()

        }

        getKeyHash()

    }

    private fun getKeyHash() {
        try {
            val info: PackageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("???????????? :", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    private fun checkRunTimePermission() {

        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSIONS[0]
        )

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. ?????? ???????????? ????????? ????????? ?????? ?????? ????????? ??? ??????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)
            loadingMapViewAndADView()

        } else {

            //3. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????.
            checkTedPermission()

        }
    }

    private fun checkTedPermission() { //????????? ??????
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_SHORT).show()

                loadingMapViewAndADView()

            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                Toast.makeText(this@MainActivity, "Permission Denied\n$deniedPermissions", Toast.LENGTH_SHORT).show()

                checkTedPermission()

            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setDeniedMessage("????????? ??????????????? ?????? ?????? ???????????? ??? ????????????.\n\n[??????] > [??????]??? ??????????????? ????????? ????????? ?????????")
            .setPermissions(*REQUIRED_PERMISSIONS)
            .check()

    }

    private fun loadingMapViewAndADView() {

        // ????????? ?????????.
        mapView = MapView(this)
        mapView.setOpenAPIKeyAuthenticationResultListener(this)
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        mapView.setZoomLevel(11, true)
        mapView.setCalloutBalloonAdapter(CustomCalloutBalloonAdapter(this))
        mapView.setCurrentLocationEventListener(this)
        mapView.setPOIItemEventListener(this)
        map_view.addView(mapView)

        // ?????? ?????????????????? ????????? ???????????? ????????? ????????????
        val prefs: SharedPreferences = getSharedPreferences(COVID19INFO_LIST, Context.MODE_PRIVATE)
        val pref_result = prefs.getString(COVID19INFO_LIST, null)

        Log.i(TAG + "pref", pref_result.toString())

        // ????????? ????????? ???????????? ??????????????????.
        try {

            var jsonArray = JSONArray(pref_result)

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

                var marker = MapPOIItem()
                marker.itemName = obj.get("centerName").toString()
                marker.tag = 0 // TODO ?????? ??????????????? ????????????
                marker.mapPoint = MapPoint.mapPointWithGeoCoord(obj.get("lng").toString().toDouble(), obj.get("lat").toString().toDouble())
                marker.markerType = MapPOIItem.MarkerType.BluePin // ?????? ?????? ??????
                marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin // ????????? ?????? ??????
                marker.userObject = obj

                mapView.addPOIItem(marker)
            }

        } catch (e: JSONException) { e.printStackTrace() }

        // ADSView??? ???????????????
        MobileAds.initialize(this)

        // ??????????????? ???????????????.
        val adRequest = AdRequest.Builder().build()
        adView!!.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {

                // Code to be executed when an ad finishes loading.

                // ????????? ?????? ?????? ????????? ???????????????.
                Log.d("@@@", "onAdLoaded")
            }

            override fun onAdFailedToLoad(errorCode: Int) {

                // Code to be executed when an ad request fails.

                // ?????? ????????? ????????? ????????? ???????????????.
                Log.d("@@@", "onAdFailedToLoad $errorCode")
            }

            override fun onAdOpened() { }

            override fun onAdClicked() { }

            override fun onAdLeftApplication() { }

            override fun onAdClosed() { }
        }

//        // ?????? ????????? ???????????????.
//        InterstitialAd.load(this, ALL_AD_BANNER, adRequest, object : InterstitialAdLoadCallback() {
//            override fun onAdFailedToLoad(adError: LoadAdError) {
//                Log.d(TAG, adError?.message)
//                mInterstitialAd = null
//            }
//
//            override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                Log.d(TAG, "Ad was loaded.")
//                mInterstitialAd = interstitialAd
//            }
//        })
//        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
//            override fun onAdDismissedFullScreenContent() {
//                Log.d(TAG, "Ad was dismissed.")
//            }
//
//            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
//                Log.d(TAG, "Ad failed to show.")
//            }
//
//            override fun onAdShowedFullScreenContent() {
//                Log.d(TAG, "Ad showed fullscreen content.")
//                mInterstitialAd = null;
//            }
//        }

    }

    private fun showDialogForLocationServiceSetting() {
        var builder = AlertDialog.Builder(this)
        builder.setTitle("?????? ????????? ?????????")
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n?????? ????????? ?????????????????????????")
        builder.setCancelable(true)
        builder.setPositiveButton("??????") { dialog, id ->
            val callGPSSettingIntent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        }
        builder.setNegativeButton("??????") { dialog, id -> dialog.cancel() }
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
            GPS_ENABLE_REQUEST_CODE ->                 //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS ????????? ?????????")
                        checkRunTimePermission()
                        return
                    }
                }
        }
    }

    //	/////////////////////////////////////////////////////////////////////////////////////////////////
    //  MapView.OpenAPIKeyAuthenticationResultListener
    override fun onDaumMapOpenAPIKeyAuthenticationResult(mapView: MapView?, resultCode: Int, resultMessage: String?) {
        Log.i(TAG, String.format("Open API Key Authentication Result : code=%d, message=%s", resultCode, resultMessage))
    }

    //	/////////////////////////////////////////////////////////////////////////////////////////////////
    //  MapView.CurrentLocationEventListener
    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
        TODO("Not yet implemented")
    }

    override fun onCurrentLocationUpdate(mapView: MapView?, currentLocation: MapPoint?, float: Float) {

        val mapPointGeo = currentLocation!!.mapPointGeoCoord
        latitude = mapPointGeo.latitude
        longtitude = mapPointGeo.longitude
//        Log.i("$TAG ????????????", "?????? : $latitude, ?????? : $longtitude")
//        Toast.makeText(this, "?????? : $latitude, ?????? : $longtitude", Toast.LENGTH_LONG)
//            .show()

    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
        TODO("Not yet implemented")
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
        TODO("Not yet implemented")
    }

    //	/////////////////////////////////////////////////////////////////////////////////////////////////
    //  MapView.POIItemEventListener
    override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, poiItem: MapPOIItem?) {
//        Log.i("onPOIItemSelected2", poiItem!!.itemName)
    }

    // ???????????? ??????
    override fun onCalloutBalloonOfPOIItemTouched(
        mapView: MapView?,
        poiItem: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {

        // ??????
        // ????????? 5????????? ??? ?????? ????????? ????????????. ???????????? ?????? ???????????? SharedPreference??? ????????????.
        val prefs: SharedPreferences = getSharedPreferences(ALL_AD_COUNT, Context.MODE_PRIVATE)

        // 1. ?????? ????????? ??? ??? ?????????????????? ????????????.
        var ad_watch_count = prefs.getInt(ALL_AD_COUNT, 0)

        Log.i("ad_watch_count", ad_watch_count.toString())

        // 2. ?????? ?????? ???????????? 0????????? ????????? ????????????.
        if (ad_watch_count == 0) {
            // ?????? ????????? ???????????????.
            InterstitialAd.load(this, ALL_AD_BANNER, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError?.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.show(this@MainActivity)

                    // 1. ????????? ??????????????? ???????????????, ?????? ?????? ???????????? 1 ???????????????.
                    ad_watch_count += 1

                    //2. ?????? ?????? ???????????? ???????????????.
                    prefs.edit().putInt(ALL_AD_COUNT, ad_watch_count).apply()

                    //3. ???????????? ?????? ?????? ???????????? ????????????.
                    requireNotNull(mInterstitialAd).fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad was dismissed.")

                            goToKakaoMap(poiItem)

                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                            Log.d(TAG, "Ad failed to show.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad showed fullscreen content.")
                            mInterstitialAd = null;
                        }
                    }
                }
            })

        } else { // ????????? ??? ???????????? ???????????????,

            //1. ?????????????????? ??????????????????.
            goToKakaoMap(poiItem)

            // 2. ?????? ???????????? 1 ??????????????????.
            ad_watch_count += 1

            // 3. ?????? ???????????? 5?????? ????????? 0?????? ?????????.
            if (ad_watch_count > MAX_AD_COUNT) ad_watch_count = 0

            // 3. ?????? ???????????? ????????????.
            prefs.edit().putInt(ALL_AD_COUNT, ad_watch_count).apply()

        }

    }


    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
        TODO("Not yet implemented")
    }

    override fun onPOIItemSelected(mapView: MapView?, poiItem: MapPOIItem?) {

        Log.i("onPOIItemSelected", poiItem!!.itemName)
    }

    private fun goToKakaoMap(poiItem: MapPOIItem?) {

        // ???????????? ????????? ????????? ??????
        var jsonObject = poiItem!!.userObject as JSONObject

        Log.i("onPOIItemSelected", jsonObject!!.get("centerName").toString())
        Log.i("onPOIItemSelected", jsonObject!!.get("address").toString())

        var lat = jsonObject!!.get("lat")
        var lng = jsonObject!!.get("lng")

        val URL = "kakaomap://route?sp=$latitude,$longtitude&ep=$lng,$lat&by=CAR" // ??????????????? ?????????????????? ????????? (?????????)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL))
        startActivity(intent)

    }

}