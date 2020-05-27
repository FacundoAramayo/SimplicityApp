package com.simplicityapp.modules.categories.factory

import com.simplicityapp.modules.categories.model.Category

interface CategoriesFactory {

    fun getCategoriesList(): List<Category>

}