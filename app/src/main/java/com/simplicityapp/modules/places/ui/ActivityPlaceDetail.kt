package com.simplicityapp.modules.places.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

import com.simplicityapp.modules.settings.ui.ActivityFullScreenImage
import com.simplicityapp.base.adapter.AdapterImageList
import com.simplicityapp.base.connection.RestAdapter
import com.simplicityapp.base.connection.callbacks.CallbackPlaceDetails
import com.simplicityapp.base.data.Constant
import com.simplicityapp.base.data.database.DatabaseHandler
import com.simplicityapp.base.data.SharedPref
import com.simplicityapp.modules.main.ui.ActivityMain
import com.simplicityapp.modules.places.model.Images
import com.simplicityapp.modules.places.model.Place
import com.simplicityapp.R
import com.simplicityapp.base.analytics.AnalyticsConstants
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.CONTENT_PLACE
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.SELECT_PLACE_ADDRESS
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.SELECT_PLACE_FAVORITES_ADD
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.SELECT_PLACE_FAVORITES_REMOVE
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.SELECT_PLACE_OPEN_MAP
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.SELECT_PLACE_OPEN_NAVIGATION
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.SELECT_PLACE_PHONE
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.SELECT_PLACE_WEB_SITE
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.VIEW_PLACE
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.logAnalyticsEvent
import com.simplicityapp.base.analytics.AnalyticsConstants.Companion.logAnalyticsShare
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.base.utils.UITools
import kotlinx.android.synthetic.main.include_place_details_content.*
import retrofit2.Call
import retrofit2.Response
import java.lang.Exception
import java.util.*

class ActivityPlaceDetail : AppCompatActivity() {

    private var place: Place? = null
    private var fab: FloatingActionButton? = null
    private var description: WebView? = null
    private var parent_view: View? = null
    private var photosView: CardView? = null
    private var descriptionView: CardView? = null
    private var googleMap: GoogleMap? = null
    private var db: DatabaseHandler? = null
    private var fabIsShowing = true

