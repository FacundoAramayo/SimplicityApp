package com.simplicityapp.base.config.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.simplicityapp.base.config.ThisApplication

class AnalyticsConstants {

    companion object {
        /**
         * --------------------------------Analytics-------------------------------------------
         */

        //COMMON PARAMS
        const val OPEN_REGISTER_FORM = "OPEN_REGISTER_FORM"
        const val OPEN_SUGGESTION_FORM = "OPEN_SUGGESTION_FORM"
        const val OPEN_GET_IN_TOUCH = "OPEN_GET_IN_TOUCH"
        const val OPEN_SETTINGS = "OPEN_SETTINGS"
        const val RATE_APP = "RATE_APP"
        const val CONTENT_PLACE = "CONTENT_PLACE"
        const val CONTENT_NOTIFICATION = "CONTENT_NOTIFICATION"

        //START EVENTS
        const val TUTORIAL_BEGIN = FirebaseAnalytics.Event.TUTORIAL_BEGIN
        const val TUTORIAL_COMPLETE = FirebaseAnalytics.Event.TUTORIAL_COMPLETE
        const val TERMS_AND_CONDITIONS_PREVIOUS = "TERMS_AND_CONDITIONS_PREVIOUS"
        const val TERMS_AND_CONDITIONS_SUCCESS = "TERMS_AND_CONDITIONS_SUCCESS"
        const val APP_OPEN = FirebaseAnalytics.Event.APP_OPEN // LOG USER

        //START PARAMS
        const val FIRST_RUN = "FIRST_RUN"
        const val NORMAL_RUN = "NORMAL_RUN"
        const val UPGRADE_RUN = "UPGRADE_RUN"

        //LOGIN EVENTS
        const val SIGN_UP = FirebaseAnalytics.Event.SIGN_UP // LOG USER

        //SHARE EVENTS
        const val SHARE = FirebaseAnalytics.Event.SHARE

        //HOME EVENTS
        const val SELECT_HOME_FEATURED_BANNER = "SELECT_HOME_FEATURED_BANNER"
        const val SELECT_HOME_NEWS = "SELECT_HOME_NEWS"

        const val SELECT_HOME_ACTION = "SELECT_HOME_ACTION"
        const val SELECT_HOME_QUICK_ACCESS = "SELECT_HOME_QUICK_ACCESS"
        const val SELECT_TOOLBAR_ACTION = "SELECT_TOOLBAR_ACTION"

        //HOME PARAMS
        const val FAB_MAP = "FAB_MAP"
        const val FAB_SEARCH = "FAB_SEARCH"
        const val FAB_FAVORITES = "FAB_FAVORITES"
        const val SHARE_APP = "SHARE_APP"
        const val REFRESH = "REFRESH"

        const val QUICK_ACCESS_DELIVERY = "QUICK_ACCESS_DELIVERY"
        const val QUICK_ACCESS_TAXI = "QUICK_ACCESS_TAXI"
        const val QUICK_ACCESS_JOBS = "QUICK_ACCESS_JOBS"
        const val QUICK_ACCESS_PHARMACY = "QUICK_ACCESS_PHARMACY"
        const val QUICK_ACCESS_SEARCH = "QUICK_ACCESS_SEARCH"
        const val QUICK_ACCESS_FAVORITES = "QUICK_ACCESS_FAVORITES"
        const val QUICK_ACCESS_MAP = "QUICK_ACCESS_MAP"
        const val QUICK_ACCESS_EMERGENCY = "QUICK_ACCESS_EMERGENCY"

        const val TOOLBAR_SETTINGS = "TOOLBAR_SETTINGS"
        const val TOOLBAR_RATE = "TOOLBAR_RATE"
        const val TOOLBAR_ABOUT = "TOOLBAR_ABOUT"

        //MENU EVENTS
        const val SELECT_MENU_ACTION = "SELECT_MENU_ACTION"

        //MENU PARAMS
        const val HOME = "HOME"
        const val ALL_PLACES = "ALL_PLACES"
        const val MAP = "MAP"
        const val SEARCH = "SEARCH"
        const val FAVORITES = "FAVORITES"
        const val NOTIFICATIONS = "NOTIFICATIONS"

        //CATEGORY EVENTS
        const val SELECT_CATEGORY = "SELECT_CATEGORY"

        //CATEGORY PARAMS
        const val CATEGORY_FEATURED = "CATEGORY_FEATURED"
        const val CATEGORY_STORE = "CATEGORY_STORE"
        const val CATEGORY_DELIVERY = "CATEGORY_DELIVERY"
        const val CATEGORY_FAST_FOOD = "CATEGORY_FAST_FOOD"
        const val CATEGORY_RESTAURANTS = "CATEGORY_RESTAURANTS"
        const val CATEGORY_BAR = "CATEGORY_BAR"
        const val CATEGORY_ICE_CREAM = "CATEGORY_ICE_CREAM"
        const val CATEGORY_GYM_CENTER = "CATEGORY_GYM_CENTER"
        const val CATEGORY_CLOTHING_STORES = "CATEGORY_CLOTHING_STORES"
        const val CATEGORY_BIG_STORES = "CATEGORY_BIG_STORES"
        const val CATEGORY_INDUSTRIAL_STORES = "CATEGORY_INDUSTRIAL_STORES"
        const val CATEGORY_BILL_PAYMENTS = "CATEGORY_BILL_PAYMENTS"
        const val CATEGORY_ART = "CATEGORY_ART"
        const val CATEGORY_JOBS = "CATEGORY_JOBS"
        const val CATEGORY_MONEY = "CATEGORY_MONEY"
        const val CATEGORY_TOURIST_DESTINATION = "CATEGORY_TOURIST_DESTINATION"
        const val CATEGORY_HOTEL = "CATEGORY_HOTEL"
        const val CATEGORY_APARTMENT_RENTAL = "CATEGORY_APARTMENT_RENTAL"
        const val CATEGORY_TEMPORARY_RENT = "CATEGORY_TEMPORARY_RENT"
        const val CATEGORY_TAXI = "CATEGORY_TAXI"
        const val CATEGORY_GAS_STATION = "CATEGORY_GAS_STATION"
        const val CATEGORY_TRANSPORT = "CATEGORY_TRANSPORT"
        const val CATEGORY_TRANSPORT_TICKETS = "CATEGORY_TRANSPORT_TICKETS"
        const val CATEGORY_PHARMACY = "CATEGORY_PHARMACY"
        const val CATEGORY_EMERGENCIES = "CATEGORY_EMERGENCIES"

        //PLACE EVENTS
        const val VIEW_PLACE = "VIEW_PLACE"

        const val SELECT_PLACE_ADDRESS = "SELECT_PLACE_ADDRESS"
        const val SELECT_PLACE_PHONE = "SELECT_PLACE_PHONE"
        const val SELECT_PLACE_WHATSAPP = "SELECT_PLACE_WHATSAPP"
        const val SELECT_PLACE_INSTAGRAM = "SELECT_PLACE_INSTAGRAM"
        const val SELECT_PLACE_FACEBOOK = "SELECT_PLACE_FACEBOOK"
        const val SELECT_PLACE_WEB_SITE = "SELECT_PLACE_WEB_SITE"
        const val SELECT_PLACE_PHOTO = "SELECT_PLACE_PHOTO"
        const val SELECT_PLACE_DESCRIPTION_CONTENT = "SELECT_PLACE_DESCRIPTION_CONTENT"
        const val SELECT_PLACE_OPEN_MAP = "SELECT_PLACE_OPEN_MAP"
        const val SELECT_PLACE_OPEN_NAVIGATION = "SELECT_PLACE_OPEN_NAVIGATION"
        const val SELECT_PLACE_SHARE = "SELECT_PLACE_SHARE"
        const val SELECT_PLACE_FAVORITES_ADD = "SELECT_PLACE_FAVORITES_ADD"
        const val SELECT_PLACE_FAVORITES_REMOVE = "SELECT_PLACE_FAVORITES_REMOVE"

        //NOTIFICATION EVENTS
        const val VIEW_NOTIFICATION = "VIEW_NOTIFICATION"
        const val SELECT_NOTIFICATION_ACTION = "SELECT_NOTIFICATION_ACTION"
        const val SELECT_NOTIFICATION_ITEM_SHARE = "SELECT_NOTIFICATION_ITEM_SHARE"
        const val SELECT_NOTIFICATION_OPEN_PHOTO = "SELECT_NOTIFICATION_OPEN_PHOTO"
        const val SELECT_NOTIFICATION_OPEN_LIST_ITEM = "SELECT_NOTIFICATION_OPEN_PHOTO"

        //NOTIFICATIONS PARAMS

        const val REFRESH_LIST = "REFRESH_LIST"

        //MAP
        const val SELECT_MAP_CATEGORY = "SELECT_MAP_CATEGORY"
        const val SELECT_MAP_PLACE = "SELECT_MAP_PLACE"

        //SEARCH
        const val SEARCH_PLACE = "SEARCH_PLACE"
        const val SELECT_SEARCHED_PLACE = "SELECT_SEARCHED_PLACE"

        const val SELECT_CATEGORY_PLACE = "SELECT_CATEGORY_PLACE"

        /**
         * --------------------------------Analytics Methods----------------------------------------
         */

        fun logAnalyticsEvent(event: String, label: String? = null, user: Boolean? = null) {
            ThisApplication.instance?.trackEvent(event, label,  user)
        }

        fun logAnalyticsSignUp() {
            ThisApplication.instance?.trackAnalyticsSignUp()
        }

        fun logAnalyticsShare(contentType: String, item: String) {
            ThisApplication.instance?.logAnalyticsShare(contentType, item)
        }
    }



}