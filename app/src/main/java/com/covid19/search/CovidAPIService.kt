package com.covid19.search

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.Result
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.net.URLEncoder

private const val BASE_URL = "https://api.odcloud.kr/api/"

internal interface CovidAPIService {

    @Headers(
        "Content-Type: application/json"
    )
    @GET("/api/v1/payment")
    fun payment(@Body request: Covid19InfoRequest)
            : Single<Result<Covid19InfoResponse>>


    // api입력 부분에 covidapi를 직접 입력? 안됨
    //
    @Headers(
        "Content-Type: application/json"
    )
    @GET("15077586/v1/centers")
    fun getCovid19InfoList(@Query("page") page: Int,
                           @Query("perPage") perPage: Int,
                           @Query("serviceKey") serviceKey: String,
                           @Query("isAll") isAll: Boolean = false)
            : Single<Result<Covid19InfoResponse>>

    companion object {
        fun create(): CovidAPIService {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()

            return retrofit.create(CovidAPIService::class.java)
        }
    }
}