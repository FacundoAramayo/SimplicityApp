package com.simplicityapp.modules.start.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.APP_OPEN
import com.simplicityapp.base.config.AppConfig
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.persistence.preferences.SharedPref
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.BuildConfig
import com.simplicityapp.modules.main.activity.ActivityMain
import com.simplicityapp.R
import com.simplicityapp.modules.main.activity.RegionSelectorActivity

class ActivitySplash : AppCompatActivity() {

    private var sharedPref: SharedPref? = null
    private lateinit var instance: ActivitySplash

    private var animationDrawable: AnimationDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        instance = this

        sharedPref = SharedPref(this)

        chooseNextActivity()
        startUI()
    }

    private fun startUI() {
        val mainLayout = findViewById<RelativeLayout>(R.id.main_layout)
        startBackgroundAnimation(mainLayout)
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
        animationDrawable?.stop()
        ActionTools.startActivityWithDelay(ActivitySplashInstance,this@ActivitySplash, i)
        AnalyticsConstants.logAnalyticsEvent(APP_OPEN, AnalyticsConstants.FIRST_RUN)
    }

    private fun startActivityMainDelay() {
        val i = Intent(this@ActivitySplash, ActivityMain::class.java)
        animationDrawable?.stop()
        ActionTools.startActivityWithDelay(ActivitySplashInstance,this@ActivitySplash, i)
        AnalyticsConstants.logAnalyticsEvent(APP_OPEN, AnalyticsConstants.NORMAL_RUN)
    }

    private fun startActivityUpgradeDelay() {
        val i = if (sharedPref?.regionId == -1) {
            Intent(this@ActivitySplash, ActivityMain::class.java)
        } else {
            Intent(this@ActivitySplash, RegionSelectorActivity::class.java)
        }
        animationDrawable?.stop()
        ActionTools.startActivityWithDelay(ActivitySplashInstance,this@ActivitySplash, i)
        AnalyticsConstants.logAnalyticsEvent(APP_OPEN, AnalyticsConstants.UPGRADE_RUN)
    }

    private fun startBackgroundAnimation(mainLayout: RelativeLayout) {
        animationDrawable = mainLayout.background as AnimationDrawable
        animationDrawable?.setExitFadeDuration(900)
        animationDrawable?.start()
    }

    val ActivitySplashInstance: ActivitySplash
        get() {
            if (instance == null) {
                instance = ActivitySplash()
            }

            return instance
        }

}
