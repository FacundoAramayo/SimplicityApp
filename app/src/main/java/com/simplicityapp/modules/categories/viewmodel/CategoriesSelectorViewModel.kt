package com.simplicityapp.modules.categories.viewmodel

import androidx.lifecycle.ViewModel
import com.simplicityapp.modules.categories.factory.CategoriesFactoryImpl
import com.simplicityapp.modules.categories.model.Category

class CategoriesSelectorViewModel : ViewModel() {

    private val categoriesFactory = CategoriesFactoryImpl()

    fun getCategories(): List<Category> {
        return categoriesFactory.getCategoriesList()
    }

    fun onCategoryClicked() {

    }


}
