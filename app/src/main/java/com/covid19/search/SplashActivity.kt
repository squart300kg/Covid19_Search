package com.covid19.search

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_splash.*
import org.json.JSONObject
import retrofit2.adapter.rxjava2.Result

class SplashActivity : AppCompatActivity() {

    private val COVID19_LIST = "COVID19_LIST"
//    private val prefs: SharedPreferences = this.getSharedPreferences(COVID19_LIST, Context.MODE_PRIVATE)

    private val covidAPIService by lazy {
        CovidAPIService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Glide.with(this).load(R.raw.splash_image_large).into(DrawableImageViewTarget(splash_image))

        getCovidInfo()

        startLoading()

    }

    private fun getCovidInfo() {
        val apiCall: Disposable = covidAPIService
            .getCovid19InfoList(1,100, isAll = false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    result ->
                         onSuccessGetCovid19Info(result)

                },
                {
                    onFailGetCovid19Info(it)
                }
            )
    }

    private fun onSuccessGetCovid19Info(response: Result<Covid19InfoResponse>?) {
        // TODO 이부분 왜 널체크하라고 뜨는지 알아보기
        if (response!!.response() == null) {
//            onFailGetPaymentHistory(response.error())
            onFailGetCovid19Info
        } else {
            val statusCode = response.response()?.code()
            if (statusCode == 200) {
                val body = requireNotNull(response.response()?.body())

                with(body) {

                    val covid19InfoList = ArrayList<JSONObject>()

                    for (covid19Info in data) {

                        //TODO 이쪽 부분 코드 리팩토링할것 생각하기
                        Log.i("covidResult : ", covid19Info.centerName)
                        Log.i("covidResult : ", covid19Info.address)
                        Log.i("covidResult : ", covid19Info.centerType)
                        Log.i("covidResult : ", covid19Info.facilityName)
                        Log.i("covidResult : ", covid19Info.lat)
                        Log.i("covidResult : ", covid19Info.lng)
                        Log.i("covidResult : ", covid19Info.org)
                        Log.i("covidResult : ", covid19Info.sido)
                        Log.i("covidResult : ", covid19Info.sigungu)
                        Log.i("covidResult : ", covid19Info.zipCode)
                        Log.i("covidResult : ", "========================================")

                        val jsonObject = JSONObject()
                        jsonObject.put("centerName", covid19Info.centerName)
                        jsonObject.put("address", covid19Info.address)
                        jsonObject.put("centerType", covid19Info.centerType)
                        jsonObject.put("facilityName", covid19Info.facilityName)
                        jsonObject.put("lat", covid19Info.lat)
                        jsonObject.put("lng", covid19Info.lng)
                        jsonObject.put("org", covid19Info.org)
                        jsonObject.put("sido", covid19Info.sido)
                        jsonObject.put("sigungu", covid19Info.sigungu)
//                        covid19InfoMap.put("zipCode", covid19Info.zipCode)

                        // 그렇다면 일단 해야할 것. 리스트에 jsonobject를 넣자
                        covid19InfoList.add(jsonObject)

                        val prefs: SharedPreferences = getSharedPreferences("COVID19INFO_LIST", Context.MODE_PRIVATE)
                        prefs.edit().putString("covid19info_list", covid19InfoList.toString()).apply()

                        // TODO 추후 API테스트. 한번에 몇뭉치의 데이터를 가져올 수 있는지를 체크할 것
                        // 아니면 차라리 그냥 api신청을 지급 하는게 더 낫다
                    }
                }
            } else {
                // TODO API 수신 실패시 동작 처리할 것
                onFailGetCovid19Info
            }
        }
    }

    private val onFailGetCovid19Info = { it: Throwable? ->

        Log.i("covidResult : ", "network error")

    }

    private fun startLoading() {
        val handler = Handler()
        handler.postDelayed(Runnable {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2800)
    }
}