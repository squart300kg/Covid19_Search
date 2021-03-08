package com.covid19.search

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.Result
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://api.odcloud.kr/api/"
//private const val BASE_URL = "https://api.odcloud.kr/api/15077586/v1"

internal interface CovidAPIService {

    @Headers(
        "Content-Type: application/json"
    )
    @GET("/api/v1/payment")
    fun payment(@Body request: Covid19InfoRequest)
            : Single<Result<Covid19InfoResponse>>

    @Headers(
        "Content-Type: application/json"
    )
    @GET("15077586/v1/centers")
    fun getCovid19InfoList(@Query("page") page: Int,
                           @Query("perPage") perPage: Int,
                           @Query("serviceKey") serviceKey: String = "data-portal-test-key",//"5mlJGQ%2BNaEgk%2BInk45j4lykrpjBX2W7F0gAXiTcTniX5P2ehcDqTAng4nWja7%2BHWuzh4gezDef5AZUMsRN5K%2FA%3D%3D",
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