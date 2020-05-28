package com.simplicityapp.modules.categories.services

import com.simplicityapp.base.connection.JsonAPI.Companion.CACHE
import com.simplicityapp.base.connection.JsonAPI.Companion.AGENT
import com.simplicityapp.modules.categories.model.CategoriesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface CategoriesAPI {

    @Headers(CACHE, AGENT)
    @GET("res/categories.json")
    suspend fun getCategories() : Response<CategoriesResponse>

}