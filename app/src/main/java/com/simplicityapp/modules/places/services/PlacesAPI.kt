package com.simplicityapp.modules.places.services

import com.simplicityapp.base.connection.JsonAPI
import com.simplicityapp.modules.places.model.Place
import com.simplicityapp.modules.places.model.PlacesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface PlacesAPI {

    @Headers(JsonAPI.CACHE, JsonAPI.AGENT)
    @GET("app/services/listPlaces")
    suspend fun getPlacesByPage(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("draft") draft: Int,
        @Query("reg_id") regId: Int
    ): Response<PlacesResponse>?

    @Headers(JsonAPI.CACHE, JsonAPI.AGENT)
    @GET("app/services/getPlaceDetails")
    suspend fun getPlaceDetails(
        @Query("place_id") place_id: Int
    ): Response<Place>?

}