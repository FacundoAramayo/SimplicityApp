package com.simplicityapp.modules.notifications.model

import com.google.gson.annotations.SerializedName

data class NewsResponse (

    @SerializedName("status")
    val status: String,

    @SerializedName("count")
    val count: Int,

    @SerializedName("count_total")
    val countTotal: Int,

    @SerializedName("pages")
    val pages: Int,

    @SerializedName("news_infos")
    val newsList: List<News>


)