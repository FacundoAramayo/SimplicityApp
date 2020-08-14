package com.simplicityapp.base.config

import android.app.Application
import android.location.Location
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SHARE
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SIGN_UP
import com.simplicityapp.base.persistence.preferences.SharedPref

class ThisApplication : Application() {

    private var firebaseAnalytics: FirebaseAnalytics? = null
    /**
     * ---------------------------------------- End of analytics ---------------------------------
     */

    var location: Location? = null
    private var sharedPref: SharedPref? = null
    var firebaseApp: FirebaseApp? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedPref = SharedPref(this)

        // activate analytics tracker
        getFirebaseAnalytics()
    }

    /*
     * --------------------------------------------------------------------------------------------
     * For Google Analytics
     */
    private fun putGoogleUserInformation(params: Bundle) {
        val acct = GoogleSignIn.getLastSignedInAccount(applicationContext)
        if (acct != null) {
            params.putString("personName", acct.displayName)
            params.putString("personEmail", acct.email)
            params.putString("personId", acct.id)
            params.putString("personPhoto", acct.photoUrl?.toString())
        }
    }

    private fun putUserEmail(params: Bundle) {
        val acct = GoogleSignIn.getLastSignedInAccount(applicationContext)
        if (acct != null) {
            params.putString("personEmail", acct.email)
        }
    }

    @Synchronized
    fun getFirebaseAnalytics(): FirebaseAnalytics? {
        if (firebaseAnalytics == null && com.simplicityapp.BuildConfig.ANALYTICS_ENABLED) {
            // Obtain the Firebase Analytics.
            firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        }
        return firebaseAnalytics
    }

    fun trackEvent(event: String,
                   label: String?,
                   user: Boolean?) {
        if (firebaseAnalytics != null) {
            val params = Bundle()

            label?.let {
                it.replace("[^A-Za-z0-9_]", "")
                params.putString("label", it)
            }

            if (user == true) {
                putGoogleUserInformation(params)
            }

            firebaseAnalytics?.logEvent(event, params)
        }
    }

    fun trackAnalyticsSignUp() {
        val bundle = Bundle()
        putGoogleUserInformation(bundle)
        firebaseAnalytics?.logEvent(SIGN_UP, bundle)
    }

    fun logAnalyticsShare(contentType: String, item: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, item)
        firebaseAnalytics?.logEvent(SHARE, bundle)
    }

    companion object {
        @get:Synchronized
        var instance: ThisApplication? = null
            private set
    }
}
