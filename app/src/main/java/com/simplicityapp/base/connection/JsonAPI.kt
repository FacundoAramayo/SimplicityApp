package com.simplicityapp.base.connection

import com.simplicityapp.base.connection.callbacks.*
import com.simplicityapp.modules.settings.model.DeviceInfo
import retrofit2.Call
import retrofit2.http.*

interface JsonAPI {
    /* Place API transaction ------------------------------- */

    @Headers(CACHE, AGENT)
    @GET("app/services/listPlaces")
    fun getPlacesByPage(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("draft") draft: Int
    ): Call<CallbackListPlace>?

    @Headers(CACHE, AGENT)
    @GET("app/services/getPlaceDetails")
    fun getPlaceDetails(
        @Query("place_id") place_id: Int
    ): Call<CallbackPlaceDetails>?

    /* News Info API transaction ------------------------------- */
    @Headers(CACHE, AGENT)
    @GET("app/services/listNewsInfo")
    fun getContentInfoByPage(
        @Query("page") page: Int,
        @Query("count") count: Int
    ): Call<CallbackListContentInfo>?

    @Headers(CACHE, AGENT)
    @POST("app/services/insertGcm")
    fun registerDevice(
        @Body deviceInfo: DeviceInfo?
    ): Call<CallbackDevice>?


    @Headers(CACHE, AGENT)
    @GET("/res/categories.json")
    suspend fun getCategories() : CallbackCategories

    companion object {
        const val CACHE = "Cache-Control: max-age=0"
        const val AGENT = "User-Agent: Place"
    }
}