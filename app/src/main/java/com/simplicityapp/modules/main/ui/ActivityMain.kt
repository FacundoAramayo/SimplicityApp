package com.simplicityapp.modules.main.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast

import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment

import com.google.android.material.navigation.NavigationView

import com.simplicityapp.BuildConfig
import com.simplicityapp.base.data.Constant
import com.simplicityapp.modules.places.ui.ActivityMaps
import com.simplicityapp.modules.notifications.ui.ActivityNotifications
import com.simplicityapp.modules.settings.ui.ActivitySetting
import com.simplicityapp.base.data.AppConfig
import com.simplicityapp.base.data.database.DatabaseHandler
import com.simplicityapp.base.data.SharedPref
import com.simplicityapp.modules.main.ui.fragment.FragmentCategory
import com.simplicityapp.modules.main.ui.fragment.FragmentHome
import com.simplicityapp.R
import com.simplicityapp.base.analytics.AnalyticsConstants
import com.simplicityapp.base.data.Constant.*
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.modules.places.ui.ActivitySearch

class ActivityMain : AppCompatActivity() {

    private var home = false


    var actionBar: ActionBar? = null
    var toolbar: Toolbar? = null
    private var cat: IntArray? = null
    private var fab: com.getbase.floatingactionbutton.FloatingActionsMenu? = null
    private var fab_button_map: com.getbase.floatingactionbutton.FloatingActionButton? = null
    private var fab_button_search: com.getbase.floatingactionbutton.FloatingActionButton? = null
    private var fab_button_favorites: com.getbase.floatingactionbutton.FloatingActionButton? = null
    private var navigationView: NavigationView? = null
    private var db: DatabaseHandler? = null
    private var sharedPref: SharedPref? = null

    private var exitTime: Long = 0

