package com.simplicityapp.modules.notifications.model

import java.io.Serializable

import com.simplicityapp.modules.places.model.Place

data class FcmNotification(
        var title: String? = null,
        var content: String? = null,
        var type: String? = null,
        var place: Place? = null,
        var news: News? = null
    ) : Serializable
