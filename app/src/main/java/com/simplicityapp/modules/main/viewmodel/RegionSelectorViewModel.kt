package com.simplicityapp.modules.main.viewmodel

import androidx.lifecycle.ViewModel
import com.simplicityapp.modules.main.model.Region
import com.simplicityapp.modules.main.repository.MainRepository
import retrofit2.Response

class RegionSelectorViewModel : ViewModel() {

    private val repository: MainRepository = MainRepository()

    suspend fun getRegionsAsync(): Response<List<Region>> {
        return repository.getRegions()
    }

}