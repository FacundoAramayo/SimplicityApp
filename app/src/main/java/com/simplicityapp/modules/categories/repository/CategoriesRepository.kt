package com.simplicityapp.modules.categories.repository

import com.simplicityapp.base.rest.RetrofitService
import com.simplicityapp.modules.categories.services.CategoriesAPI

class CategoriesRepository {

    private var jsonApi : CategoriesAPI = RetrofitService.createService(CategoriesAPI::class.java)

    suspend fun getCategories() = jsonApi.getCategories()

}