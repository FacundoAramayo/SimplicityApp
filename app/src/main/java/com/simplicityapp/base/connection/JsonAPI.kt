package com.simplicityapp.base.connection

import com.simplicityapp.base.connection.callbacks.*
import com.simplicityapp.modules.settings.model.DeviceInfo
import retrofit2.Call
import retrofit2.http.*

interface JsonAPI {

    //TODO: Todas las peticiones deben ir a su respectivo módulo, en services, y ser suspend fun con corutinas

    /* Place API transaction ------------------------------- */

    //Pendiente de quitar (ya está la implementación de courutina en FragmentHome)
    @Headers(CACHE, AGENT)
    @GET("app/services/listPlaces")
    fun getPlacesByPage(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("draft") draft: Int,
        @Query("reg_id") regId: Int
    ): Call<ListPlaceResponse>?

    //Pendiente de quitar (ya está la implementación de courutina en FragmentHome)
    @Headers(CACHE, AGENT)
    @GET("app/services/getPlaceDetails")
    fun getPlaceDetails(
        @Query("place_id") place_id: Int
    ): Call<CallbackPlaceDetails>?

    @Headers(CACHE, AGENT)
    @POST("app/services/insertGcm")
    fun registerDevice(
        @Body deviceInfo: DeviceInfo?
    ): Call<CallbackDevice>?

    companion object {
        const val CACHE = "Cache-Control: max-age=0"
        const val AGENT = "User-Agent: Place"
    }
}