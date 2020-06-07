package com.simplicityapp.base.connection.callbacks

import java.io.Serializable
import java.util.ArrayList

import com.simplicityapp.modules.places.model.Place

class ListPlaceResponse : Serializable {

    var status = ""
    var count = -1
    var count_total = -1
    var pages = -1
    var places: List<Place> = ArrayList()

}
