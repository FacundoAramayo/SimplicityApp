package com.simplicityapp.modules.start.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import com.simplicityapp.BuildConfig
import com.simplicityapp.R
import com.simplicityapp.base.analytics.AnalyticsConstants
import com.simplicityapp.base.data.AppConfig
import com.simplicityapp.base.data.Constant
import com.simplicityapp.base.data.SharedPref
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.base.utils.UITools
import com.simplicityapp.modules.main.ui.ActivityMain

class ActivitySplash : AppCompatActivity() {

    private var sharedPref: SharedPref? = null
    private lateinit var instance: ActivitySplash

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        instance = this

        sharedPref = SharedPref(this)

        chooseNextActivity()
    }

    private fun chooseNextActivity() {
        when (checkFirstRun()) {
            AppConfig.FIRST_RUN -> startActivityWelcomeDelay()
            AppConfig.NORMAL_RUN -> startActivityMainDelay()
            AppConfig.UPGRADE_RUN -> startActivityUpgradeDelay()
        }
    }

    private fun checkFirstRun(): Int {
        val prefs = getSharedPreferences(Constant.PREFS_NAME, Context.MODE_PRIVATE)
        val savedVersionCode = prefs.getInt(Constant.PREF_VERSION_CODE_KEY, Constant.DOESNT_EXIST_CODE)

        return when {
            BuildConfig.VERSION_CODE == savedVersionCode -> AppConfig.NORMAL_RUN
            savedVersionCode == Constant.DOESNT_EXIST_CODE -> AppConfig.FIRST_RUN
            BuildConfig.VERSION_CODE > savedVersionCode -> AppConfig.UPGRADE_RUN
            else -> 400
        }

    }

    private fun startActivityWelcomeDelay() {
        val i = Intent(this@ActivitySplash, ActivityWelcome::class.java)
        ActionTools.startActivityWithDelay(ActivitySplashInstance,this@ActivitySplash, i)
        AnalyticsConstants.logEvent(AnalyticsConstants.OPEN_APP_TYPE, AnalyticsConstants.FIRST_RUN)
    }

    private fun startActivityMainDelay() {
        val i = Intent(this@ActivitySplash, ActivityMain::class.java)
        ActionTools.startActivityWithDelay(ActivitySplashInstance,this@ActivitySplash, i)
        AnalyticsConstants.logEvent(AnalyticsConstants.OPEN_APP_TYPE, AnalyticsConstants.NORMAL_RUN)
    }

    private fun startActivityUpgradeDelay() {
        val i = Intent(this@ActivitySplash, ActivityMain::class.java)
        ActionTools.startActivityWithDelay(ActivitySplashInstance,this@ActivitySplash, i)
        AnalyticsConstants.logEvent(AnalyticsConstants.OPEN_APP_TYPE, AnalyticsConstants.UPGRADE_RUN)
    }

    val ActivitySplashInstance: ActivitySplash
        get() {
            if (instance == null) {
                instance = ActivitySplash()
            }

            return instance
        }

}
