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

    // 맥북 디버그용 키해시 : S4hP67qeLpxOpFdOvSnjeDaJawc=
    //

    // TODO 1. 안드로이드 최저버전 26으로 설정함 좀 더 낮출 수 없는지 알아볼 것 - 성공
    // TODO 3. 처음 실행시, 위치퍼미션 설정 안한상태여서 터짐 - 성공
    // TODO 4. 스플래시 로딩시간이 0.1초라면 API로드가 안된 상황일텐데 이땐 어떻게 될지 테스트 - 이상 무 - 성공
    // TODO 5. 콜백메소드 정리하기 - 성공
    // TODO 6. 현재위치 추출할 것 - 성공
    // TODO 9. 광고넣기 - 성공

    // TODO 2. 프로가드 적용할 것 - 성공
    // TODO 7. 아이콘모양 정할 것 - 성공
    // TODO 8. 스플래시이미지 정할 것 - 성공
    // TODO 9. 말풍선 이미지 좀 더 예쁘게 바꿔보기 - 성공
    // TODO 10. 전면광고 넣기! - 성공
    // TODO 11. 광고를 길찾기 전에 넣기, 5번에 한 번씩 광고를 띄워준다. sharedpreference를 이용 - 성공


    // TODO 배포직전!!!!!!
    // TODO 1. JKS파일 정보 메모하기,
    // TODO 2. 키해시값 새로 추출하기
    // TODO 3. 광고ID바꾸기(배너), - 성공
    // TODO 4. 광고ID바꾸기(전면), - 성공
    // TODO 5. covid19 API키 바꾸기 - 성공
    // TODO 6. splash_activity에서 앱을 실행할 때마다 api정보를 계속 내부저장소에 저장하는가? 확인할 것
    // TODO 7. jks파일로 apk재추출 테스트해볼 것 - 성공
    // TODO 8. 스플래시이미지 적용할 것
    // 확인하고자 하는 것 : 하나의 키파일로 갱신되는 apk파일을 계속 만들 수 있는가?
    // 실험1. 일단 release배너 api키를 넣고 apk 생성 : 광고 안뜸
    // 실험2. test api키를 넣고 apk를 재생성해본다.   : 광고 뜸
    // 결과 : 하나의 jks파일을 가지고 apk를 계속 갱신할 수 있다

    // TODO 추후 바꿀 것.
    // TODO 1. api수가 늘어나면 그에 맞춘 페이징 처리 및 로딩 처리
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
                Log.d("키해시는 :", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    private fun checkRunTimePermission() {

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSIONS[0]
        )

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면 위치 값을 가져올 수 있음
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            loadingMapViewAndADView()

        } else {

            //3. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다.
            checkTedPermission()

        }
    }

    private fun checkTedPermission() { //퍼미션 체크
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
            .setDeniedMessage("권한을 거부하시면 해당 앱을 사용하실 수 없습니다.\n\n[설정] > [권한]에 들어가셔서 권한을 허용해 주세요")
            .setPermissions(*REQUIRED_PERMISSIONS)
            .check()

    }

    private fun loadingMapViewAndADView() {

        // 맵뷰를 띄운다.
        mapView = MapView(this)
        mapView.setOpenAPIKeyAuthenticationResultListener(this)
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        mapView.setZoomLevel(11, true)
        mapView.setCalloutBalloonAdapter(CustomCalloutBalloonAdapter(this))
        mapView.setCurrentLocationEventListener(this)
        mapView.setPOIItemEventListener(this)
        map_view.addView(mapView)

        // 내부 저장소로부터 코로나 접종지역 정보를 가져온다
        val prefs: SharedPreferences = getSharedPreferences(COVID19INFO_LIST, Context.MODE_PRIVATE)
        val pref_result = prefs.getString(COVID19INFO_LIST, null)

        Log.i(TAG + "pref", pref_result.toString())

        // 가져온 정보를 파싱해서 마크표시한다.
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
                marker.tag = 0 // TODO 추후 무슨뜻인지 알아낼것
                marker.mapPoint = MapPoint.mapPointWithGeoCoord(obj.get("lng").toString().toDouble(), obj.get("lat").toString().toDouble())
                marker.markerType = MapPOIItem.MarkerType.BluePin // 기존 마커 색깔
                marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin // 선택된 마커 색깔
                marker.userObject = obj

                mapView.addPOIItem(marker)
            }

        } catch (e: JSONException) { e.printStackTrace() }

        // ADSView를 초기화한다
        MobileAds.initialize(this)

        // 배너광고를 초기화한다.
        val adRequest = AdRequest.Builder().build()
        adView!!.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {

                // Code to be executed when an ad finishes loading.

                // 광고가 문제 없이 로드시 출력됩니다.
                Log.d("@@@", "onAdLoaded")
            }

            override fun onAdFailedToLoad(errorCode: Int) {

                // Code to be executed when an ad request fails.

                // 광고 로드에 문제가 있을시 출력됩니다.
                Log.d("@@@", "onAdFailedToLoad $errorCode")
            }

            override fun onAdOpened() { }

            override fun onAdClicked() { }

            override fun onAdLeftApplication() { }

            override fun onAdClosed() { }
        }

