package com.simplicityapp.modules.categories.services

import com.simplicityapp.base.connection.JsonAPI.Companion.CACHE
import com.simplicityapp.base.connection.JsonAPI.Companion.AGENT
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