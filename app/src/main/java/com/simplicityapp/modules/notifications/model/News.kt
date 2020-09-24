package com.simplicityapp.modules.notifications.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class News (
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("title")
    val title: String,

    @SerializedName("brief_content")
    val brief_content: String,

    @SerializedName("full_content")
    val full_content: String,

    @SerializedName("image")
    val image: String,

    @SerializedName("last_update")
    val last_update: Long = 0,

    @SerializedName("client_reg_id")
    val reg_id: String? = null
): Serializable