    private var onProcess = false
    private var isFromNotif = false
    private var callback: Call<CallbackPlaceDetails>? = null
    private var lyt_progress: View? = null
    private var lyt_distance: View? = null
    private var distance = -1f
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_details)
        parent_view = findViewById(android.R.id.content)
        photosView = findViewById(R.id.cardview_photos)
        descriptionView = findViewById(R.id.cardview_description)

        db = DatabaseHandler(this)
        // animation transition
        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), EXTRA_OBJ)

        place = intent.getSerializableExtra(EXTRA_OBJ) as Place
        isFromNotif = intent.getBooleanExtra(EXTRA_NOTIF_FLAG, false)

        fab = findViewById<View>(R.id.fab) as FloatingActionButton
        lyt_progress = findViewById(R.id.lyt_progress)
        lyt_distance = findViewById(R.id.lyt_distance)
        UITools.displayImage(this, findViewById<View>(R.id.image) as ImageView, Constant.getURLimgPlace(place!!.image))
        distance = place!!.distance

        configFab()
        setupToolbar(place!!.name)
        initMap()

        // handle when favorite button clicked
        fab!!.setOnClickListener {
            if (db!!.isFavoritesExist(place!!.place_id)) {
                db!!.deleteFavorites(place!!.place_id)
                Snackbar.make(parent_view!!, place!!.name + " " + getString(R.string.remove_favorite), Snackbar.LENGTH_SHORT).show()
                // analytics tracking
                logAnalyticsEvent(SELECT_PLACE_FAVORITES_REMOVE, place?.name.orEmpty(), user= true, fullUser = false)
                fabToggle(false)
            } else {
                db!!.addFavorites(place!!.place_id)
                Snackbar.make(parent_view!!, place!!.name + " " + getString(R.string.add_favorite), Snackbar.LENGTH_SHORT).show()
                // analytics tracking
                logAnalyticsEvent(SELECT_PLACE_FAVORITES_ADD, place?.name.orEmpty(), user= true, fullUser = false)
                fabToggle(true)
            }
        }

        // analytics tracking
        logAnalyticsEvent(VIEW_PLACE, place?.name.orEmpty())
    }


    private fun displayData(p: Place) {

        if (p.phone == "") {
            (findViewById<View>(R.id.lyt_phone) as LinearLayout).visibility = GONE
        }

        if (p.website == "") {
            (findViewById<View>(R.id.lyt_website) as LinearLayout).visibility = GONE
        }

        (findViewById<View>(R.id.address) as TextView).text = p.address
        (findViewById<View>(R.id.phone) as TextView).text = if (p.phone == "-" || p.phone!!.trim { it <= ' ' } == "") getString(R.string.no_phone_number) else p.phone
        (findViewById<View>(R.id.website) as TextView).text = if (p.website == "-" || p.website!!.trim { it <= ' ' } == "") getString(R.string.no_website) else p.website

        try {
            if (p.description?.replace("&nbsp;", "").isNullOrBlank()) {
                cardview_description.visibility = GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        description = findViewById<View>(R.id.description) as WebView
        var html_data = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> "
        html_data += p.description
        description!!.settings.builtInZoomControls = true
        description!!.setBackgroundColor(Color.TRANSPARENT)
        description!!.webChromeClient = WebChromeClient()
        description!!.loadData(html_data, "text/html; charset=UTF-8", null)
        description!!.settings.javaScriptEnabled = true
        // disable scroll on touch
        description!!.setOnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE }

        if (distance == -1f) {
            lyt_distance!!.visibility = View.GONE
        } else {
            lyt_distance!!.visibility = View.VISIBLE
            (findViewById<View>(R.id.distance) as TextView).text = Tools.getFormatedDistance(distance)
        }

        setImageGallery(db!!.getListImageByPlaceId(p.place_id))
        try {
            if ((p.images.size > 1).not()) {
                cardview_photos.visibility = GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        loadPlaceData()
        if (description != null) description!!.onResume()
        super.onResume()
    }

    // this method name same with android:onClick="clickLayout" at layout xml
    fun clickLayout(view: View) {
        when (view.id) {
            R.id.lyt_address -> if (!place!!.isDraft) {
                logAnalyticsEvent(SELECT_PLACE_ADDRESS, place?.name.orEmpty(), true, false)
                val uri = Uri.parse("http://maps.google.com/maps?q=loc: ${place!!.lat},${place!!.lng}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            R.id.lyt_phone -> if (!place!!.isDraft && place!!.phone != "-" && place!!.phone!!.trim { it <= ' ' } != "") {
                logAnalyticsEvent(SELECT_PLACE_PHONE, place?.name.orEmpty(), true, false)
                ActionTools.Companion.dialNumber(this, place!!.phone!!)
            } else {
                Snackbar.make(parent_view!!, R.string.fail_dial_number, Snackbar.LENGTH_SHORT).show()
            }
            R.id.lyt_website -> if (!place!!.isDraft && place!!.website != "-" && place!!.website!!.trim { it <= ' ' } != "") {
                logAnalyticsEvent(SELECT_PLACE_WEB_SITE, place?.name.orEmpty(), true, false)
                ActionTools.directUrl(this, place!!.website!!)
            } else {
                Snackbar.make(parent_view!!, R.string.fail_open_website, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setImageGallery(images: List<Images>) {
        val new_images = ArrayList<Images>()
        val new_images_str = ArrayList<String>()
        new_images.add(Images(place!!.place_id, place!!.image))
        new_images.addAll(images)
        for (img in new_images) {
            new_images_str.add(Constant.getURLimgPlace(img.name))
        }

        val galleryRecycler = findViewById<View>(R.id.galleryRecycler) as RecyclerView
        galleryRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = AdapterImageList(this, new_images)
        galleryRecycler.adapter = adapter
        adapter.setOnItemClickListener { view, viewModel, pos ->
            logAnalyticsEvent(AnalyticsConstants.SELECT_PLACE_PHOTO, place?.name, user= true, fullUser = false)
            val i = Intent(this@ActivityPlaceDetail, ActivityFullScreenImage::class.java)
            i.putExtra(ActivityFullScreenImage.EXTRA_POS, pos)
            i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, new_images_str)
            startActivity(i)
        }
        place?.images = new_images
    }

    private fun fabToggle(isFavorite: Boolean) {
        if (isFavorite) {
            fab!!.setImageResource(R.drawable.ic_nav_favorites)
        } else {
            fab!!.setImageResource(R.drawable.ic_nav_favorites_outline)
        }
    }

    private fun configFab() {
        if (db!!.isFavoritesExist(place!!.place_id)) {
            fab!!.setImageResource(R.drawable.ic_nav_favorites)
        } else {
            fab!!.setImageResource(R.drawable.ic_nav_favorites_outline)
        }
    }

    private fun setupToolbar(name: String?) {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.title = ""

        (findViewById<View>(R.id.toolbar_title) as TextView).text = name

        val collapsing_toolbar = findViewById<View>(R.id.collapsing_toolbar) as CollapsingToolbarLayout
        collapsing_toolbar.setContentScrimColor(SharedPref(this).themeColorInt)
        (findViewById<View>(R.id.app_bar_layout) as AppBarLayout).addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (collapsing_toolbar.height + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsing_toolbar)) {
                hideFab()
            } else {
                showFab()
            }
        })
    }

    private fun showFab() {
        if (!fabIsShowing) {
            fab?.isVisible = true
            fabIsShowing = true
        }
    }

    private fun hideFab() {
        if (fabIsShowing) {
            fab?.isVisible = false
            fabIsShowing = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_place_details, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            backAction()
            return true
        } else if (id == R.id.action_share) {
            if (!place!!.isDraft) {
                logAnalyticsEvent(AnalyticsConstants.SELECT_PLACE_SHARE, place?.name, user= true, fullUser = false)
                logAnalyticsShare(CONTENT_PLACE, place?.name.orEmpty())
                ActionTools.methodShare(this@ActivityPlaceDetail, place!!)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initMap() {
        if (googleMap == null) {
            val mapFragment1 = fragmentManager.findFragmentById(R.id.mapPlaces) as MapFragment
            mapFragment1.getMapAsync { gMap ->
                googleMap = gMap
                if (googleMap == null) {
                    Snackbar.make(parent_view!!, R.string.unable_create_map, Snackbar.LENGTH_SHORT).show()
                } else {
                    // config map
                    googleMap = Tools.configStaticMap(this@ActivityPlaceDetail, googleMap!!, place!!)
                }
            }
        }

        (findViewById<View>(R.id.bt_navigate) as Button).setOnClickListener {
            logAnalyticsEvent(SELECT_PLACE_OPEN_NAVIGATION, place?.name.orEmpty(), user= true, fullUser = false)
            val navigation = Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=${place!!.lat},${place!!.lng}"))
            startActivity(navigation)
        }
        (findViewById<View>(R.id.bt_view) as Button).setOnClickListener { openPlaceInMap() }
        (findViewById<View>(R.id.map) as LinearLayout).setOnClickListener { openPlaceInMap() }
    }

    private fun openPlaceInMap() {
        logAnalyticsEvent(SELECT_PLACE_OPEN_MAP, place?.name.orEmpty(), user= true, fullUser = false)
        val intent = Intent(this@ActivityPlaceDetail, ActivityMaps::class.java)
        intent.putExtra(ActivityMaps.EXTRA_OBJ, place)
        startActivity(intent)
    }

    override fun onDestroy() {
        if (callback != null && callback!!.isExecuted) callback!!.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        backAction()
    }

    override fun onPause() {
        super.onPause()
        if (description != null) description!!.onPause()
    }

    private fun backAction() {
        if (isFromNotif) {
            val i = Intent(this, ActivityMain::class.java)
            startActivity(i)
        }
        overridePendingTransition(R.anim.exit_slide_in, R.anim.exit_slide_out)
        super.onBackPressed()
    }

    // places detail load with lazy scheme
    private fun loadPlaceData() {
        place = db!!.getPlace(place!!.place_id)
        if (place!!.isDraft) {
            if (Tools.checkConnection(this)) {
                requestDetailsPlace(place!!.place_id)
            } else {
                onFailureRetry(getString(R.string.no_internet))
            }
        } else {
            displayData(place!!)
        }
    }

    private fun requestDetailsPlace(place_id: Int) {
        if (onProcess) {
            Snackbar.make(parent_view!!, R.string.task_running, Snackbar.LENGTH_SHORT).show()
            return
        }
        onProcess = true
        showProgressbar(true)
        callback = RestAdapter.createAPI().getPlaceDetails(place_id)
        callback!!.enqueue(object : retrofit2.Callback<CallbackPlaceDetails> {
            override fun onResponse(call: Call<CallbackPlaceDetails>, response: Response<CallbackPlaceDetails>) {
                val resp = response.body()
                if (resp != null) {
                    place = db!!.updatePlace(resp.place)
                    displayDataWithDelay(place)
                } else {
                    onFailureRetry(getString(R.string.failed_load_details))
                }

            }

            override fun onFailure(call: Call<CallbackPlaceDetails>?, t: Throwable) {
                if (call != null && !call.isCanceled) {
                    val conn = Tools.checkConnection(this@ActivityPlaceDetail)
                    if (conn) {
                        onFailureRetry(getString(R.string.failed_load_details))
                    } else {
                        onFailureRetry(getString(R.string.no_internet))
                    }
                }
            }
        })
    }

    private fun displayDataWithDelay(resp: Place?) {
        Handler().postDelayed({
            showProgressbar(false)
            onProcess = false
            displayData(resp!!)
        }, 1000)
    }

    private fun onFailureRetry(msg: String) {
        showProgressbar(false)
        onProcess = false
        snackbar = Snackbar.make(parent_view!!, msg, Snackbar.LENGTH_INDEFINITE)
        snackbar!!.setAction(R.string.RETRY) { loadPlaceData() }
        snackbar!!.show()
        retryDisplaySnackbar()
    }

    private fun retryDisplaySnackbar() {
        if (snackbar != null && !snackbar!!.isShown) {
            Handler().postDelayed({ retryDisplaySnackbar() }, 1000)
        }
    }

    private fun showProgressbar(show: Boolean) {
        lyt_progress!!.visibility = if (show) View.VISIBLE else View.GONE
    }

    companion object {

        private val SCREEN_NAME = "PLACE_DETAILS"
        private val EXTRA_OBJ = "key.EXTRA_OBJ"
        private val EXTRA_NOTIF_FLAG = "key.EXTRA_NOTIF_FLAG"

        // give preparation animation activity transition
        fun navigate(activity: AppCompatActivity?, sharedView: View, p: Place, analyticsEvent: String) {
            logAnalyticsEvent(analyticsEvent, p.name, true, false)
            val intent = Intent(activity, ActivityPlaceDetail::class.java)
            intent.putExtra(EXTRA_OBJ, p)
            activity?.let {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedView, EXTRA_OBJ)
                ActivityCompat.startActivity(activity, intent, options.toBundle())
            }
        }

        fun navigateBase(context: Context, obj: Place, from_notif: Boolean?): Intent {
            val i = Intent(context, ActivityPlaceDetail::class.java)
            i.putExtra(EXTRA_OBJ, obj)
            i.putExtra(EXTRA_NOTIF_FLAG, from_notif)
            return i
        }
    }
}
