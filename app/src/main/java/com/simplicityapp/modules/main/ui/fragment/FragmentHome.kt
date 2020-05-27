package com.simplicityapp.modules.main.ui.fragment

import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.simplicityapp.R
import com.simplicityapp.base.adapter.AdapterNewsList
import com.simplicityapp.base.adapter.AdapterPlaceGrid
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.connection.RestAdapter
import com.simplicityapp.base.connection.callbacks.CallbackListContentInfo
import com.simplicityapp.base.connection.callbacks.CallbackListPlace
import com.simplicityapp.base.config.AppConfig
import com.simplicityapp.base.config.AppConfig.LIMIT_PLACES_TO_UPDATE
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.config.Constant.LOG_TAG
import com.simplicityapp.base.config.SharedPref
import com.simplicityapp.base.config.ThisApplication
import com.simplicityapp.base.data.database.DatabaseHandler
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.base.utils.UITools
import com.simplicityapp.base.widget.SpacingItemDecoration
import com.simplicityapp.modules.main.ui.ActivityMain
import com.simplicityapp.modules.notifications.model.ContentInfo
import com.simplicityapp.modules.notifications.ui.ActivityNotificationDetails.Companion.navigate
import com.simplicityapp.modules.places.ui.ActivityPlaceDetail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FragmentHome : Fragment() {

    private var count_total: Int = 0
    private var category_id: Int = 0

    private var root_view: View? = null
    private var mainScrollView: ScrollView? = null
    private var recyclerFeatured: RecyclerView? = null
    private var recyclerNews: RecyclerView? = null
    private var tv_featured_title: TextView? = null
    private var tv_news_title: TextView? = null
    private var snackbar_retry: Snackbar? = null
    private var button_share_app: Button? = null
    private var button_home_subscription: Button? = null
    private var btnQuickAccessGastronomy: LinearLayout? = null
    private var btnQuickAccessTaxi: LinearLayout? = null
    private var btnQuickAccessJobs: LinearLayout? = null
    private var btnQuickAccessPharmacy: LinearLayout? = null
    private var btnQuickAccessSearch: LinearLayout? = null
    private var btnQuickAccessFavorites: LinearLayout? = null
    private var btnQuickAccessMap: LinearLayout? = null
    private var btnQuickAccessEmergency: LinearLayout? = null

    private var db: DatabaseHandler? = null
    private var sharedPref: SharedPref? = null
    private var adapterFeatured: AdapterPlaceGrid? = null
    private var adapterNews: AdapterNewsList? = null

    private var shimmerContainer: ShimmerFrameLayout? = null

    private var callbackPlaces: Call<CallbackListPlace>? = null
    private var callbackNews: Call<CallbackListContentInfo>? = null

    private var backToHome = false

    private var featuredOnProcess = false
    private var newsOnProcess = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root_view = inflater.inflate(R.layout.fragment_home, null)
        db = DatabaseHandler(context)
        sharedPref = SharedPref(context)
        category_id = 0

        initUI()

        return root_view
    }

    private fun initUI() {
        setHasOptionsMenu(true)

        mainScrollView = root_view?.findViewById(R.id.main_scroll_view)
        recyclerFeatured = root_view?.findViewById<View>(R.id.recyclerFeatured) as RecyclerView
        recyclerNews = root_view?.findViewById<View>(R.id.recyclerNews) as RecyclerView
        tv_featured_title = root_view?.findViewById(R.id.tv_featured_title)
        tv_news_title = root_view?.findViewById(R.id.tv_news_title)
        button_share_app = root_view?.findViewById(R.id.button_home_share_app)
        button_home_subscription = root_view?.findViewById(R.id.button_home_subscription)
        btnQuickAccessGastronomy = root_view?.findViewById(R.id.lyt_quick_access_gastronomy)
        btnQuickAccessTaxi = root_view?.findViewById(R.id.lyt_quick_access_taxi)
        btnQuickAccessJobs = root_view?.findViewById(R.id.lyt_quick_access_jobs)
        btnQuickAccessPharmacy = root_view?.findViewById(R.id.lyt_quick_access_pharmacy)
        btnQuickAccessSearch = root_view?.findViewById(R.id.lyt_quick_access_search)
        btnQuickAccessFavorites = root_view?.findViewById(R.id.lyt_quick_access_fav)
        btnQuickAccessMap = root_view?.findViewById(R.id.lyt_quick_access_map)
        btnQuickAccessEmergency = root_view?.findViewById(R.id.lyt_quick_access_emergency)

        shimmerContainer = root_view?.findViewById(R.id.shimmer_view_container)
        shimmerContainer?.visibility = View.VISIBLE
        mainScrollView?.visibility = View.GONE

        initRecyclerFeatured()
        initRecyclerNews()

        button_share_app?.setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_ACTION, AnalyticsConstants.SHARE_APP, true)
            activity?.let { it1 -> ActionTools.methodShare(it1) }
        }

        button_home_subscription?.setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_ACTION, AnalyticsConstants.OPEN_REGISTER_FORM, true)
            activity?.let { it1 -> ActionTools.directUrl(it1, Constant.LINK_TO_SUBSCRIPTION_FORM) }
        }

        btnQuickAccessGastronomy?.setOnClickListener {
            backToHome = true
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_DELIVERY)
            ActivityMain.ActivityMainInstance.onItemSelected(R.id.nav_delivery, resources.getString(R.string.title_nav_delivery), false, true)
        }

        btnQuickAccessTaxi?.setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_TAXI)
            ActivityMain.ActivityMainInstance.onItemSelected(R.id.nav_taxi, resources.getString(R.string.title_nav_taxi), false, true)
        }

        btnQuickAccessJobs?.setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_JOBS)
            ActivityMain.ActivityMainInstance.onItemSelected(R.id.nav_jobs, resources.getString(R.string.title_nav_jobs), false, true)
        }

        btnQuickAccessPharmacy?.setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_PHARMACY)
            ActivityMain.ActivityMainInstance.onItemSelected(R.id.nav_pharmacy, resources.getString(R.string.title_nav_pharmacy), false, true)
        }

        btnQuickAccessSearch?.setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_SEARCH)
            ActivityMain.ActivityMainInstance.searchIntent()
        }

        btnQuickAccessFavorites?.setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_FAVORITES)
            ActivityMain.ActivityMainInstance.favoritesIntent()
        }

        btnQuickAccessMap?.setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_MAP)
            ActivityMain.ActivityMainInstance.mapIntent()
        }

        btnQuickAccessEmergency?.setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_EMERGENCY)
            ActivityMain.ActivityMainInstance.onItemSelected(R.id.nav_emergency, resources.getString(R.string.title_nav_emergency), false, true)
        }

        startLoadMoreFeaturedAdapter()
    }

    override fun onDestroyView() {
        if (snackbar_retry != null) snackbar_retry?.dismiss()
        if (callbackPlaces != null && callbackPlaces!!.isExecuted) {
            callbackPlaces?.cancel()
        }
        if (callbackNews != null && callbackNews!!.isExecuted) {
            callbackNews?.cancel()
        }
        super.onDestroyView()
    }

    override fun onStop() {
        if (snackbar_retry != null) snackbar_retry?.dismiss()
        if (callbackPlaces != null && callbackPlaces!!.isExecuted) {
            callbackPlaces?.cancel()
        }
        if (callbackNews != null && callbackNews!!.isExecuted) {
            callbackNews?.cancel()
        }
        super.onStop()
    }

    override fun onResume() {
        adapterFeatured?.notifyDataSetChanged()
        adapterNews?.notifyDataSetChanged()
        refreshNews()
        isLoadComplete(false)
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        if (sharedPref!!.isRefreshPlaces || db?.placesSize!! < LIMIT_PLACES_TO_UPDATE) {
            refreshContent()
            refreshNews()
        } else {
            startLoadMoreFeaturedAdapter()
        }
    }

    private fun isLoadComplete(complete: Boolean) {
        if (complete) {
            if (!featuredOnProcess and !newsOnProcess and (shimmerContainer?.isShimmerStarted == true)) {
                shimmerContainer?.stopShimmer()
                mainScrollView?.visibility = View.VISIBLE
                shimmerContainer?.visibility = View.GONE
            }
        } else {
            if (shimmerContainer?.isShimmerStarted == false) {
                mainScrollView?.visibility = View.GONE
                shimmerContainer?.visibility = View.VISIBLE
                shimmerContainer?.startShimmer()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_category, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_refresh) {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_TOOLBAR_ACTION, AnalyticsConstants.REFRESH)
            refreshContent()
            refreshNews()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getScreenWidth(): Int {
        return Resources.getSystem().getDisplayMetrics().widthPixels
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            shimmerContainer?.startShimmer()
            recyclerFeatured!!.visibility = View.GONE
            recyclerNews?.visibility = View.GONE
        } else {
            if (shimmerContainer?.isShimmerStarted == true) {
                shimmerContainer?.stopShimmer()
            }
            shimmerContainer?.visibility = View.GONE
            mainScrollView?.visibility = View.VISIBLE
            recyclerFeatured!!.visibility = View.VISIBLE
            recyclerNews?.visibility = View.VISIBLE
        }
    }

    private fun refreshContent() {
        ThisApplication.instance?.location = null
        sharedPref?.lastPlacePage = 1
        sharedPref?.isRefreshPlaces = true
        if (snackbar_retry != null) snackbar_retry?.dismiss()
        actionRefreshFeatured(sharedPref!!.lastPlacePage)
    }

    //FEATURED LIST METHODS
    private fun initRecyclerFeatured() {
        recyclerFeatured?.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        recyclerFeatured?.addItemDecoration(SpacingItemDecoration(UITools.getGridSpanCount(activity!!) , UITools.dpToPx(4), true))

        //set data and list adapter
        adapterFeatured = AdapterPlaceGrid(activity, recyclerFeatured, ArrayList(), StaggeredGridLayoutManager.HORIZONTAL, getScreenWidth())
        recyclerFeatured?.adapter = adapterFeatured

        // on item list clicked
        adapterFeatured?.setOnItemClickListener {
                v, obj -> ActivityPlaceDetail.navigate((activity as ActivityMain?)!!, v.findViewById(R.id.lyt_content), obj, AnalyticsConstants.SELECT_HOME_FEATURED_BANNER)
        }
    }

    private fun startLoadMoreFeaturedAdapter() {
        adapterFeatured?.resetListData()
        val items = db?.getPlacesByPage(category_id, Constant.LIMIT_LOADMORE, 0)
        adapterFeatured?.insertData(items, true)
        val item_count = db!!.getPlacesSize(category_id)
        // detect when scroll reach bottom
        adapterFeatured?.setOnLoadMoreListener { current_page ->
            if (item_count > adapterFeatured!!.itemCount && current_page != 0) {
                displayDataByPageFeatured(current_page)
            } else {
                adapterFeatured?.setLoaded()
            }
        }
    }

    private fun displayDataByPageFeatured(next_page: Int) {
        adapterFeatured?.setLoading()
        Handler().postDelayed({
            val items = db?.getPlacesByPage(category_id, Constant.LIMIT_LOADMORE, next_page * Constant.LIMIT_LOADMORE)
            adapterFeatured?.insertData(items, false)
        }, 500)
    }

    // checking some condition before perform refresh data
    private fun actionRefreshFeatured(page_no: Int) {
        val conn = Tools.checkConnection(context!!)
        if (conn) {
            if (!featuredOnProcess) {
                onRefreshFeatured(page_no)
            } else {
                Snackbar.make(root_view!!, R.string.task_running, Snackbar.LENGTH_SHORT).show()
            }
        } else {
            onFailureRetryFeatured(page_no, getString(R.string.no_internet))
        }
    }

    private fun onRefreshFeatured(page_no: Int) {
        featuredOnProcess = true
        isLoadComplete(false)
        val isDraft = if (AppConfig.LAZY_LOAD) 1 else 0
        callbackPlaces = RestAdapter.createAPI().getPlacesByPage(page_no, Constant.LIMIT_PLACE_REQUEST, isDraft)
        callbackPlaces!!.enqueue(object : retrofit2.Callback<CallbackListPlace> {
            override fun onResponse(call: Call<CallbackListPlace>, response: Response<CallbackListPlace>) {
                val resp = response.body()
                if (resp != null) {
                    count_total = resp.count_total
                    if (page_no == 1) db!!.refreshTablePlace()
                    db!!.insertListPlace(resp.places)  // save result into database
                    sharedPref!!.lastPlacePage = page_no + 1
                    delayNextRequestFeatured(page_no)
                } else {
                    onFailureRetryFeatured(page_no, getString(R.string.refresh_failed))
                }
            }

            override fun onFailure(call: Call<CallbackListPlace>?, t: Throwable) {
                if (call != null && !call.isCanceled) {
                    Log.e(LOG_TAG, "FragmentHome - onFailire ${t.message}")
                    val conn = Tools.checkConnection(context!!)
                    if (conn) {
                        onFailureRetryFeatured(page_no, getString(R.string.refresh_failed))
                    } else {
                        onFailureRetryFeatured(page_no, getString(R.string.no_internet))
                    }
                }
            }
        })
    }

    private fun onFailureRetryFeatured(page_no: Int, msg: String) {
        featuredOnProcess = false
        isLoadComplete(true)
        startLoadMoreFeaturedAdapter()
        snackbar_retry = Snackbar.make(root_view!!, msg, Snackbar.LENGTH_INDEFINITE)
        snackbar_retry!!.setAction(R.string.RETRY) { actionRefreshFeatured(page_no) }
        snackbar_retry!!.show()
    }

    private fun delayNextRequestFeatured(page_no: Int) {
        if (count_total == 0) {
            onFailureRetryFeatured(page_no, getString(R.string.refresh_failed))
            return
        }
        if (page_no * Constant.LIMIT_PLACE_REQUEST > count_total) { // when all data loaded
            featuredOnProcess = false
            isLoadComplete(true)
            startLoadMoreFeaturedAdapter()
            sharedPref!!.isRefreshPlaces = false
            root_view?.let { Snackbar.make(it, R.string.load_success, Snackbar.LENGTH_LONG).show() }
            return
        }
        Handler().postDelayed({ onRefreshFeatured(page_no + 1) }, 500)
    }

    //NEWS LIST METHODS

    private var post_total = 0

    // can be, ONLINE or OFFLINE
    private var MODE = "ONLINE"


    private fun initRecyclerNews() {
        recyclerNews?.layoutManager = LinearLayoutManager(context)
        recyclerNews?.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        //set data and list adapter
        adapterNews = AdapterNewsList(activity, recyclerNews, ArrayList())
        recyclerNews?.adapter = adapterNews

        // on item list clicked
        adapterNews?.setOnItemClickListener(AdapterNewsList.OnItemClickListener { v, obj, position ->
            navigate(
                activity!!,
                obj,
                false,
                AnalyticsConstants.SELECT_HOME_NEWS
            )
        })

        // detect when scroll reach bottom
        adapterNews?.setOnLoadMoreListener {
            (AdapterNewsList.OnLoadMoreListener { current_page ->
                if (post_total > adapterNews!!.getItemCount() && current_page != 0) {
                    val next_page = current_page + 1
                    requestActionNews(next_page)
                } else {
                    adapterNews?.setLoaded()
                }
            })
        }
    }

    fun requestActionNews(page_no: Int) {
        if (page_no == 1) {
            isLoadComplete(false)
        } else {
            adapterNews?.setLoading()
        }
        Handler().postDelayed(
            { requestListNews(page_no) },
            if (MODE == "OFFLINE") 50 else 1000.toLong()
        )
    }

    private fun requestListNews(page_no: Int) {
        if (MODE == "ONLINE") {
            val api = RestAdapter.createAPI()
            callbackNews = api.getContentInfoByPage(
                page_no,
                Constant.LIMIT_NEWS_REQUEST
            )
            callbackNews?.enqueue(object : Callback<CallbackListContentInfo?> {
                override fun onResponse(
                    call: Call<CallbackListContentInfo?>,
                    response: Response<CallbackListContentInfo?>
                ) {
                    val resp = response.body()
                    if (resp != null && resp.status == "success") {
                        if (page_no == 1) {
                            adapterNews?.resetListData()
                            db!!.refreshTableContentInfo()
                        }
                        post_total = resp.count_total
                        db!!.insertListContentInfo(resp.news_infos)
                        displayApiResult(resp.news_infos)
                    } else {
                        onFailRequestNews(page_no)
                    }
                }

                override fun onFailure(
                    call: Call<CallbackListContentInfo?>,
                    t: Throwable
                ) {
                    if (!call.isCanceled) onFailRequestNews(page_no)
                }
            })
        } else {
            if (page_no == 1) adapterNews?.resetListData()
            val limit = Constant.LIMIT_NEWS_REQUEST
            val offset = page_no * limit - limit
            post_total = db!!.contentInfoSize
            val items =
                db!!.getContentInfoByPage(limit, offset)
            displayApiResult(items)
        }
    }

    private fun displayApiResult(items: List<ContentInfo>) {
        adapterNews?.insertData(items)
        newsOnProcess = false
        isLoadComplete(true)
        showNews(true)
        if (items.size == 0) {
            showNews(false)
        }
    }

    private fun onFailRequestNews(page_no: Int) {
        adapterNews?.setLoaded()
        newsOnProcess = false
        showNews(false)
    }

    private fun refreshNews() {
        if (callbackNews != null && callbackNews!!.isExecuted) callbackNews!!.cancel()
        newsOnProcess = false
        showNews(false)
        MODE = "ONLINE"
        post_total = 0
        requestActionNews(1)
    }

    private fun showNews(show: Boolean) {
        if (show) {
            tv_news_title?.visibility = View.VISIBLE
            recyclerNews?.visibility = View.VISIBLE
        } else {
            tv_news_title?.visibility = View.GONE
            recyclerNews?.visibility = View.GONE
        }
    }



    companion object {
        var TAG_CATEGORY = "key.TAG_CATEGORY"
    }

}