    private var fragment: Fragment? = null
    private val bundle = Bundle()
    private var firstRun: Boolean = false
    private var backToHome = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.enter_slide_in, R.anim.enter_slide_out)
        setContentView(R.layout.activity_main_places)
        instance = this

        if (intent?.extras?.get(IS_FIRST_OPEN) == true) {
            firstRun = true
            AnalyticsConstants.logAnalyticsSignUp()
        }

        db = DatabaseHandler(this)
        sharedPref = SharedPref(this)

        configOpenApp()
        initUI()
    }

    /**
     * Código para configurar las preferencias una vez que el usuario realizó con éxito su registro
     */
    private fun configOpenApp() {
        val prefs = getSharedPreferences(Constant.PREFS_NAME, Context.MODE_PRIVATE)
        val currentVersionCode = BuildConfig.VERSION_CODE
        prefs.edit().putInt(Constant.PREF_VERSION_CODE_KEY, currentVersionCode).apply()
    }

    private fun initUI() {
        cat = resources.getIntArray(R.array.id_category)
        initToolbar()
        initDrawerMenu()
        initFabButtons()
        onItemSelected(R.id.nav_home, getString(R.string.title_home), false)
    }

    private fun initToolbar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        //DeviceTools.setActionBarColor(this, actionBar!!)
    }

    private fun initFabButtons() {
        fab = findViewById<View>(R.id.floating_action_button) as com.getbase.floatingactionbutton.FloatingActionsMenu
        fab_button_map = findViewById(R.id.fb_button_map)
        fab_button_search = findViewById(R.id.fb_button_search)
        fab_button_favorites = findViewById(R.id.fb_button_favorites)

        fab_button_map?.setOnClickListener {
            mapIntent()
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_ACTION, AnalyticsConstants.FAB_MAP)
        }

        fab_button_search?.setOnClickListener {
            searchIntent()
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_ACTION, AnalyticsConstants.FAB_SEARCH)
        }

        fab_button_favorites?.setOnClickListener {
            favoritesIntent()
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_HOME_ACTION, AnalyticsConstants.FAB_FAVORITES)
        }

    }

    private fun initDrawerMenu() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = object : ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            override fun onDrawerOpened(drawerView: View) {
                updateFavoritesCounter(navigationView!!, R.id.nav_favorites, db!!.favoritesSize)
                super.onDrawerOpened(drawerView)
            }
        }
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView?.setNavigationItemSelectedListener { item -> onItemSelected(item.itemId, item.title.toString()) }

        /*CONFIG UI WITH AppConfig*/
        if (!AppConfig.ENABLE_CONTENT_INFO) navigationView?.menu?.removeItem(R.id.nav_news)
        if (!AppConfig.ENABLE_USER_PROFILE) navigationView?.menu?.removeItem(R.id.nav_profile)
        if (!AppConfig.ENABLE_EMPTY_CATEGORIES and firstRun.not()) { removeEmptyCategories() }

    }

    private fun removeEmptyCategories() {
        cat?.forEach {
            val placesByCategory = db?.getAllPlaceByCategory(it)?.size
            if (placesByCategory == 0) {
                val categoryId = getMenuItemId(it)
                categoryId?.let { navigationView?.menu?.removeItem(categoryId) }
            }
        }
    }

    fun searchIntent() {
        val intent = Intent(this, ActivitySearch::class.java)
        startActivity(intent)
        fab?.collapse()
    }

    fun mapIntent() {
        val intent = Intent(this, ActivityMaps::class.java)
        startActivity(intent)
        fab?.collapse()
    }

    fun favoritesIntent() {
        onItemSelected(R.id.nav_favorites, getString(R.string.title_nav_fav), false)
        fab?.collapse()
    }



    private fun getMenuItemId(categoryId: Int): Int? {
        return when (categoryId) {
            0 -> R.id.nav_featured
            2 -> R.id.nav_pharmacy
            3 -> R.id.nav_gym
            4 -> R.id.nav_food
            5 -> R.id.nav_bar
            6 -> R.id.nav_fast_food
            7 -> R.id.nav_delivery
            8 -> R.id.nav_ice_cream_store
            9 -> R.id.nav_hotels
            10 -> R.id.nav_temporary_rent
            11 -> R.id.nav_tour
            12 -> R.id.nav_money
            13 -> R.id.nav_bill_payments
            14 -> R.id.nav_apartment_rental
            15 -> R.id.nav_taxi
            16 -> R.id.nav_gas_station
            17 -> R.id.nav_transport
            18 -> R.id.nav_transport_tickets
            19 -> R.id.nav_clothing_stores
            20 -> R.id.nav_big_stores
            21 -> R.id.nav_industrial_stores
            22 -> R.id.nav_art_and_design
            23 -> R.id.nav_jobs
            24 -> R.id.nav_emergency
            else -> null
        }
    }

    override fun onBackPressed() {
        if (backToHome) {
            onItemSelected(R.id.nav_home, resources.getString(R.string.title_home), false)
            return
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START)
        } else {
            doExitApp()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_TOOLBAR_ACTION, AnalyticsConstants.TOOLBAR_SETTINGS)
                val i = Intent(applicationContext, ActivitySetting::class.java)
                startActivity(i)
            }
            R.id.action_rate -> {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_TOOLBAR_ACTION, AnalyticsConstants.TOOLBAR_RATE)
                ActionTools.rateAction(this@ActivityMain)
            }
            R.id.action_about -> {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_TOOLBAR_ACTION, AnalyticsConstants.TOOLBAR_ABOUT)
                ActionTools.aboutAction(this@ActivityMain)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openFragmentCategory(title: String, categoryId: Int) {
        fragment = FragmentCategory()
        home = false
        bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![categoryId])
        actionBar?.title = title
    }

    fun onItemSelected(id: Int, title: String, logAnalytics: Boolean = true, backToHome: Boolean = false): Boolean {
        this.backToHome = backToHome
        when (id) {
            R.id.nav_home -> {
                fragment = FragmentHome()
                home = true
                actionBar?.title = title
                if (logAnalytics) {
                    AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.HOME)
                }
            }
            R.id.nav_all -> {
                fragment = FragmentCategory()
                home = false
                bundle.putInt(FragmentCategory.TAG_CATEGORY, -1)
                actionBar?.title = title
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.ALL_PLACES)
            }
            R.id.nav_map -> {
                val i = Intent(applicationContext, ActivityMaps::class.java)
                home = false
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.MAP)
                startActivity(i)
            }
            R.id.nav_search -> {
                val i = Intent(applicationContext, ActivitySearch::class.java)
                home = false
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.SEARCH)
                startActivity(i)
            }
            R.id.nav_favorites -> {
                fragment = FragmentCategory()
                home = false
                bundle.putInt(FragmentCategory.TAG_CATEGORY, -2)
                actionBar?.title = title
                if (logAnalytics) {
                    AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.FAVORITES)
                }
            }
            R.id.nav_news -> {
                val i = Intent(this, ActivityNotifications::class.java)
                home = false
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.NOTIFICATIONS)
                startActivity(i)
            }
            R.id.nav_profile -> {
//                val j = Intent(this, ActivityProfile::class.java)
//                home = false
//                AnalyticsConstants.logEvent(AnalyticsConstants.SELECT_MENU_ITEM, AnalyticsConstants.ACTION_PROFILE)
//                startActivity(j)
            }

            R.id.nav_featured -> {
                openFragmentCategory(title, 0)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_FEATURED)
            }
            R.id.nav_shopping -> {
                openFragmentCategory(title, 1)
            }
            R.id.nav_pharmacy -> {
                openFragmentCategory(title, 2)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_PHARMACY)
            }
            R.id.nav_gym -> {
                openFragmentCategory(title, 3)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_GYM_CENTER)
            }
            R.id.nav_food -> {
                openFragmentCategory(title, 4)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_RESTAURANTS)
            }
            R.id.nav_bar -> {
                openFragmentCategory(title, 5)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_BAR)
            }
            R.id.nav_fast_food -> {
                openFragmentCategory(title, 6)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_FAST_FOOD)
            }
            R.id.nav_delivery -> {
                openFragmentCategory(title, 7)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_DELIVERY)
            }
            R.id.nav_ice_cream_store -> {
                openFragmentCategory(title, 8)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_ICE_CREAM)
            }
            R.id.nav_hotels -> {
                openFragmentCategory(title, 9)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_HOTEL)
            }
            R.id.nav_temporary_rent -> {
                openFragmentCategory(title, 10)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_TEMPORARY_RENT)
            }
            R.id.nav_tour -> {
                openFragmentCategory(title, 11)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_TOURIST_DESTINATION)
            }
            R.id.nav_money -> {
                openFragmentCategory(title, 12)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_MONEY)
            }
            R.id.nav_bill_payments -> {
                openFragmentCategory(title, 13)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_BILL_PAYMENTS)
            }
            R.id.nav_apartment_rental -> {
                openFragmentCategory(title, 14)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_APARTMENT_RENTAL)
            }
            R.id.nav_taxi -> {
                openFragmentCategory(title, 15)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_TAXI)
            }
            R.id.nav_gas_station -> {
                openFragmentCategory(title, 16)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_GAS_STATION)
            }
            R.id.nav_transport -> {
                openFragmentCategory(title, 17)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_TRANSPORT)
            }
            R.id.nav_transport_tickets -> {
                openFragmentCategory(title, 18)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_TRANSPORT_TICKETS)
            }
            R.id.nav_clothing_stores -> {
                openFragmentCategory(title, 19)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_CLOTHING_STORES)
            }
            R.id.nav_big_stores -> {
                openFragmentCategory(title, 20)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_BIG_STORES)
            }
            R.id.nav_industrial_stores -> {
                openFragmentCategory(title, 21)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_INDUSTRIAL_STORES)
            }
            R.id.nav_art_and_design -> {
                openFragmentCategory(title, 22)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_ART)
            }
            R.id.nav_jobs -> {
                openFragmentCategory(title, 23)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_JOBS)
            }
            R.id.nav_emergency -> {
                openFragmentCategory(title, 24)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_EMERGENCIES)
            }

            //COMMERCE ITEMS
            R.id.nav_subscription -> {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.OPEN_REGISTER_FORM, true)
                ActionTools.directUrl(this, LINK_TO_SUBSCRIPTION_FORM)
            }
            R.id.nav_suggestions -> {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.OPEN_SUGGESTION_FORM, true)
                ActionTools.directUrl(this, LINK_TO_SUGGESTIONS_FORM)
            }
            R.id.nav_get_in_touch -> {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.OPEN_GET_IN_TOUCH, true)
                ActionTools.sendEmail(CONTACT_EMAIL, SUBJECT_EMAIL, "\n\n--\nMensaje enviado desde Simplicity App.", this)
            }

            //SETTINGS
            R.id.nav_rate_app -> {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.RATE_APP)
                ActionTools.rateAction(this@ActivityMain)
            }
            R.id.nav_setting -> {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.OPEN_SETTINGS)
                val i = Intent(applicationContext, ActivitySetting::class.java)
                startActivity(i)
            }

        }
        /* IMPORTANT : cat[index_array], index is start from 0 */

        if (home) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_content, fragment!!)
            fragmentTransaction.commit()
        } else if (fragment != null) {
            fragment?.arguments = bundle
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_content, fragment!!)
            fragmentTransaction.commit()
        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }


    fun doExitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this, R.string.press_again_exit_app, Toast.LENGTH_SHORT).show()
            exitTime = System.currentTimeMillis()
        } else {
            finish()
        }
    }

    override fun onResume() {
        updateFavoritesCounter(navigationView!!, R.id.nav_favorites, db!!.favoritesSize)
        super.onResume()
    }

    public override fun onStart() {
        active = true
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        active = false
    }


    private fun updateFavoritesCounter(nav: NavigationView, @IdRes itemId: Int, count: Int) {
        val view = nav.menu.findItem(itemId).actionView.findViewById<View>(R.id.counter) as TextView
        view.text = count.toString()
    }

    companion object {
        var active = false

        const val BACK_TO_HOME = "BACK_TO_HOME"

        private lateinit var instance: ActivityMain

        val ActivityMainInstance: ActivityMain
            get() {
                if (instance == null) {
                    instance = ActivityMain()
                }

                return instance
            }

        //TODO: Analizar este caso de FAB, no es el getbase
        fun animateFab(hide: Boolean) {
            val f_ab = ActivityMainInstance.findViewById<View>(R.id.floating_action_button) as com.getbase.floatingactionbutton.FloatingActionsMenu
            val moveY = if (hide) 2 * f_ab.height else 0
            f_ab.animate().translationY(moveY.toFloat()).setStartDelay(100).setDuration(400).start()
        }
    }
}
