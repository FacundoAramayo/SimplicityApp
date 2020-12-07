package com.simplicityapp.modules.main.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.simplicityapp.BuildConfig
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.config.AppConfig
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.config.Constant.*
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.modules.categories.fragment.CategoryFragment
import com.simplicityapp.modules.main.fragment.FragmentHome
import com.simplicityapp.modules.settings.activity.ActivitySetting
import com.simplicityapp.modules.categories.activity.CategoriesSelectorActivity
import com.simplicityapp.modules.places.activity.ActivitySearch
import com.simplicityapp.R
import com.simplicityapp.base.BaseActivity
import com.simplicityapp.base.config.AppConfig.*
import com.simplicityapp.databinding.ActivityMainPlacesBinding
import com.simplicityapp.modules.maps.activity.ActivityMapsV2
import com.simplicityapp.modules.notifications.activity.ActivityNotificationsV2

class ActivityMain : BaseActivity() {

    private lateinit var binding: ActivityMainPlacesBinding
    private val bundle = Bundle()
    private var home = false
    private var backToHome = false
    private var firstRun: Boolean = false
    private var exitTime: Long = 0
    private var categories: IntArray? = null
    private var fragment: Fragment? = null
    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
    private var regionId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        overridePendingTransition(R.anim.enter_slide_in, R.anim.enter_slide_out)
        binding = ActivityMainPlacesBinding.inflate(layoutInflater)
        initActivity(binding)
        instance = this
        configOpenApp()
    }

    /**
     * Código para configurar las preferencias una vez que el usuario realizó con éxito su registro
     */
    private fun configOpenApp() {
        val prefs = getSharedPreferences(Constant.PREFS_NAME, Context.MODE_PRIVATE)
        val currentVersionCode = BuildConfig.VERSION_CODE
        prefs.edit().putInt(Constant.PREF_VERSION_CODE_KEY, currentVersionCode).apply()
    }

    private fun checkRegion() {
        regionId = sharedPref.regionId
        if (regionId == NO_REGION_SELECTED) {
            val intent = Intent(this, RegionSelectorActivity::class.java)
            startActivity(intent)
        }
    }

    override fun getArguments() {
        if (intent?.extras?.get(IS_FIRST_OPEN) == true) {
            firstRun = true
            AnalyticsConstants.logAnalyticsSignUp()
        }
    }

    override fun initUI() {
        categories = resources.getIntArray(R.array.id_category)
        initToolbar()
        initDrawerMenu()
        onItemSelected(R.id.nav_home, getString(R.string.title_home), false)
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
    }

    private fun initDrawerMenu() {
        val toggle = object : ActionBarDrawerToggle(this, binding.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                db?.getFavoritesCount(sharedPref.regionId)?.let {
                    updateFavoritesCounter(binding.navView, R.id.nav_favorites, it)
                }
                super.onDrawerOpened(drawerView)
            }
        }
        binding.drawerLayout.setDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener { item -> onItemSelected(item.itemId, item.title.toString()) }

        /*CONFIG UI WITH AppConfig*/
        binding.navView.run {
            if (!AppConfig.ENABLE_CONTENT_INFO) menu.removeItem(R.id.nav_news)
            if (!AppConfig.ENABLE_USER_PROFILE) menu.removeItem(R.id.nav_profile)
            if (!AppConfig.ENABLE_EMPTY_CATEGORIES and !firstRun) { removeEmptyCategories() }
        }
    }

    private fun removeEmptyCategories() {
        categories?.forEach {
            val placesByCategory = db?.getAllPlaceByCategory(it)?.size
            if (placesByCategory == 0) {
                val categoryId = getMenuItemId(it)
                categoryId?.let { binding.navView.menu.removeItem(categoryId) }
            }
        }
    }

    fun searchIntent() {
        val intent = Intent(this, ActivitySearch::class.java)
        startActivity(intent)
    }

    fun mapIntent() {
        val intent = Intent(this, ActivityMapsV2::class.java)
        startActivity(intent)
    }

    fun favoritesIntent() {
        onItemSelected(R.id.nav_favorites, getString(R.string.title_nav_fav), false)
    }

    fun categorySelectorIntent(guideType: String? = null, categoryId: Int = 0) {
        val intent = Intent(this, CategoriesSelectorActivity::class.java)
        intent.putExtra(GUIDE_TYPE, guideType)
        intent.putExtra(TAG_CATEGORY, categoryId.toString())
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
        if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
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
        bundle.putInt(TAG_CATEGORY, categories!![categoryId])
        actionBar?.title = title
    }

    private fun onItemSelected(id: Int, title: String, logAnalytics: Boolean = true, backToHome: Boolean = false): Boolean {
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
            R.id.nav_map -> {
                val i = Intent(applicationContext, ActivityMapsV2::class.java)
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
                fragment = CategoryFragment()
                home = false
                bundle.putInt(TAG_CATEGORY, -2)
                actionBar?.title = title
                if (logAnalytics) {
                    AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.FAVORITES)
                }
            }
            R.id.nav_news -> {
                val i = Intent(this, ActivityNotificationsV2::class.java)
                home = false
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.NOTIFICATIONS)
                startActivity(i)
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
                ActionTools.directUrl(this, LINK_TO_SUBSCRIPTION_FORM, resources.getString(R.string.fail_open_website))
            }
            R.id.nav_suggestions -> {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.OPEN_SUGGESTION_FORM, true)
                ActionTools.directUrl(this, LINK_TO_SUGGESTIONS_FORM, resources.getString(R.string.fail_open_website))
            }
            R.id.nav_get_in_touch -> {
                AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.SELECT_MENU_ACTION, AnalyticsConstants.OPEN_GET_IN_TOUCH, true)
                ActionTools.sendEmail(CONTACT_EMAIL, SUBJECT_EMAIL, FOOTER_MESSAGE, this)
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

    fun openNews() {
        onItemSelected(R.id.nav_news, getString(R.string.title_nav_news))
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
        super.onResume()
        checkRegion()
        updateRegionTitle()
        db?.getFavoritesCount(sharedPref.regionId)?.let {
            updateFavoritesCounter(binding.navView, R.id.nav_favorites, it)
        }
    }

    public override fun onStart() {
        super.onStart()
        active = true
    }

    override fun onDestroy() {
        super.onDestroy()
        active = false
    }

    private fun updateFavoritesCounter(nav: NavigationView, @IdRes itemId: Int, count: Int) {
        val view = nav.menu.findItem(itemId).actionView.findViewById<View>(R.id.counter) as TextView
        view.text = count.toString()
    }

    private fun updateRegionTitle() {
        val navHeader = binding.navView.getHeaderView(0)
        val cityName = navHeader?.findViewById<TextView>(R.id.textView_city_name)
        val cityBox = navHeader?.findViewById<LinearLayout>(R.id.lyt_cityBox)
        cityName?.text = sharedPref.regionTitle
        cityBox?.setOnClickListener { changeRegion() }
    }

    private fun changeRegion() {
        val intent = Intent(this, RegionSelectorActivity::class.java)
        intent.putExtra(IS_FROM_HOME, true)
        startActivity(intent)
    }

    companion object {
        var active = false
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
