package com.simplicityapp.modules.main.fragment

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.simplicityapp.R
import com.simplicityapp.baseui.adapter.AdapterNewsList
import com.simplicityapp.baseui.adapter.AdapterPlaceGrid
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.config.AppConfig
import com.simplicityapp.base.config.AppConfig.LIMIT_PLACES_TO_UPDATE
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.config.Constant.JOBS_GUIDE
import com.simplicityapp.base.config.Constant.SUCCESS_RESPONSE
import com.simplicityapp.base.persistence.preferences.SharedPref
import com.simplicityapp.base.persistence.db.DatabaseHandler
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.baseui.utils.UITools
import com.simplicityapp.baseui.decorator.SpacingItemDecoration
import com.simplicityapp.databinding.FragmentHomeBinding
import com.simplicityapp.modules.main.activity.ActivityMain
import com.simplicityapp.modules.notifications.activity.ActivityNotificationDetails.Companion.navigate
import com.simplicityapp.modules.notifications.model.News
import com.simplicityapp.modules.notifications.repository.NewsRepository
import com.simplicityapp.modules.places.activity.ActivityPlaceDetail
import com.simplicityapp.modules.places.model.Place
import com.simplicityapp.modules.places.model.PlacesResponse
import com.simplicityapp.modules.places.repository.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FragmentHome : Fragment() {

    private var countTotal: Int = 0
    private var categoryId: Int = 0
    private var snackbarRetry: Snackbar? = null
    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: DatabaseHandler
    private lateinit var sharedPref: SharedPref
    private val placesRepository: PlacesRepository = PlacesRepository()
    private val newsRepository: NewsRepository = NewsRepository()
    private var adapterFeatured: AdapterPlaceGrid? = null
    private var adapterNews: AdapterNewsList? = null
    private var backToHome = false
    private var featuredOnProcess = false
    private var newsOnProcess = false
    private var newsPostTotal = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = DatabaseHandler(context)
        sharedPref = SharedPref(context)
        categoryId = 0
        initUI()
        isLoadComplete(false)
        return binding.root
    }

    private fun initUI() {
        setHasOptionsMenu(true)
        binding.apply {
            shimmerViewContainer.visibility = View.VISIBLE
            mainScrollView.visibility = View.GONE
            buttonHomeShareApp.setOnClickListener {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_ACTION, AnalyticsConstants.SHARE_APP, true)
                activity?.let { it1 -> ActionTools.methodShare(it1) }
            }
            buttonHomeSubscription.setOnClickListener {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_ACTION, AnalyticsConstants.OPEN_REGISTER_FORM, true)
                activity?.let { it1 -> ActionTools.directUrl(it1, Constant.LINK_TO_SUBSCRIPTION_FORM) }
            }
            lytQuickAccessGastronomy.setOnClickListener {
                backToHome = true
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_DELIVERY)
                ActivityMain.ActivityMainInstance.onItemSelected(R.id.nav_delivery, resources.getString(R.string.title_nav_delivery), false, true)
            }
            lytQuickAccessTaxi.setOnClickListener {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_TAXI)
                ActivityMain.ActivityMainInstance.onItemSelected(R.id.nav_taxi, resources.getString(R.string.title_nav_taxi), false, true)
            }
            lytQuickAccessJobs.setOnClickListener {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_JOBS)
                ActivityMain.ActivityMainInstance.categorySelectorIntent(JOBS_GUIDE)
            }
            lytQuickAccessPharmacy.setOnClickListener {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_PHARMACY)
                ActivityMain.ActivityMainInstance.onItemSelected(R.id.nav_pharmacy, resources.getString(R.string.title_nav_pharmacy), false, true)
            }
            lytQuickAccessSearch.setOnClickListener {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_SEARCH)
                ActivityMain.ActivityMainInstance.searchIntent()
            }
            lytQuickAccessFav.setOnClickListener {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_FAVORITES)
                ActivityMain.ActivityMainInstance.favoritesIntent()
            }
            lytQuickAccessMap.setOnClickListener {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_MAP)
                ActivityMain.ActivityMainInstance.mapIntent()
            }
            lytQuickAccessEmergency.setOnClickListener {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_QUICK_ACCESS, AnalyticsConstants.QUICK_ACCESS_EMERGENCY)
                ActivityMain.ActivityMainInstance.onItemSelected(R.id.nav_emergency, resources.getString(R.string.title_nav_emergency), false, true)
            }
        }
        initRecyclerFeatured()
        initRecyclerNews()
    }

    override fun onResume() {
        adapterFeatured?.notifyDataSetChanged()
        adapterNews?.notifyDataSetChanged()
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        if (sharedPref.isRefreshPlaces || db.placesSize < LIMIT_PLACES_TO_UPDATE) {
            refreshAll()
        }
    }

    private fun refreshAll() {
        val conn = Tools.checkConnection(context!!)
        if (conn) {
            if (!featuredOnProcess and !newsOnProcess) {
                startShimmer()
                refreshFeaturedPlaces()
                refreshNews()
            } else {
                Snackbar.make(binding.root, R.string.task_running, Snackbar.LENGTH_SHORT).show()
            }
        } else {
            Snackbar.make(binding.root, R.string.no_internet, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun isLoadComplete(complete: Boolean) {
        if (complete) {
            if (!featuredOnProcess and !newsOnProcess and (binding.shimmerViewContainer.isShimmerStarted)) {
                stopShimmer()
            }
        } else {
            if (!binding.shimmerViewContainer.isShimmerStarted) {
                startShimmer()
            }
        }
    }

    private fun startShimmer() {
        binding.apply {
            mainScrollView.visibility = View.GONE
            shimmerViewContainer.visibility = View.VISIBLE
            shimmerViewContainer.startShimmer()
        }
    }

    private fun stopShimmer() {
        binding.apply {
            shimmerViewContainer.stopShimmer()
            mainScrollView.visibility = View.VISIBLE
            shimmerViewContainer.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_category, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_refresh) {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_TOOLBAR_ACTION, AnalyticsConstants.REFRESH)
            refreshAll()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    //FEATURED LIST METHODS
    private fun refreshFeaturedPlaces() {
        featuredOnProcess = true
        isLoadComplete(false)
        showFeaturedPlaces(false)
        sharedPref.lastPlacePage = 1
        sharedPref.isRefreshPlaces = true
        actionRefreshFeatured(sharedPref.lastPlacePage)
    }

    private fun initRecyclerFeatured() {
        binding.recyclerFeatured.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        binding.recyclerFeatured.addItemDecoration(
            SpacingItemDecoration(
                UITools.getGridSpanCount(activity!!),
                UITools.dpToPx(4),
                true
            )
        )
        //set data and list adapter
        adapterFeatured = AdapterPlaceGrid(activity, binding.recyclerFeatured, ArrayList(), StaggeredGridLayoutManager.HORIZONTAL, getScreenWidth())
        binding.recyclerFeatured.adapter = adapterFeatured
        // on item list clicked
        adapterFeatured?.setOnItemClickListener {
                v, obj -> ActivityPlaceDetail.navigate((activity as ActivityMain?)!!, v.findViewById(R.id.lyt_content), obj, AnalyticsConstants.SELECT_HOME_FEATURED_BANNER)
        }
    }

    // checking some condition before perform refresh data
    private fun actionRefreshFeatured(page: Int) {
        requestListFeatured(page)
    }

    private fun requestListFeatured(page_no: Int) {
        featuredOnProcess = true
        isLoadComplete(false)
        val isDraft = if (AppConfig.LAZY_LOAD) 1 else 0
        var response: PlacesResponse? = null
        GlobalScope.launch(Dispatchers.Main) {
            response = placesRepository.getPlacesByPage(page_no, Constant.LIMIT_PLACE_REQUEST, isDraft, sharedPref.regionId)?.body()
            response?.let {
                if (it.status == SUCCESS_RESPONSE) {
                    countTotal = it.count_total
                    if (page_no == 1) {
                        adapterFeatured?.resetListData()
                        db.refreshTablePlace()
                    }
                    db.insertListPlace(it.places, sharedPref.regionId)
                    sharedPref.lastPlacePage = page_no + 1
                    displayFeaturedResult(filterFeatured(it.places))
                } else {
                    onFailRequestFeatured()
                }
            } ?: onFailRequestFeatured()
        }
    }

    private fun displayFeaturedResult(items: List<Place>) {
        adapterFeatured?.insertData(items, true)
        featuredOnProcess = false
        isLoadComplete(true)
        if (items.isEmpty()) {
            showFeaturedPlaces(false)
        } else {
            showFeaturedPlaces(true)
        }
    }

    private fun onFailRequestFeatured() {
        featuredOnProcess = false
        isLoadComplete(true)
        showFeaturedPlaces(false)
        snackbarRetry = Snackbar.make(binding.root, getString(R.string.refresh_failed), Snackbar.LENGTH_INDEFINITE)
        snackbarRetry?.setAction(R.string.RETRY) { refreshFeaturedPlaces() }
        snackbarRetry?.show()
    }

    private fun filterFeatured(places: List<Place>): List<Place> {
        val featured = mutableListOf<Place>()
        places.forEach { place ->
            place.categories.forEach {
                if (it.cat_id == 0) {
                    Log.d("LOG-", "place: ${place.name}, categories: ${place.categories}")
                    featured.add(place)
                }
            }
        }
        return featured.toList()
    }

    //NEWS LIST METHODS
    private fun refreshNews() {
        newsOnProcess = true
        isLoadComplete(false)
        showNews(false)
        newsPostTotal = 0
        actionRefreshNews(1)
    }

    private fun initRecyclerNews() {
        binding.recyclerNews.layoutManager = LinearLayoutManager(context)
        binding.recyclerNews.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL))
        //set data and list adapter
        adapterNews = AdapterNewsList(activity, binding.recyclerNews, ArrayList())
        binding.recyclerNews.adapter = adapterNews
        // on item list clicked
        adapterNews?.setOnItemClickListener(AdapterNewsList.OnItemClickListener { v, obj, position ->
            navigate(
                activity!!,
                obj,
                false,
                AnalyticsConstants.SELECT_HOME_NEWS
            )
        })
    }

    private fun actionRefreshNews(page: Int) {
        requestListNews(page)
    }

    private fun requestListNews(page_no: Int) {
        newsOnProcess = true
        isLoadComplete(false)
        GlobalScope.launch(Dispatchers.Main) {
            val response = newsRepository.getNews(page_no, Constant.LIMIT_NEWS_REQUEST)?.body()
            response?.let {
                if (it.status == SUCCESS_RESPONSE) {
                    if (page_no == 1) {
                        adapterNews?.resetListData()
                        db.refreshTableContentInfo()
                    }
                    newsPostTotal = it.countTotal
                    db.insertListContentInfo(it.newsList)
                    displayNewsResult(it.newsList)
                } else {
                    onFailRequestNews()
                }
            } ?: onFailRequestNews()
        }
    }

    private fun displayNewsResult(items: List<News>) {
        adapterNews?.insertData(items)
        newsOnProcess = false
        isLoadComplete(true)
        if (items.isEmpty()) {
            showNews(false)
        } else {
            showNews(true)
        }
    }

    private fun onFailRequestNews() {
        newsOnProcess = false
        isLoadComplete(true)
        showNews(false)
        snackbarRetry = Snackbar.make(binding.root, getString(R.string.refresh_failed), Snackbar.LENGTH_INDEFINITE)
        snackbarRetry?.setAction(R.string.RETRY) { refreshNews() }
        snackbarRetry?.show()
    }

    private fun showNews(show: Boolean) {
        if (show) {
            binding.tvNewsTitle.visibility = View.VISIBLE
            binding.recyclerNews.visibility = View.VISIBLE
        } else {
            binding.tvNewsTitle.visibility = View.GONE
            binding.recyclerNews.visibility = View.GONE
        }
    }

    private fun showFeaturedPlaces(show: Boolean) {
        if (show) {
            binding.tvFeaturedTitle.visibility = View.VISIBLE
            binding.recyclerFeatured.visibility = View.VISIBLE
        } else {
            binding.tvFeaturedTitle.visibility = View.GONE
            binding.recyclerFeatured.visibility = View.GONE
        }
    }

}
