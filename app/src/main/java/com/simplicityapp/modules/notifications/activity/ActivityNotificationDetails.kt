package com.simplicityapp.modules.notifications.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.balysv.materialripple.MaterialRippleLayout
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.baseui.utils.UITools
import com.simplicityapp.base.config.Constant
import com.simplicityapp.modules.main.activity.ActivityMain
import com.simplicityapp.modules.places.activity.ActivityFullScreenImage
import com.simplicityapp.modules.start.activity.ActivitySplash
import com.simplicityapp.R
import com.simplicityapp.modules.notifications.model.News
import java.util.ArrayList

class ActivityNotificationDetails : AppCompatActivity() {

    private var fromNotif: Boolean? = null

    // extra obj
    private var news: News? = null

    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
    private var parentView: View? = null
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.enter_slide_in, R.anim.enter_slide_out)
        setContentView(R.layout.activity_notifications_details)

        news = intent.getSerializableExtra(EXTRA_OBJECT) as News
        fromNotif = intent.getBooleanExtra(EXTRA_FROM_NOTIF, false)

        initComponent()
        initToolbar()
        displayData()

        // analytics tracking
        AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.VIEW_NOTIFICATION, news?.title.orEmpty())
    }

    private fun initComponent() {
        parentView = findViewById(android.R.id.content)
    }

    private fun initToolbar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        actionBar!!.title = Html.fromHtml(news!!.title)
    }

    private fun displayData() {

        webView = findViewById<View>(R.id.content) as WebView
        var html_data = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> "
        html_data += news!!.full_content
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings
        webView!!.settings.builtInZoomControls = true
        webView!!.setBackgroundColor(Color.TRANSPARENT)
        webView!!.webChromeClient = WebChromeClient()
        webView!!.loadData(html_data, "text/html; charset=UTF-8", null)

        // disable scroll on touch
        webView!!.setOnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE }

        (findViewById<View>(R.id.date) as TextView).text =
            Tools.getFormattedDate(news!!.last_update)
        UITools.displayImage(
            this,
            findViewById<View>(R.id.image) as ImageView,
            Constant.getURLimgNews(news!!.image)
        )

        (findViewById<View>(R.id.lyt_image) as MaterialRippleLayout).setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(
                AnalyticsConstants.SELECT_NOTIFICATION_OPEN_PHOTO, news?.image
            )
            val images_list = ArrayList<String>()
            images_list.add(Constant.getURLimgNews(news!!.image))
            val i = Intent(this@ActivityNotificationDetails, ActivityFullScreenImage::class.java)
            i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, images_list)
            startActivity(i)
        }
    }

    override fun onPause() {
        super.onPause()
        if (webView != null) webView!!.onPause()
    }

    override fun onResume() {
        if (webView != null) webView!!.onResume()
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_notification_details, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackAction()
            return true
        } else if (id == R.id.action_share) {
            AnalyticsConstants.logAnalyticsEvent(
                AnalyticsConstants.SELECT_NOTIFICATION_ITEM_SHARE, news?.title
            )
            AnalyticsConstants.logAnalyticsShare(
                AnalyticsConstants.CONTENT_NOTIFICATION,
                news?.title.orEmpty()
            )
            ActionTools.methodShareNews(this, news!!)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        onBackAction()
    }

    private fun onBackAction() {
        if (fromNotif!!) {
            if (ActivityMain.active) {
                finish()
            } else {
                val intent = Intent(applicationContext, ActivitySplash::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        } else {
            super.onBackPressed()
            overridePendingTransition(R.anim.exit_slide_in, R.anim.exit_slide_out)
        }
    }

    companion object {

        private val EXTRA_OBJECT = "key.EXTRA_OBJECT"
        private val EXTRA_FROM_NOTIF = "key.EXTRA_FROM_NOTIF"
        private val SCREEN_NAME = "NOTIFICATIONS_DETAILS"

        // activity transition
        fun navigate(activity: Activity, obj: News, from_notif: Boolean?, analyticsEvent: String) {
            AnalyticsConstants.logAnalyticsEvent(analyticsEvent, obj.title
            )
            val i =
                navigateBase(
                    activity,
                    obj,
                    from_notif
                )
            activity.startActivity(i)
        }

        fun navigateBase(context: Context, obj: News, fromNotif: Boolean?): Intent {
            val i = Intent(context, ActivityNotificationDetails::class.java)
            i.putExtra(EXTRA_OBJECT, obj)
            i.putExtra(EXTRA_FROM_NOTIF, fromNotif)
            return i
        }
    }

}
