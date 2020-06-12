package com.simplicityapp.base.utils

import java.lang.Exception

inline fun <T> tryOrDefault(f: () -> T, defaultValue: T): T {
    return try {
        f()
    } catch (e: Exception) {
        e.printStackTrace()
        defaultValue
    }
}