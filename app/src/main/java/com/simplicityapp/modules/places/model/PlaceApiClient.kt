package com.simplicityapp.modules.places.model

import java.io.Serializable
import java.util.ArrayList

data class PlaceApiClient (
        var places: List<Place> = ArrayList(),
        var place_category: List<PlaceCategory> = ArrayList(),
        var images: List<Images> = ArrayList()
    )
    : Serializable
