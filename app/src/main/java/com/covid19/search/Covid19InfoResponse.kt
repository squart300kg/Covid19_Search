package com.covid19.search

data class Covid19Info(val id: Int,
                        val centerName: String,
                        val sido: String,
                        val sigungu: String,
                        val facilityName: String,
                        val zipCode: String,
                        val address: String,
                        val lat: String,
                        val lng: String,
                        val centerType: String,
                        val org: String)

internal data class Covid19InfoResponse(val page: Int,
                                val perPage: Int,
                                val totalCount: Int,
                                val currentCount: Int,
                                val data: ArrayList<Covid19Info>)

