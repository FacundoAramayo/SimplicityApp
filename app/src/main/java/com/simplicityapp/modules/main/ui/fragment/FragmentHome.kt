package com.simplicityapp.modules.main.ui.fragment

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar

import com.simplicityapp.R
import com.simplicityapp.base.adapter.AdapterPlaceGrid
import com.simplicityapp.base.analytics.AnalyticsConstants
import com.simplicityapp.base.connection.RestAdapter
import com.simplicityapp.base.connection.callbacks.CallbackListPlace
import com.simplicityapp.base.data.AppConfig
import com.simplicityapp.base.data.AppConfig.LIMIT_PLACES_TO_UPDATE
import com.simplicityapp.base.data.Constant
import com.simplicityapp.base.data.Constant.LOG_TAG
import com.simplicityapp.base.data.SharedPref
import com.simplicityapp.base.data.ThisApplication
import com.simplicityapp.base.data.database.DatabaseHandler
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.base.utils.UITools
import com.simplicityapp.base.widget.SpacingItemDecoration
import com.simplicityapp.modules.main.ui.ActivityMain
import com.simplicityapp.modules.places.ui.ActivityPlaceDetail
import retrofit2.Call
import retrofit2.Response
import java.util.ArrayList

class FragmentHome : Fragment() {

    private var count_total: Int = 0
    private var category_id: Int = 0

    private var root_view: View? = null
    private var recyclerView: RecyclerView? = null
    private var lyt_progress: View? = null
    private var lyt_title_animated_background: LinearLayout? = null
    private var text_progress: TextView? = null
    private var snackbar_retry: Snackbar? = null
    private var button_share_app: Button? = null
    private var button_home_subscription: Button? = null

    private var db: DatabaseHandler? = null
    private var sharedPref: SharedPref? = null
    private var adapter: AdapterPlaceGrid? = null

    private var callback: Call<CallbackListPlace>? = null

    private var onProcess = false

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

        recyclerView = root_view?.findViewById<View>(R.id.recycler) as RecyclerView
        lyt_progress = root_view?.findViewById(R.id.lyt_progress)
        lyt_title_animated_background = root_view?.findViewById(R.id.lyt_title_animated_background)
        text_progress = root_view?.findViewById<View>(R.id.text_progress) as TextView
        button_share_app = root_view?.findViewById(R.id.button_home_share_app)
        button_home_subscription = root_view?.findViewById(R.id.button_home_subscription)

        recyclerView?.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        recyclerView?.addItemDecoration(SpacingItemDecoration(UITools.getGridSpanCount(activity!!) , UITools.dpToPx(activity!!, 4), true))

        //set data and list adapter
        adapter = AdapterPlaceGrid(activity, recyclerView, ArrayList(), StaggeredGridLayoutManager.HORIZONTAL, getScreenWidth())
        recyclerView?.adapter = adapter

        // on item list clicked
        adapter?.setOnItemClickListener {
                v, obj -> ActivityPlaceDetail.navigate((activity as ActivityMain?)!!, v.findViewById(R.id.lyt_content), obj, AnalyticsConstants.SELECT_HOME_FEATURED_BANNER)
        }

