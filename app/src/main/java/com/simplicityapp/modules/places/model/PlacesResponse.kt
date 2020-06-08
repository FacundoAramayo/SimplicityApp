package com.simplicityapp.modules.places.model

import com.google.gson.annotations.SerializedName

data class PlacesResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("count")
    val count: Int,

    @SerializedName("count_total")
    val count_total: Int,

    @SerializedName("pages")
    val pages: Int,

    @SerializedName("places")
    val places: List<Place>
)