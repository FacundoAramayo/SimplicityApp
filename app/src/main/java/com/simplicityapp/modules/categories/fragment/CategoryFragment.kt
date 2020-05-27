package com.simplicityapp.modules.categories.fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

import com.google.android.material.snackbar.Snackbar

import java.util.ArrayList

import com.simplicityapp.modules.main.activity.ActivityMain
import com.simplicityapp.modules.places.activity.ActivityPlaceDetail
import com.simplicityapp.R
import com.simplicityapp.baseui.adapter.AdapterPlaceGrid
import com.simplicityapp.base.rest.RestAdapter
import com.simplicityapp.base.connection.callbacks.CallbackListPlace
import com.simplicityapp.base.config.AppConfig
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.persistence.db.DatabaseHandler
import com.simplicityapp.base.persistence.preferences.SharedPref
import com.simplicityapp.base.config.ThisApplication
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.baseui.utils.UITools
import com.simplicityapp.baseui.decorator.SpacingItemDecoration
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.config.Constant.LOG_TAG
import retrofit2.Call
import retrofit2.Response

class CategoryFragment : Fragment() {

    private var count_total: Int = 0
    private var category_id: Int = 0

    private var root_view: View? = null
    private var recyclerView: RecyclerView? = null
    private var lyt_progress: View? = null
    private var lyt_not_found: View? = null
    private var text_progress: TextView? = null
    private var snackbar_retry: Snackbar? = null

    private var db: DatabaseHandler? = null
    private var sharedPref: SharedPref? = null
    private var adapter: AdapterPlaceGrid? = null

    private var callback: Call<CallbackListPlace>? = null

    private var onProcess = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root_view = inflater.inflate(R.layout.fragment_category, null)
        db = DatabaseHandler(context)
        sharedPref =
            SharedPref(context)
        category_id = arguments!!.getInt(TAG_CATEGORY)

        initUI()

        return root_view
    }

    private fun initUI() {
        // activate fragment menu
        setHasOptionsMenu(true)

        recyclerView = root_view?.findViewById<View>(R.id.recycler) as RecyclerView
        lyt_progress = root_view?.findViewById(R.id.lyt_progress)
        lyt_not_found = root_view?.findViewById(R.id.lyt_not_found)
        text_progress = root_view?.findViewById<View>(R.id.text_progress) as TextView

        recyclerView?.layoutManager = StaggeredGridLayoutManager(UITools.getGridSpanCount(activity!!), StaggeredGridLayoutManager.VERTICAL)
        recyclerView?.addItemDecoration(
            SpacingItemDecoration(
                UITools.getGridSpanCount(activity!!),
                UITools.dpToPx(4),
                true
            )
        )

        //set data and list adapter
        adapter = AdapterPlaceGrid(activity, recyclerView, ArrayList(), StaggeredGridLayoutManager.VERTICAL, 0)
        recyclerView?.adapter = adapter

        // on item list clicked
        adapter?.setOnItemClickListener { v, obj -> ActivityPlaceDetail.navigate((activity as? AppCompatActivity), v.findViewById(R.id.lyt_content), obj, AnalyticsConstants.SELECT_CATEGORY_PLACE) }

        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(v: RecyclerView, state: Int) {
                super.onScrollStateChanged(v, state)
                if (state == RecyclerView.SCROLL_STATE_DRAGGING || state == RecyclerView.SCROLL_STATE_SETTLING) {
                    ActivityMain.animateFab(true)
                } else {
                    ActivityMain.animateFab(false)
                }
            }
        })
        startLoadMoreAdapter()
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
        if (sharedPref!!.isRefreshPlaces || db?.placesSize == 0) {
            actionRefresh(sharedPref!!.lastPlacePage)
        } else {
            startLoadMoreAdapter()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_category, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_refresh) {
            ThisApplication.instance?.location = null
            sharedPref?.lastPlacePage = 1
            sharedPref?.isRefreshPlaces = true
            text_progress?.text = ""
            if (snackbar_retry != null) snackbar_retry?.dismiss()
            actionRefresh(sharedPref!!.lastPlacePage)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startLoadMoreAdapter() {
        adapter?.resetListData()
        val items = db?.getPlacesByPage(category_id, Constant.LIMIT_LOADMORE, 0)
        adapter?.insertData(items, false)
        showNoItemView()
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
            showNoItemView()
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
                    Log.e(LOG_TAG, "FragmentCategory, onFailure: " + t.message)
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
            lyt_not_found!!.visibility = View.GONE
        } else {
            lyt_progress!!.visibility = View.GONE
            recyclerView!!.visibility = View.VISIBLE
        }
    }

    private fun showNoItemView() {
        if (adapter!!.itemCount == 0) {
            lyt_not_found!!.visibility = View.VISIBLE
        } else {
            lyt_not_found!!.visibility = View.GONE
        }
    }

    private fun onFailureRetry(page_no: Int, msg: String) {
        onProcess = false
        showProgress(onProcess)
        showNoItemView()
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
