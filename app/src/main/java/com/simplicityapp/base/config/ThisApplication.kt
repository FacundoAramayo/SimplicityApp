package com.simplicityapp.base.config

import android.app.Application
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SHARE
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SIGN_UP

import com.simplicityapp.base.connection.RestAdapter
import com.simplicityapp.base.connection.callbacks.CallbackDevice
import com.simplicityapp.base.config.Constant.LOG_TAG
import com.simplicityapp.base.utils.Tools
import retrofit2.Call
import retrofit2.Response

class ThisApplication : Application() {

    private var callback: Call<CallbackDevice>? = null
    private var firebaseAnalytics: FirebaseAnalytics? = null
    /**
     * ---------------------------------------- End of analytics ---------------------------------
     */

    var location: Location? = null
    private var sharedPref: SharedPref? = null
    private var fcm_count = 0
    private val FCM_MAX_COUNT = 10

    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedPref = SharedPref(this)

        // initialize firebase
        val firebaseApp = FirebaseApp.initializeApp(this)

        // obtain regId & registering device to server
        obtainFirebaseToken(firebaseApp)

        // activate analytics tracker
        getFirebaseAnalytics()
    }


    private fun obtainFirebaseToken(firebaseApp: FirebaseApp?) {
        if (!Tools.checkConnection(this)) return
        fcm_count++

        val resultTask = FirebaseInstanceId.getInstance().instanceId
        resultTask.addOnSuccessListener { instanceIdResult ->
            val regId = instanceIdResult.token
            if (!TextUtils.isEmpty(regId)) sendRegistrationToServer(regId)
        }

        resultTask.addOnFailureListener(OnFailureListener { e ->
            Log.e(LOG_TAG, "Constant.LOG_TAG, Failed obtain fcmID : " + e.message)
            if (fcm_count > FCM_MAX_COUNT) return@OnFailureListener
            obtainFirebaseToken(firebaseApp)
        })
    }

    /**
     * --------------------------------------------------------------------------------------------
     * For Firebase Cloud Messaging
     */
    private fun sendRegistrationToServer(token: String) {
        if (Tools.checkConnection(this) && !TextUtils.isEmpty(token) && sharedPref!!.isOpenAppCounterReach) {
            val api = RestAdapter.createAPI()
            val deviceInfo = Tools.getDeviceInfo(this)
            deviceInfo.regid = token

            callback = api.registerDevice(deviceInfo)
            callback!!.enqueue(object : retrofit2.Callback<CallbackDevice> {
                override fun onResponse(
                    call: Call<CallbackDevice>,
                    response: Response<CallbackDevice>
                ) {
                    val resp = response.body()
                    if (resp != null) {
                        if (resp.status == "success") {
                            sharedPref!!.setOpenAppCounter(0)
                        }
                    }
                }

                override fun onFailure(call: Call<CallbackDevice>, t: Throwable) {}
            })
        }
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
