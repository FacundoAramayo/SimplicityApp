package com.simplicityapp.modules.notifications.repository

import com.simplicityapp.base.rest.RetrofitService
import com.simplicityapp.modules.notifications.services.NewsAPI

class NewsRepository {

    private var jsonApi : NewsAPI = RetrofitService.createService(NewsAPI::class.java)

    suspend fun getNews(page: Int, count: Int) = jsonApi.getNewsByPage(page, count)

}