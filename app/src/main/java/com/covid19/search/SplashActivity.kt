package com.covid19.search

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_splash.*
import org.json.JSONObject
import retrofit2.adapter.rxjava2.Result

class SplashActivity : AppCompatActivity() {

    private val COVID19INFO_LIST = "0"
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

        if (requireNotNull(response).response() == null) {

            onFailGetCovid19Info

        } else {

            val statusCode = response.response()?.code()
            if (statusCode == 200) {

                val body = requireNotNull(response.response()?.body())
                saveCovid19Info(body)

            } else {

                onFailGetCovid19Info

            }

        }
    }

    private fun saveCovid19Info(body: Covid19InfoResponse) {

        val covid19InfoList = ArrayList<JSONObject>()

        for (covid19Info in body.data) {

            //TODO 이쪽 부분 코드 리팩토링할것 생각하기
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
            jsonObject.put("zipCode", covid19Info.zipCode)

            covid19InfoList.add(jsonObject)

            val prefs: SharedPreferences = getSharedPreferences(COVID19INFO_LIST, Context.MODE_PRIVATE)
            prefs.edit().putString(COVID19INFO_LIST, covid19InfoList.toString()).apply()

        }

    }

    private val onFailGetCovid19Info = { it: Throwable? ->

        Log.i("covidResult : ", "network error")

    }

    private fun startLoading() {

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2800)


    }
}