package com.simplicityapp.modules.notifications.ui

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

import java.util.ArrayList

import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.base.utils.UITools
import com.simplicityapp.modules.settings.ui.ActivityFullScreenImage
import com.simplicityapp.base.config.Constant
import com.simplicityapp.modules.main.ui.ActivityMain
import com.simplicityapp.modules.start.ui.ActivitySplash
import com.simplicityapp.modules.notifications.model.ContentInfo
import com.simplicityapp.R
import com.simplicityapp.base.config.analytics.AnalyticsConstants

class ActivityNotificationDetails : AppCompatActivity() {

    private var from_notif: Boolean? = null

    // extra obj
    private var contentInfo: ContentInfo? = null

    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
    private var parent_view: View? = null
    private var webview: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.enter_slide_in, R.anim.enter_slide_out)
        setContentView(R.layout.activity_notifications_details)

        contentInfo = intent.getSerializableExtra(EXTRA_OBJECT) as ContentInfo
        from_notif = intent.getBooleanExtra(EXTRA_FROM_NOTIF, false)

        initComponent()
        initToolbar()
        displayData()

        // analytics tracking
        AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.VIEW_NOTIFICATION, contentInfo?.title.orEmpty())
    }

    private fun initComponent() {
        parent_view = findViewById(android.R.id.content)
    }

    private fun initToolbar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        actionBar!!.title = Html.fromHtml(contentInfo!!.title)
    }

    private fun displayData() {

        webview = findViewById<View>(R.id.content) as WebView
        var html_data = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> "
        html_data += contentInfo!!.full_content
        webview!!.settings.javaScriptEnabled = true
        webview!!.settings
        webview!!.settings.builtInZoomControls = true
        webview!!.setBackgroundColor(Color.TRANSPARENT)
        webview!!.webChromeClient = WebChromeClient()
        webview!!.loadData(html_data, "text/html; charset=UTF-8", null)

        // disable scroll on touch
        webview!!.setOnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE }

        (findViewById<View>(R.id.date) as TextView).text =
            Tools.getFormattedDate(contentInfo!!.last_update)
        UITools.displayImage(
            this,
            findViewById<View>(R.id.image) as ImageView,
            Constant.getURLimgNews(contentInfo!!.image)
        )

        (findViewById<View>(R.id.lyt_image) as MaterialRippleLayout).setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(
                AnalyticsConstants.SELECT_NOTIFICATION_OPEN_PHOTO, contentInfo?.image
            )
            val images_list = ArrayList<String>()
            images_list.add(Constant.getURLimgNews(contentInfo!!.image))
            val i = Intent(this@ActivityNotificationDetails, ActivityFullScreenImage::class.java)
            i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, images_list)
            startActivity(i)
        }
    }

    override fun onPause() {
        super.onPause()
        if (webview != null) webview!!.onPause()
    }

    override fun onResume() {
        if (webview != null) webview!!.onResume()
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
                AnalyticsConstants.SELECT_NOTIFICATION_ITEM_SHARE, contentInfo?.title
            )
            AnalyticsConstants.logAnalyticsShare(
                AnalyticsConstants.CONTENT_NOTIFICATION,
                contentInfo?.title.orEmpty()
            )
            ActionTools.methodShareNews(this, contentInfo!!)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        onBackAction()
    }

    private fun onBackAction() {
        if (from_notif!!) {
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
        fun navigate(activity: Activity, obj: ContentInfo, from_notif: Boolean?, analyticsEvent: String) {
            AnalyticsConstants.logAnalyticsEvent(analyticsEvent, obj.title
            )
            val i = navigateBase(activity, obj, from_notif)
            activity.startActivity(i)
        }

        fun navigateBase(context: Context, obj: ContentInfo, from_notif: Boolean?): Intent {
            val i = Intent(context, ActivityNotificationDetails::class.java)
            i.putExtra(EXTRA_OBJECT, obj)
            i.putExtra(EXTRA_FROM_NOTIF, from_notif)
            return i
        }
    }

}
