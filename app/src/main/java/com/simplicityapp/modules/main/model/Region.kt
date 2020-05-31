package com.simplicityapp.modules.main.model

import com.google.gson.annotations.SerializedName

data class Region (
    @SerializedName("reg_id")
    val regionId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("lat")
    val latitude: Long,

    @SerializedName("lon")
    val longitude: Long,

    @SerializedName("radius")
    val radius: Int
    )