//        // 전면 광고를 초기화한다.
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
//        Log.i("$TAG 현재위치", "위도 : $latitude, 경도 : $longtitude")
//        Toast.makeText(this, "위도 : $latitude, 경도 : $longtitude", Toast.LENGTH_LONG)
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

    // 길찾기를 클릭
    override fun onCalloutBalloonOfPOIItemTouched(
        mapView: MapView?,
        poiItem: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {

        // 개요
        // 길찾기 5번마다 한 번씩 광고를 띄워준다. 길찾기를 위한 카운트는 SharedPreference를 이용한다.
        val prefs: SharedPreferences = getSharedPreferences(ALL_AD_COUNT, Context.MODE_PRIVATE)

        // 1. 우선 광고를 몇 번 시청했는지를 조회한다.
        var ad_watch_count = prefs.getInt(ALL_AD_COUNT, 0)

        Log.i("ad_watch_count", ad_watch_count.toString())

        // 2. 광고 시청 카운트가 0이라면 광고를 띄워준다.
        if (ad_watch_count == 0) {
            // 전면 광고를 초기화한다.
            InterstitialAd.load(this, ALL_AD_BANNER, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError?.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.show(this@MainActivity)

                    // 1. 광고를 성공적으로 띄워줬다면, 광고 시청 카운트를 1 증가시킨다.
                    ad_watch_count += 1

                    //2. 광고 시청 카운트를 저장시킨다.
                    prefs.edit().putInt(ALL_AD_COUNT, ad_watch_count).apply()

                    //3. 전면광고 상태 콜백 메소드를 정의한다.
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

        } else { // 광고를 한 번이라도 시청했다면,

            //1. 카카오맵으로 이동시켜준다.
            goToKakaoMap(poiItem)

            // 2. 광고 카운트를 1 증가시켜준다.
            ad_watch_count += 1

            // 3. 광고 카운트가 5보다 크다면 0으로 해준다.
            if (ad_watch_count > MAX_AD_COUNT) ad_watch_count = 0

            // 3. 광고 카운트를 저장한다.
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

        // 목적지의 위도와 경도를 추출
        var jsonObject = poiItem!!.userObject as JSONObject

        Log.i("onPOIItemSelected", jsonObject!!.get("centerName").toString())
        Log.i("onPOIItemSelected", jsonObject!!.get("address").toString())

        var lat = jsonObject!!.get("lat")
        var lng = jsonObject!!.get("lng")

        val URL = "kakaomap://route?sp=$latitude,$longtitude&ep=$lng,$lat&by=CAR" // 출발점부터 도착점까지의 길찾기 (자동차)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL))
        startActivity(intent)

    }

}