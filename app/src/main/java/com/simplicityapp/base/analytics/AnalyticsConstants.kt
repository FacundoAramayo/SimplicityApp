package com.simplicityapp.base.analytics

import com.simplicityapp.base.data.ThisApplication

class AnalyticsConstants {

    companion object {
        /**
         * --------------------------------Analytics Tags-------------------------------------------
         */
        const val GENERIC_TAG = "GENERIC_EVENT"
        const val OPEN_APP_TYPE = "OPEN_APP_TYPE"
        const val OPTIONS_ITEMS_SELECTED = "OPTIONS_ITEMS_SELECTED"
        const val MENU_ITEMS_SELECTED = "MENU_ITEMS_SELECTED"
        const val HOME_FAB_OPTION_SELECTED = "HOME_FAB_OPTION_SELECTED"

        const val VIEW_PLACE = "VIEW_PLACE"
        const val VIEW_CONTENT = "VIEW_CONTENT"

        /**
         * --------------------------------Analytics Actions----------------------------------------
         */
        const val FIRST_RUN = "FIRST_RUN"
        const val NORMAL_RUN = "NORMAL_RUN"
        const val UPGRADE_RUN = "UPGRADE_RUN"

        const val ACTION_SETTINGS = "ACTION_SETTINGS"
        const val ACTION_RATE = "ACTION_RATE"
        const val ACTION_ABOUT = "ACTION_ABOUT"

        const val ACTION_HOME = "ACTION_HOME"
        const val ACTION_ALL_PLACES = "ACTION_ALL_PLACES"
        const val ACTION_MAP = "ACTION_MAP"
        const val ACTION_SEARCH = "ACTION_SEARCH"
        const val ACTION_FAVORITES = "ACTION_FAVORITES"
        const val ACTION_NOTIFICATIONS = "ACTION_NOTIFICATIONS"
        const val ACTION_PROFILE = "ACTION_PROFILE"
        const val ACTION_FEATURED = "ACTION_FEATURED"
        const val ACTION_SHOPPING = "ACTION_SHOPPING"
        const val ACTION_PHARMACY = "ACTION_PHARMACY"
        const val ACTION_GYM = "ACTION_GYM"
        const val ACTION_FOOD = "ACTION_FOOD"
        const val ACTION_BAR = "ACTION_BAR"
        const val ACTION_FAST_FOOD = "ACTION_FAST_FOOD"
        const val ACTION_DELIVERY = "ACTION_DELIVERY"
        const val ACTION_ICE_CREAM_STORE = "ACTION_ICE_CREAM_STORE"
        const val ACTION_HOTELS = "ACTION_HOTELS"
        const val ACTION_TEMPORARY_RENT = "ACTION_TEMPORARY_RENT"
        const val ACTION_TOUR = "ACTION_TOUR"
        const val ACTION_MONEY = "ACTION_MONEY"
        const val ACTION_BILL_PAYMENTS = "ACTION_BILL_PAYMENTS"
        const val ACTION_APARTMENT_RENTAL = "ACTION_APARTMENT_RENTAL"
        const val ACTION_TAXI = "ACTION_TAXI"
        const val ACTION_GAS_STATION = "ACTION_GAS_STATION"
        const val ACTION_TRANSPORT = "ACTION_TRANSPORT"

        const val ACTION_FLOATING_BUTTON_MAP = "ACTION_FLOATING_BUTTON_MAP"
        const val ACTION_FLOATING_BUTTON_SEARCH = "ACTION_FLOATING_BUTTON_SEARCH"
        const val ACTION_FLOATING_BUTTON_FAVORITES = "ACTION_FLOATING_BUTTON_FAVORITES"

        const val FAVORITES_ADD = "FAVORITES_ADD"
        const val FAVORITES_REMOVE = "FAVORITES_REMOVE"

        const val ACTION_PLACE_PHONE = "ACTION_PLACE_PHONE"
        const val ACTION_PLACE_WEBSITE = "ACTION_PLACE_WEBSITE"
        const val ACTION_PLACE_ADDRESS = "ACTION_PLACE_ADDRESS"

        const val ACTION_PLACE_OPEN_MAP = "ACTION_PLACE_OPEN_MAP"
        const val ACTION_PLACE_NAVIGATE_MAP = "ACTION_PLACE_NAVIGATE_MAP"


        /**
         * --------------------------------Analytics Methods----------------------------------------
         */
        fun logEvent(tag: String, action: String) {
            ThisApplication.getInstance().trackEvent(tag, action, "-")
        }
    }



}