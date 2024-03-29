package com.simplicityapp.base.persistence.db

object DatabaseConstants {

    // Database Version
    const val DATABASE_VERSION = 6

    // Database Name
    const val DATABASE_NAME = "simplicity_app"

    // Main Table Name
    const val TABLE_PLACE = "place"
    const val TABLE_IMAGES = "images"
    const val TABLE_CATEGORY = "category"
    const val TABLE_NEWS_INFO = "content_info"

    // Relational table Place to Category ( N to N )
    const val TABLE_PLACE_CATEGORY = "place_category"

    // table only for android client
    const val TABLE_FAVORITES = "favorites_table"

    // Table Columns names TABLE_PLACE
    const val KEY_PLACE_ID = "place_id"
    const val KEY_NAME = "name"
    const val KEY_IMAGE = "image"
    const val KEY_ADDRESS = "address"
    const val KEY_PHONE = "phone"
    const val KEY_WEBSITE = "website"
    const val KEY_DESCRIPTION = "description"
    const val KEY_LNG = "lng"
    const val KEY_LAT = "lat"
    const val KEY_DISTANCE = "distance"
    const val KEY_LAST_UPDATE = "last_update"
    const val KEY_REGION_ID = "reg_id"
    const val KEY_WHATSAPP = "whatsapp"
    const val KEY_INSTAGRAM = "instagram"
    const val KEY_FACEBOOK = "facebook"
    const val KEY_SHORT_DESCRIPTION = "short_description"
    const val KEY_CATEGORIES_LIST = "categories_list"
    const val KEY_OFFER_IMAGE = "offer_image"

    // Table Columns names TABLE_IMAGES
    const val KEY_IMG_PLACE_ID = "place_id"
    const val KEY_IMG_NAME = "name"

    // Table Columns names TABLE_CATEGORY
    const val KEY_CAT_ID = "cat_id"
    const val KEY_CAT_NAME = "name"
    const val KEY_CAT_ICON = "icon"

    // Table Columns names TABLE_NEWS_INFO
    const val KEY_NEWS_ID = "id"
    const val KEY_NEWS_TITLE = "title"
    const val KEY_NEWS_BRIEF_CONTENT = "brief_content"
    const val KEY_NEWS_FULL_CONTENT = "full_content"
    const val KEY_NEWS_IMAGE = "image"
    const val KEY_NEWS_LAST_UPDATE = "last_update"

    // Table Relational Columns names TABLE_PLACE_CATEGORY
    const val KEY_RELATION_PLACE_ID = KEY_PLACE_ID
    const val KEY_RELATION_CAT_ID = KEY_CAT_ID
}