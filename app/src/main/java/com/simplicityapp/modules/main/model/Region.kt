package com.simplicityapp.modules.main.model

import com.google.gson.annotations.SerializedName

data class Region (
    @SerializedName("reg_id")
    val regionId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("lat")
    val latitude: Float,

    @SerializedName("lon")
    val longitude: Float,

    @SerializedName("radius")
    val radius: Int
    )