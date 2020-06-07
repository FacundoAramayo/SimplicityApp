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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.simplicityapp.base.rest.RestAdapter
import com.simplicityapp.base.connection.callbacks.ListPlaceResponse
import com.simplicityapp.base.config.AppConfig
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.persistence.db.DatabaseHandler
import com.simplicityapp.base.persistence.preferences.SharedPref
import com.simplicityapp.base.config.ThisApplication
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.config.Constant.LOG_TAG
import com.simplicityapp.baseui.adapter.AdapterPlaceList
import com.simplicityapp.modules.places.activity.ActivityPlaceDetail
import com.simplicityapp.R
import java.util.ArrayList
import retrofit2.Call
import retrofit2.Response

class CategoryFragment : Fragment() {

    private var countTotal: Int = 0
    private var categoryId: Int = 0

    private var rootView: View? = null
    private var recyclerView: RecyclerView? = null
    private var lytProgress: View? = null
    private var lytNotFound: View? = null
    private var textProgress: TextView? = null
    private var snackbarRetry: Snackbar? = null

    private lateinit var db: DatabaseHandler
    private lateinit var sharedPref: SharedPref
    private var adapter: AdapterPlaceList? = null

    private var response: Call<ListPlaceResponse>? = null

    private var onProcess = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_category, null)
        db = DatabaseHandler(context)
        sharedPref = SharedPref(context)
        categoryId = arguments!!.getInt(TAG_CATEGORY)

        initUI()

        return rootView
    }

    private fun initUI() {
        // activate fragment menu
        setHasOptionsMenu(true)

        recyclerView = rootView?.findViewById<View>(R.id.recycler) as RecyclerView
        lytProgress = rootView?.findViewById(R.id.lyt_progress)
        lytNotFound = rootView?.findViewById(R.id.lyt_not_found)
        textProgress = rootView?.findViewById<View>(R.id.text_progress) as TextView

        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        //set data and list adapter
        adapter = AdapterPlaceList(activity, recyclerView, ArrayList())
        recyclerView?.adapter = adapter

        // on item list clicked
        adapter?.setOnItemClickListener { v, obj -> ActivityPlaceDetail.navigate((activity as? AppCompatActivity), v.findViewById(R.id.lyt_content), obj, AnalyticsConstants.SELECT_CATEGORY_PLACE) }
        startLoadMoreAdapter()
    }

    override fun onDestroyView() {
        if (snackbarRetry != null) snackbarRetry?.dismiss()
        if (response != null && response!!.isExecuted) {
            response?.cancel()
        }
        super.onDestroyView()
    }

    override fun onResume() {
        adapter?.notifyDataSetChanged()
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        if (sharedPref.isRefreshPlaces || db.placesSize == 0) {
            actionRefresh(sharedPref.lastPlacePage)
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
            sharedPref.lastPlacePage = 1
            sharedPref.isRefreshPlaces = true
            textProgress?.text = ""
            if (snackbarRetry != null) snackbarRetry?.dismiss()
            actionRefresh(sharedPref.lastPlacePage)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startLoadMoreAdapter() {
        adapter?.resetListData()
        val items = db.getPlacesByPage(categoryId, Constant.LIMIT_LOADMORE, 0)
        adapter?.insertData(items, false)
        showNoItemView()
        val item_count = db.getPlacesSize(categoryId)
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
            val items = db.getPlacesByPage(categoryId, Constant.LIMIT_LOADMORE, next_page * Constant.LIMIT_LOADMORE)
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
                Snackbar.make(rootView!!, R.string.task_running, Snackbar.LENGTH_SHORT).show()
            }
        } else {
            onFailureRetry(page_no, getString(R.string.no_internet))
        }
    }

    private fun onRefresh(page_no: Int) {
        onProcess = true
        showProgress(onProcess)
        val isDraft = if (AppConfig.LAZY_LOAD) 1 else 0
        response = RestAdapter.createAPI().getPlacesByPage(page_no, Constant.LIMIT_PLACE_REQUEST, isDraft, sharedPref.regionId)
        response!!.enqueue(object : retrofit2.Callback<ListPlaceResponse> {
            override fun onResponse(call: Call<ListPlaceResponse>, response: Response<ListPlaceResponse>) {
                val resp = response.body()
                if (resp != null) {
                    countTotal = resp.count_total
                    if (page_no == 1) db.refreshTablePlace()
                    db.insertListPlace(resp.places, sharedPref.regionId)  // save result into database
                    sharedPref.lastPlacePage = page_no + 1
                    delayNextRequest(page_no)
                    val str_progress = String.format(getString(R.string.load_of), page_no * Constant.LIMIT_PLACE_REQUEST, countTotal)
                    textProgress!!.text = str_progress
                } else {
                    onFailureRetry(page_no, getString(R.string.refresh_failed))
                }
            }

            override fun onFailure(call: Call<ListPlaceResponse>?, t: Throwable) {
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
            lytProgress!!.visibility = View.VISIBLE
            recyclerView!!.visibility = View.GONE
            lytNotFound!!.visibility = View.GONE
        } else {
            lytProgress!!.visibility = View.GONE
            recyclerView!!.visibility = View.VISIBLE
        }
    }

    private fun showNoItemView() {
        if (adapter!!.itemCount == 0) {
            lytNotFound!!.visibility = View.VISIBLE
        } else {
            lytNotFound!!.visibility = View.GONE
        }
    }

    private fun onFailureRetry(page_no: Int, msg: String) {
        onProcess = false
        showProgress(onProcess)
        showNoItemView()
        startLoadMoreAdapter()
        snackbarRetry = Snackbar.make(rootView!!, msg, Snackbar.LENGTH_INDEFINITE)
        snackbarRetry!!.setAction(R.string.RETRY) { actionRefresh(page_no) }
        snackbarRetry!!.show()
    }

    private fun delayNextRequest(page_no: Int) {
        if (countTotal == 0) {
            onFailureRetry(page_no, getString(R.string.refresh_failed))
            return
        }
        if (page_no * Constant.LIMIT_PLACE_REQUEST > countTotal) { // when all data loaded
            onProcess = false
            showProgress(onProcess)
            startLoadMoreAdapter()
            sharedPref.isRefreshPlaces = false
            textProgress!!.text = ""
            Snackbar.make(rootView!!, R.string.load_success, Snackbar.LENGTH_LONG).show()
            return
        }
        Handler().postDelayed({ onRefresh(page_no + 1) }, 500)
    }

    companion object {
        var TAG_CATEGORY = "key.TAG_CATEGORY"
    }
}
