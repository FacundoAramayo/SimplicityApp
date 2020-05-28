package com.simplicityapp.modules.categories.viewmodel

import androidx.lifecycle.ViewModel
import com.simplicityapp.modules.categories.factory.CategoriesFactoryImpl
import com.simplicityapp.modules.categories.model.CategoriesResponse
import com.simplicityapp.modules.categories.model.Category
import com.simplicityapp.modules.categories.repository.CategoriesRepository
import retrofit2.Response

class CategoriesSelectorViewModel : ViewModel() {

    private val categoriesFactory = CategoriesFactoryImpl()
    val repository: CategoriesRepository = CategoriesRepository()

    fun getCategories(): List<Category> {
        return categoriesFactory.getCategoriesList()
    }

    suspend fun getCategoriesAsync(): Response<CategoriesResponse> {
        return repository.getCategories()
    }

    fun onCategoryClicked() {

    }


}
