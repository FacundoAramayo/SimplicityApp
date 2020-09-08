package com.simplicityapp.modules.settings.model

import java.io.Serializable

data class DeviceInfo (
        var device: String? = null,
        var email: String? = null,
        var name: String? = null,
        var version: String? = null,
        var regid: String? = null,
        var date_create: Long = 0
    ): Serializable
