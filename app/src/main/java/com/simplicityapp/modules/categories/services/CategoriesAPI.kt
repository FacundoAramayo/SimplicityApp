package com.simplicityapp.modules.categories.services

import com.simplicityapp.modules.settings.services.DeviceAPI.Companion.CACHE
import com.simplicityapp.modules.settings.services.DeviceAPI.Companion.AGENT
import com.simplicityapp.modules.categories.model.CategoriesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface CategoriesAPI {

    @Headers(CACHE, AGENT)
    @GET("res/{type}.json")
    suspend fun getCategories(
        @Path(value="type") type: String
    ) : Response<CategoriesResponse>

}