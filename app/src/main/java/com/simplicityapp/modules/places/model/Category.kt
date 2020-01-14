package com.simplicityapp.modules.places.model

import java.io.Serializable

data class Category (
        var cat_id: Int = 0,
        var name: String? = null,
        var icon: Int = 0
    ): Serializable
