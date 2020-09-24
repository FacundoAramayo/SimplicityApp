package com.simplicityapp.modules.notifications.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.simplicityapp.R
import com.simplicityapp.base.BaseActivity
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.logAnalyticsEvent
import com.simplicityapp.base.utils.ActionTools.Companion.aboutAction
import com.simplicityapp.base.utils.ActionTools.Companion.rateAction
import com.simplicityapp.base.utils.Tools.Companion.checkConnection
import com.simplicityapp.baseui.adapter.AdapterNews
import com.simplicityapp.baseui.decorator.SpacingItemDecoration
import com.simplicityapp.baseui.utils.UITools.Companion.dpToPx
import com.simplicityapp.databinding.ActivityNotificationsBinding
import com.simplicityapp.modules.notifications.activity.ActivityNotificationDetails.Companion.navigate
import com.simplicityapp.modules.notifications.model.News
import com.simplicityapp.modules.notifications.model.NewsResponse
import com.simplicityapp.modules.notifications.repository.NewsRepository
import com.simplicityapp.modules.settings.activity.ActivitySetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ActivityNotificationsV2 : BaseActivity() {
    
    private lateinit var binding: ActivityNotificationsBinding
    private var actionBar: ActionBar? = null
    private var parentView: View? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: AdapterNews? = null
    private var lytProgress: View? = null
    private var newsPostTotal = 0
    private var failed_page = 0
    private var snackbarRetry: Snackbar? = null
    // can be, ONLINE or OFFLINE
    private var MODE = "ONLINE"

    private val newsRepository: NewsRepository = NewsRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.enter_slide_in, R.anim.enter_slide_out)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        initActivity(binding)
        parentView = findViewById(android.R.id.content)
        initComponent()
    }

    override fun initUI() {
        initToolbar()
        lytProgress = findViewById(R.id.lyt_progress)
        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.addItemDecoration(SpacingItemDecoration(1, dpToPx(4), true))
        mAdapter = AdapterNews(this, recyclerView, ArrayList())
        recyclerView?.adapter = mAdapter
    }

    private fun initToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setTitle(R.string.title_nav_news)
    }

    private fun initComponent() {
        mAdapter?.setOnItemClickListener { v, obj, position ->
            navigate(
                this@ActivityNotificationsV2,
                obj,
                false,
                AnalyticsConstants.SELECT_NOTIFICATION_OPEN_LIST_ITEM
            )
        }
        // detect when scroll reach bottom
        mAdapter?.setOnLoadMoreListener { current_page ->
            if (newsPostTotal > mAdapter!!.itemCount && current_page != 0) {
                val next_page = current_page + 1
                requestAction(next_page)
            } else {
                mAdapter!!.setLoaded()
            }
        }
        // if already have data news at db, use mode OFFLINE
        db?.contentInfoSize?.let {
            if (it > 0) MODE = "OFFLINE"
        }
        requestAction(1)
    }

    private fun displayNewsResult(items: List<News>?) {
        items?.let {
            mAdapter?.insertData(it)
            showProgress(false)
            if (it.isEmpty()) {
                showNoItemView(true)
            }
        }
    }

    private fun requestListContentInfo(page_no: Int) {
        if (MODE == "ONLINE") {
            GlobalScope.launch(Dispatchers.Main) {
                val response = newsRepository.getNews(page_no, Constant.LIMIT_NEWS_REQUEST)?.body()
                response?.let {
                    if (it.status == Constant.SUCCESS_RESPONSE) {
                        val news = filterUserNews(it)
                        if (page_no == 1) {
                            mAdapter?.resetListData()
                            db?.refreshTableContentInfo()
                        }
                        newsPostTotal = it.countTotal
                        db?.insertListContentInfo(news)
                        displayNewsResult(news)
                    } else {
                        onFailRequest(page_no)
                    }
                } ?: onFailRequest(page_no)
            }
        } else {
            if (page_no == 1) mAdapter!!.resetListData()
            val limit = Constant.LIMIT_NEWS_REQUEST
            val offset = page_no * limit - limit
            newsPostTotal = db?.contentInfoSize ?: 0
            val items = db?.getContentInfoByPage(limit, offset)
            displayNewsResult(items)
        }
    }

    private fun filterUserNews(newsResponse: NewsResponse): List<News> {
        val newsFiltered = mutableListOf<News>()
        val regId = sharedPref.fcmRegId
        newsResponse.newsList.forEach {
            if (it.reg_id.isNullOrEmpty() or (it.reg_id == regId)) {
                newsFiltered.add(it)
            }
        }
        return newsFiltered.toList()
    }

    private fun onFailRequest(page_no: Int) {
        failed_page = page_no
        mAdapter!!.setLoaded()
        showProgress(false)
        if (checkConnection(this)) {
            showFailedView(true, getString(R.string.refresh_failed))
        } else {
            showFailedView(true, getString(R.string.no_internet))
        }
    }

    private fun requestAction(page_no: Int) {
        showFailedView(false, "")
        showNoItemView(false)
        if (page_no == 1) {
            showProgress(true)
        } else {
            mAdapter?.setLoading()
        }
        Handler().postDelayed(
            { requestListContentInfo(page_no) },
            if (MODE == "OFFLINE") 50 else 1000.toLong()
        )
    }

    override fun onResume() {
        if (checkConnection(this)) {
            refreshNotifications()
        }
        super.onResume()
    }

    public override fun onStop() {
        super.onStop()
        showProgress(false)
    }

    private fun showFailedView(show: Boolean, message: String) {
        if (snackbarRetry == null) {
            parentView?.let {
                snackbarRetry = Snackbar.make(it, "", Snackbar.LENGTH_INDEFINITE)
            }
        }
        snackbarRetry?.setText(message)
        snackbarRetry?.setAction(R.string.RETRY) { requestAction(failed_page) }
        if (show) {
            snackbarRetry?.show()
        } else {
            snackbarRetry?.dismiss()
        }
    }

    private fun showNoItemView(show: Boolean) {
        val lytNoItem = findViewById<View>(R.id.lyt_no_item)
        if (show) {
            recyclerView!!.visibility = View.GONE
            lytNoItem.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            lytNoItem.visibility = View.GONE
        }
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            lytProgress!!.visibility = View.VISIBLE
        } else {
            lytProgress!!.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_notifications, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.action_refresh -> {
                logAnalyticsEvent(
                    AnalyticsConstants.SELECT_NOTIFICATION_ACTION,
                    AnalyticsConstants.REFRESH_LIST,
                    false
                )
                refreshNotifications()
            }
            R.id.action_settings -> {
                val i = Intent(applicationContext, ActivitySetting::class.java)
                startActivity(i)
            }
            R.id.action_rate -> {
                rateAction(this@ActivityNotificationsV2)
            }
            R.id.action_about -> {
                aboutAction(this@ActivityNotificationsV2)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshNotifications() {
        showFailedView(false, "")
        MODE = "ONLINE"
        newsPostTotal = 0
        requestAction(1)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.exit_slide_in, R.anim.exit_slide_out)
    }
}