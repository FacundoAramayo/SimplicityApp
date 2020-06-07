package com.simplicityapp.modules.main.repository

import com.simplicityapp.base.rest.RetrofitService
import com.simplicityapp.modules.main.services.RegionsAPI

class MainRepository {

    private var jsonApi : RegionsAPI = RetrofitService.createService(RegionsAPI::class.java)

    suspend fun getRegions() = jsonApi.getRegions()

}