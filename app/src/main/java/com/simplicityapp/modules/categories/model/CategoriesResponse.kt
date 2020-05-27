package com.simplicityapp.modules.categories.model

import com.google.gson.annotations.SerializedName

data class CategoriesResponse(
    @SerializedName("category_list")
    val categories: List<Category>
)