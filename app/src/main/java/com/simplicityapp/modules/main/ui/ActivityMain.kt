package com.simplicityapp.modules.main.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
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
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.modules.places.ui.ActivitySearch

class ActivityMain : AppCompatActivity() {

    internal var home = false


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
    private var nav_header_lyt: RelativeLayout? = null

    private var exitTime: Long = 0

    var fragment: Fragment? = null
    val bundle = Bundle()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.enter_slide_in, R.anim.enter_slide_out)
        setContentView(R.layout.activity_main_places)
        instance = this

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
        initToolbar()
        initDrawerMenu()
        initFabButtons()
        cat = resources.getIntArray(R.array.id_category)
        onItemSelected(R.id.nav_home, getString(R.string.title_home))
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
            val intent = Intent(this, ActivityMaps::class.java)
            startActivity(intent)
            fab?.collapse()
            AnalyticsConstants.logEvent(AnalyticsConstants.HOME_FAB_OPTION_SELECTED, AnalyticsConstants.ACTION_FLOATING_BUTTON_MAP)
        }

        fab_button_search?.setOnClickListener {
            val intent = Intent(this, ActivitySearch::class.java)
            startActivity(intent)
            fab?.collapse()
            AnalyticsConstants.logEvent(AnalyticsConstants.HOME_FAB_OPTION_SELECTED, AnalyticsConstants.ACTION_FLOATING_BUTTON_SEARCH)
        }

        fab_button_favorites?.setOnClickListener {
            onItemSelected(R.id.nav_favorites, getString(R.string.title_nav_fav))
            fab?.collapse()
            AnalyticsConstants.logEvent(AnalyticsConstants.HOME_FAB_OPTION_SELECTED, AnalyticsConstants.ACTION_FLOATING_BUTTON_FAVORITES)
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

        val nav_header = navigationView?.getHeaderView(0)
        nav_header_lyt = nav_header?.findViewById<View>(R.id.nav_header_lyt) as RelativeLayout
        //nav_header_lyt.setBackgroundColor(DeviceTools.colorBrighter(sharedPref.getThemeColorInt()));
        nav_header.findViewById<View>(R.id.menu_nav_setting).setOnClickListener {
            val i = Intent(applicationContext, ActivitySetting::class.java)
            startActivity(i)
        }

    }

    override fun onBackPressed() {
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
                val i = Intent(applicationContext, ActivitySetting::class.java)
                AnalyticsConstants.logEvent(AnalyticsConstants.OPTIONS_ITEMS_SELECTED, AnalyticsConstants.ACTION_SETTINGS)
                startActivity(i)
            }
            R.id.action_rate -> {
                ActionTools.rateAction(this@ActivityMain)
                AnalyticsConstants.logEvent(AnalyticsConstants.OPTIONS_ITEMS_SELECTED, AnalyticsConstants.ACTION_RATE)
            }
            R.id.action_about -> {
                ActionTools.aboutAction(this@ActivityMain)
                AnalyticsConstants.logEvent(AnalyticsConstants.OPTIONS_ITEMS_SELECTED, AnalyticsConstants.ACTION_ABOUT)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openFragmentCategory(title: String, categoryId: Int, analyticsAction: String) {
        fragment = FragmentCategory()
        home = false
        bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![categoryId])
        actionBar?.title = title
        AnalyticsConstants.logEvent(AnalyticsConstants.MENU_ITEMS_SELECTED, analyticsAction)
    }

    private fun onItemSelected(id: Int, title: String): Boolean {

        when (id) {
            R.id.nav_home -> {
                fragment = FragmentHome()
                home = true
                actionBar?.title = title
                AnalyticsConstants.logEvent(AnalyticsConstants.MENU_ITEMS_SELECTED, AnalyticsConstants.ACTION_HOME)
            }
            R.id.nav_all -> {
                fragment = FragmentCategory()
                home = false
                bundle.putInt(FragmentCategory.TAG_CATEGORY, -1)
                actionBar?.title = title
                AnalyticsConstants.logEvent(AnalyticsConstants.MENU_ITEMS_SELECTED, AnalyticsConstants.ACTION_ALL_PLACES)
            }
            R.id.nav_map -> {
                val i = Intent(applicationContext, ActivityMaps::class.java)
                home = false
                AnalyticsConstants.logEvent(AnalyticsConstants.MENU_ITEMS_SELECTED, AnalyticsConstants.ACTION_MAP)
                startActivity(i)
            }
            R.id.nav_search -> {
                val i = Intent(applicationContext, ActivitySearch::class.java)
                home = false
                AnalyticsConstants.logEvent(AnalyticsConstants.MENU_ITEMS_SELECTED, AnalyticsConstants.ACTION_SEARCH)
                startActivity(i)
            }
            R.id.nav_favorites -> {
                fragment = FragmentCategory()
                home = false
                bundle.putInt(FragmentCategory.TAG_CATEGORY, -2)
                actionBar?.title = title
                AnalyticsConstants.logEvent(AnalyticsConstants.MENU_ITEMS_SELECTED, AnalyticsConstants.ACTION_FAVORITES)
            }
            R.id.nav_news -> {
                val i = Intent(this, ActivityNotifications::class.java)
                home = false
                AnalyticsConstants.logEvent(AnalyticsConstants.MENU_ITEMS_SELECTED, AnalyticsConstants.ACTION_NOTIFICATIONS)
                startActivity(i)
            }
            R.id.nav_profile -> {
//                val j = Intent(this, ActivityProfile::class.java)
//                home = false
//                AnalyticsConstants.logEvent(AnalyticsConstants.MENU_ITEMS_SELECTED, AnalyticsConstants.ACTION_PROFILE)
//                startActivity(j)
            }

            R.id.nav_featured -> {
                openFragmentCategory(title, 0, AnalyticsConstants.ACTION_FEATURED)
            }
            R.id.nav_shopping -> {
                openFragmentCategory(title, 1, AnalyticsConstants.ACTION_SHOPPING)
            }
            R.id.nav_pharmacy -> {
                openFragmentCategory(title, 2, AnalyticsConstants.ACTION_PHARMACY)
            }
            R.id.nav_gym -> {
                openFragmentCategory(title, 3, AnalyticsConstants.ACTION_GYM)
            }
            R.id.nav_food -> {
                openFragmentCategory(title, 4, AnalyticsConstants.ACTION_FOOD)
            }
            R.id.nav_bar -> {
                openFragmentCategory(title, 5, AnalyticsConstants.ACTION_BAR)
            }
            R.id.nav_fast_food -> {
                openFragmentCategory(title, 6, AnalyticsConstants.ACTION_FAST_FOOD)
            }
            R.id.nav_delivery -> {
                openFragmentCategory(title, 7, AnalyticsConstants.ACTION_DELIVERY)
            }
            R.id.nav_ice_cream_store -> {
                openFragmentCategory(title, 8, AnalyticsConstants.ACTION_ICE_CREAM_STORE)
            }
            R.id.nav_hotels -> {
                openFragmentCategory(title, 9, AnalyticsConstants.ACTION_HOTELS)
            }
            R.id.nav_temporary_rent -> {
                openFragmentCategory(title, 10, AnalyticsConstants.ACTION_TEMPORARY_RENT)
            }
            R.id.nav_tour -> {
                openFragmentCategory(title, 11, AnalyticsConstants.ACTION_TOUR)
            }
            R.id.nav_money -> {
                openFragmentCategory(title, 12, AnalyticsConstants.ACTION_MONEY)
            }
            R.id.nav_bill_payments -> {
                openFragmentCategory(title, 13, AnalyticsConstants.ACTION_BILL_PAYMENTS)
            }
            R.id.nav_apartment_rental -> {
                openFragmentCategory(title, 14, AnalyticsConstants.ACTION_APARTMENT_RENTAL)
            }
            R.id.nav_taxi -> {
                openFragmentCategory(title, 15, AnalyticsConstants.ACTION_TAXI)
            }
            R.id.nav_gas_station -> {
                openFragmentCategory(title, 16, AnalyticsConstants.ACTION_GAS_STATION)
            }
            R.id.nav_transport -> {
                openFragmentCategory(title, 17, AnalyticsConstants.ACTION_TRANSPORT)
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
        if (actionBar != null) {
            //DeviceTools.setActionBarColor(this, actionBar!!)
        }
        if (nav_header_lyt != null) {
            //nav_header_lyt.setBackgroundColor(DeviceTools.colorBrighter(sharedPref.getThemeColorInt()));
        }
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
