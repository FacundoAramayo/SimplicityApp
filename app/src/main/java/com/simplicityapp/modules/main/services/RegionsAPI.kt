package com.simplicityapp.modules.main.services

import com.simplicityapp.modules.settings.services.DeviceAPI.Companion.AGENT
import com.simplicityapp.modules.settings.services.DeviceAPI.Companion.CACHE
import com.simplicityapp.modules.main.model.Region
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface RegionsAPI {

    @Headers(CACHE, AGENT)
    @GET("app/services/getRegions")
    suspend fun getRegions(): Response<List<Region>>

}