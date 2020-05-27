package com.simplicityapp.modules.categories.model

import com.google.gson.annotations.SerializedName

data class Category (
    @SerializedName("category_id")
    val categoryId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("background_resource")
    val backgroundResource: String
)
