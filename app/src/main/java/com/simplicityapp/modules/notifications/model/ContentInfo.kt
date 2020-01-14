package com.simplicityapp.modules.notifications.model

import java.io.Serializable

data class ContentInfo (
        var id: Int = 0,
        var title: String? = null,
        var brief_content: String? = null,
        var full_content: String? = null,
        var image: String? = null,
        var last_update: Long = 0
    ): Serializable
