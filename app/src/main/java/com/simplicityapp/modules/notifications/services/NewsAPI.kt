package com.simplicityapp.modules.notifications.services

import com.simplicityapp.base.connection.JsonAPI.Companion.AGENT
import com.simplicityapp.base.connection.JsonAPI.Companion.CACHE
import com.simplicityapp.modules.notifications.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NewsAPI {

    /* News Info API transaction ------------------------------- */
    @Headers(CACHE, AGENT)
    @GET("app/services/listNewsInfo")
    suspend fun getNewsByPage(
        @Query("page") page: Int,
        @Query("count") count: Int
    ): Response<NewsResponse>?

}