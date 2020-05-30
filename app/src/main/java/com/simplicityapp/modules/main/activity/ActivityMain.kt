package com.simplicityapp.modules.main.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.config.AppConfig
import com.simplicityapp.base.persistence.db.DatabaseHandler
import com.simplicityapp.base.persistence.preferences.SharedPref
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.config.Constant.*
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.modules.categories.fragment.CategoryFragment
import com.simplicityapp.modules.main.fragment.FragmentHome
import com.simplicityapp.modules.places.activity.ActivityMaps
import com.simplicityapp.modules.notifications.activity.ActivityNotifications
import com.simplicityapp.modules.settings.activity.ActivitySetting
import com.simplicityapp.modules.categories.activity.CategoriesSelectorActivity
import com.simplicityapp.modules.places.activity.ActivitySearch
import com.simplicityapp.R

class ActivityMain : AppCompatActivity() {

    private val bundle = Bundle()
    private var home = false
    private var backToHome = false
    private var firstRun: Boolean = false
    private var exitTime: Long = 0
    private var cat: IntArray? = null
    private var navigationView: NavigationView? = null
    private var db: DatabaseHandler? = null
    private var sharedPref: SharedPref? = null
    private var fragment: Fragment? = null
    private var drawerLayout: DrawerLayout? = null
    var actionBar: ActionBar? = null
    var toolbar: Toolbar? = null

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
        sharedPref =
            SharedPref(this)

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
        toolbar = findViewById(R.id.toolbar)
        initToolbar()
        initDrawerMenu()
        onItemSelected(R.id.nav_home, getString(R.string.title_home), false)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
    }

    private fun initDrawerMenu() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = object : ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            override fun onDrawerOpened(drawerView: View) {
                updateFavoritesCounter(navigationView!!, R.id.nav_favorites, db!!.favoritesSize)
                super.onDrawerOpened(drawerView)
            }
        }
        drawerLayout?.setDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById(R.id.nav_view)
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
    }

    fun mapIntent() {
        val intent = Intent(this, ActivityMaps::class.java)
        startActivity(intent)
    }

    fun favoritesIntent() {
        onItemSelected(R.id.nav_favorites, getString(R.string.title_nav_fav), false)
    }

    fun categorySelectorIntent(guideType: String) {
        val intent = Intent(this, CategoriesSelectorActivity::class.java)
        intent.putExtra(GUIDE_TYPE, guideType)
        startActivity(intent)
    }

    private fun getMenuItemId(categoryId: Int): Int? {
        return when (categoryId) {
            0 -> R.id.nav_featured
            else -> null
        }
    }

    override fun onBackPressed() {
        if (backToHome) {
            onItemSelected(R.id.nav_home, resources.getString(R.string.title_home), false)
            return
        }

        if (drawerLayout?.isDrawerOpen(GravityCompat.START) == false) {
            drawerLayout?.openDrawer(GravityCompat.START)
        } else {
            exitApp()
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
        fragment = CategoryFragment()
        home = false
        bundle.putInt(CategoryFragment.TAG_CATEGORY, cat!![categoryId])
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
//            R.id.nav_all -> {
//                fragment =
//                    CategoryFragment()
//                home = false
//                bundle.putInt(CategoryFragment.TAG_CATEGORY, -1)
//                actionBar?.title = title
//                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.ALL_PLACES)
//            }
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
                fragment =
                    CategoryFragment()
                home = false
                bundle.putInt(CategoryFragment.TAG_CATEGORY, -2)
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
            R.id.nav_commercial_guide -> {
                home = false
                categorySelectorIntent(COMMERCIAL_GUIDE)
            }
            R.id.nav_jobs_guide -> {
                home = false
                categorySelectorIntent(JOBS_GUIDE)
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_CATEGORY, AnalyticsConstants.CATEGORY_JOBS)
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


    private fun exitApp() {
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
                    instance =
                        ActivityMain()
                }

                return instance
            }
    }
}
