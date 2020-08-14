package com.simplicityapp.modules.settings.services

import com.simplicityapp.modules.settings.model.CallbackDevice
import com.simplicityapp.modules.settings.model.DeviceInfo
import retrofit2.Call
import retrofit2.http.*

interface DeviceAPI {

    @Headers(
        CACHE,
        AGENT
    )
    @POST("app/services/insertGcm")
    fun registerDevice(
        @Body deviceInfo: DeviceInfo?
    ): Call<CallbackDevice>?

    companion object {
        const val CACHE = "Cache-Control: max-age=0"
        const val AGENT = "User-Agent: Place"
    }
}