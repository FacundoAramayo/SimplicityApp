package com.simplicityapp.modules.places.repository

import com.simplicityapp.base.rest.RetrofitService
import com.simplicityapp.modules.places.services.PlacesAPI

class PlacesRepository {

    private var jsonApi : PlacesAPI = RetrofitService.createService(PlacesAPI::class.java)

    suspend fun getPlacesByPage(page: Int, count: Int, draft: Int, regId: Int) = jsonApi.getPlacesByPage(page, count, draft, regId)

    suspend fun getPlace(id: Int) = jsonApi.getPlaceDetails(id)

}