package com.simplicityapp.modules.places.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.simplicityapp.base.utils.tryOrDefault

import java.io.Serializable
import java.util.ArrayList

class Place (
        var place_id: Int = 0,
        var name: String? = null,
        var image: String? = null,
        var address: String? = null,
        var whatsapp: String? = null,
        var short_description: String? = null,
        var instagram: String? = null,
        var facebook: String? = null,
        var phone: String? = null,
        var website: String? = null,
        var description: String? = null,
        var lng: Double = 0.toDouble(),
        var lat: Double = 0.toDouble(),
        var last_update: Long = 0,
        var distance: Float = -1f,
        var categories: List<Category> = ArrayList(),
        var images: List<Images> = ArrayList(),
        var reg_id: Int = -1,
        var offer_image: String? = null
    ): Serializable, ClusterItem {

    var categoriesList: String? = null

        override fun getPosition(): LatLng {
            return LatLng(lat, lng)
        }

    fun hasLatLngPosition(): Boolean {
        if (this.lat == 0.toDouble()) {
            return false
        }
        if (this.lng == 0.toDouble()) {
            return false
        }
        return true
    }

    fun getCategoriesAsString(): String? {
        var categoriesString = ""
        categories.forEach {
            categoriesString += "${it.name}, "
        }

        return tryOrDefault({categoriesString.substring(0, categoriesString.length -2 )}, null)
    }
}