        button_share_app?.setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_SHARE_APP, user = true, fullUser = false)
            activity?.let { it1 -> ActionTools.methodShare(it1) }
        }

        button_home_subscription?.setOnClickListener {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_OPEN_REGISTER_FORM, user = true, fullUser = true)
            activity?.let { it1 -> ActionTools.directUrl(it1, Constant.LINK_TO_SUBSCRIPTION_FORM) }
        }

        startAnimationTitle()
        startLoadMoreAdapter()
    }

    private fun startAnimationTitle() {
        val animationDrawable: AnimationDrawable = lyt_title_animated_background?.background as AnimationDrawable
        animationDrawable.setExitFadeDuration(3500)
        animationDrawable.start()
    }

    override fun onDestroyView() {
        if (snackbar_retry != null) snackbar_retry?.dismiss()
        if (callback != null && callback!!.isExecuted) {
            callback?.cancel()
        }
        super.onDestroyView()
    }

    override fun onResume() {
        adapter?.notifyDataSetChanged()
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        if (sharedPref!!.isRefreshPlaces || db?.placesSize!! < LIMIT_PLACES_TO_UPDATE) {
            refreshContent()
        } else {
            startLoadMoreAdapter()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_category, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun refreshContent() {
        ThisApplication.instance?.location = null
        sharedPref?.lastPlacePage = 1
        sharedPref?.isRefreshPlaces = true
        text_progress?.text = ""
        if (snackbar_retry != null) snackbar_retry?.dismiss()
        actionRefresh(sharedPref!!.lastPlacePage)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_refresh) {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_REFRESH)
            refreshContent()
        }
        return super.onOptionsItemSelected(item)
    }

    fun getScreenWidth(): Int {
        return Resources.getSystem().getDisplayMetrics().widthPixels
    }

    private fun startLoadMoreAdapter() {
        adapter?.resetListData()
        val items = db?.getPlacesByPage(category_id, Constant.LIMIT_LOADMORE, 0)
        adapter?.insertData(items, true)
        val item_count = db!!.getPlacesSize(category_id)
        // detect when scroll reach bottom
        adapter?.setOnLoadMoreListener { current_page ->
            if (item_count > adapter!!.itemCount && current_page != 0) {
                displayDataByPage(current_page)
            } else {
                adapter?.setLoaded()
            }
        }
    }

    //TODO: Need refactor --> Room
    private fun displayDataByPage(next_page: Int) {
        adapter?.setLoading()
        Handler().postDelayed({
            val items = db?.getPlacesByPage(category_id, Constant.LIMIT_LOADMORE, next_page * Constant.LIMIT_LOADMORE)
            adapter?.insertData(items, false)
        }, 500)
    }

    // checking some condition before perform refresh data
    private fun actionRefresh(page_no: Int) {
        val conn = Tools.checkConnection(context!!)
        if (conn) {
            if (!onProcess) {
                onRefresh(page_no)
            } else {
                Snackbar.make(root_view!!, R.string.task_running, Snackbar.LENGTH_SHORT).show()
            }
        } else {
            onFailureRetry(page_no, getString(R.string.no_internet))
        }
    }

    private fun onRefresh(page_no: Int) {
        onProcess = true
        showProgress(onProcess)
        val isDraft = if (AppConfig.LAZY_LOAD) 1 else 0
        callback = RestAdapter.createAPI().getPlacesByPage(page_no, Constant.LIMIT_PLACE_REQUEST, isDraft)
        callback!!.enqueue(object : retrofit2.Callback<CallbackListPlace> {
            override fun onResponse(call: Call<CallbackListPlace>, response: Response<CallbackListPlace>) {
                val resp = response.body()
                if (resp != null) {
                    count_total = resp.count_total
                    if (page_no == 1) db!!.refreshTablePlace()
                    db!!.insertListPlace(resp.places)  // save result into database
                    sharedPref!!.lastPlacePage = page_no + 1
                    delayNextRequest(page_no)
                    val str_progress = String.format(getString(R.string.load_of), page_no * Constant.LIMIT_PLACE_REQUEST, count_total)
                    text_progress!!.text = str_progress
                } else {
                    onFailureRetry(page_no, getString(R.string.refresh_failed))
                }
            }

            override fun onFailure(call: Call<CallbackListPlace>?, t: Throwable) {
                if (call != null && !call.isCanceled) {
                    Log.e(LOG_TAG, "FragmentHome - onFailire ${t.message}")
                    val conn = Tools.checkConnection(context!!)
                    if (conn) {
                        onFailureRetry(page_no, getString(R.string.refresh_failed))
                    } else {
                        onFailureRetry(page_no, getString(R.string.no_internet))
                    }
                }
            }
        })
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            lyt_progress!!.visibility = View.VISIBLE
            recyclerView!!.visibility = View.GONE
        } else {
            lyt_progress!!.visibility = View.GONE
            recyclerView!!.visibility = View.VISIBLE
        }
    }

    private fun onFailureRetry(page_no: Int, msg: String) {
        onProcess = false
        showProgress(onProcess)
        startLoadMoreAdapter()
        snackbar_retry = Snackbar.make(root_view!!, msg, Snackbar.LENGTH_INDEFINITE)
        snackbar_retry!!.setAction(R.string.RETRY) { actionRefresh(page_no) }
        snackbar_retry!!.show()
    }

    private fun delayNextRequest(page_no: Int) {
        if (count_total == 0) {
            onFailureRetry(page_no, getString(R.string.refresh_failed))
            return
        }
        if (page_no * Constant.LIMIT_PLACE_REQUEST > count_total) { // when all data loaded
            onProcess = false
            showProgress(onProcess)
            startLoadMoreAdapter()
            sharedPref!!.isRefreshPlaces = false
            text_progress!!.text = ""
            Snackbar.make(root_view!!, R.string.load_success, Snackbar.LENGTH_LONG).show()
            return
        }
        Handler().postDelayed({ onRefresh(page_no + 1) }, 500)
    }

    companion object {
        var TAG_CATEGORY = "key.TAG_CATEGORY"
    }

}
