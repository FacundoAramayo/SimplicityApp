package com.simplicityapp.modules.places.activity

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
import android.view.View.VISIBLE
import android.view.WindowManager
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import com.simplicityapp.baseui.adapter.AdapterImageList
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.persistence.preferences.SharedPref
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.CONTENT_PLACE
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SELECT_PLACE_ADDRESS
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SELECT_PLACE_FAVORITES_ADD
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SELECT_PLACE_FAVORITES_REMOVE
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SELECT_PLACE_OPEN_NAVIGATION
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SELECT_PLACE_PHONE
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SELECT_PLACE_WEB_SITE
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.VIEW_PLACE
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.logAnalyticsEvent
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.logAnalyticsShare
import com.simplicityapp.base.utils.ActionTools
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.baseui.utils.UITools
import com.simplicityapp.databinding.ActivityPlaceDetailsBinding
import com.simplicityapp.modules.main.activity.ActivityMain
import com.simplicityapp.modules.places.model.Images
import com.simplicityapp.modules.places.model.Place
import com.simplicityapp.R
import com.simplicityapp.base.BaseActivity
import com.simplicityapp.base.config.AppConfig.*
import com.simplicityapp.base.config.Constant.*
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SELECT_PLACE_FACEBOOK
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SELECT_PLACE_INSTAGRAM
import com.simplicityapp.base.config.analytics.AnalyticsConstants.Companion.SELECT_PLACE_WHATSAPP
import com.simplicityapp.base.utils.show
import java.lang.Exception

class ActivityPlaceDetail : BaseActivity() {

    private var place: Place? = null
    private lateinit var parentView: View
    private var isFromNotif = false
    private lateinit var binding: ActivityPlaceDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        initActivity(binding)
        parentView = findViewById(android.R.id.content)
        ViewCompat.setTransitionName(binding.placeAppBarLayout,
            EXTRA_OBJECT
        )
        logAnalyticsEvent(VIEW_PLACE, place?.name.orEmpty())
    }

    override fun getArguments() {
        place = intent.getSerializableExtra(EXTRA_OBJECT) as Place
        isFromNotif = intent.getBooleanExtra(EXTRA_NOTIF_FLAG, false)
    }

    override fun initUI() {
        configFab()
        setupToolbar(place!!.name)
    }

    private fun displayData(place: Place) {

        binding.details.apply {
            if (place.categoriesList.isNullOrEmpty().not()) {
                textViewPlaceCategories?.visibility = VISIBLE
                textViewPlaceCategories?.text = place.categoriesList.orEmpty()
            }

            if (place.hasLatLngPosition()
                and !place.address.isNullOrEmpty()
                and (place.distance != -1f)
            ) {
                val distance = place.distance
                placeLytDistance.visibility = VISIBLE
                cardViewContact?.show()
                placeDistance.text = Tools.getFormattedDistance(distance)
            }
            if (place.address.isNullOrEmpty()) { placeLytAddress.visibility = GONE } else cardViewContact?.show()
            if (place.hasLatLngPosition().not()) { placeHowToGet.visibility = GONE } else cardViewContact?.show()
            if (place.phone.isNullOrEmpty()) { placeLytPhone.visibility = GONE } else cardViewContact?.show()

            if (place.whatsapp.isNullOrEmpty()) { placeLytWhatsapp?.visibility = GONE } else cardViewSocialMedia?.show()
            if (place.instagram.isNullOrEmpty()) { placeLytInstagram?.visibility = GONE } else cardViewSocialMedia?.show()
            if (place.facebook.isNullOrEmpty()) { placeLytFacebook?.visibility = GONE } else cardViewSocialMedia?.show()
            if (place.website.isNullOrEmpty()) { placeLytWebsite.visibility = GONE } else cardViewSocialMedia?.show()

            if (place.description.isNullOrEmpty()) { placeCardViewDescription.visibility = GONE }

            placeAddress.text = place.address
            placePhone.text = place.phone
            placeWhatsapp?.text = place.whatsapp
            placeInstagram?.text = place.instagram
            placeFacebook?.text = place.facebook
            placeWebsite.text = place.website

            val htmlData = "$WEB_VIEW_HTML_CONFIG $FONT_FAMILY ${place.description}"
            placeDescriptionWebView.run {
                settings?.builtInZoomControls = true
                setBackgroundColor(Color.TRANSPARENT)
                webChromeClient = WebChromeClient()
                loadData(htmlData, WEB_VIEW_MIME_TYPE, null)
                settings.javaScriptEnabled = true
                // disable scroll on touch
                setOnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE }
            }

            db?.getListImageByPlaceId(place.place_id)?.let {
                setImageGallery(it)
            }
            try {
                if ((place.images.size > 1).not()) {
                    placeCardViewPhotos.visibility = GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        loadPlaceData()
        setOnClickListeners(applicationContext)
        setToolbarColor()
        binding.details.placeDescriptionWebView?.onResume()
        super.onResume()
    }

    private fun setOnClickListeners(context: Context) {
        binding.details.apply {
            placeAddress.setOnClickListener {
                if (place!!.hasLatLngPosition()) {
                    logAnalyticsEvent(SELECT_PLACE_ADDRESS, place?.name.orEmpty(), false)
                    val uri = Uri.parse("http://maps.google.com/maps?q=loc: ${place!!.lat},${place!!.lng}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            placeHowToGet.setOnClickListener {
                logAnalyticsEvent(SELECT_PLACE_OPEN_NAVIGATION, place?.name.orEmpty(), false)
                val navigation = Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=${place!!.lat},${place!!.lng}"))
                startActivity(navigation)
            }
            placeLytPhone.setOnClickListener {
                if (!place!!.phone.isNullOrEmpty()) {
                    logAnalyticsEvent(SELECT_PLACE_PHONE, place?.name.orEmpty(), false)
                    ActionTools.dialNumber(context, place!!.phone!!, resources.getString(R.string.fail_dial_number))
                } else {
                    Snackbar.make(parentView, R.string.fail_dial_number, Snackbar.LENGTH_SHORT).show()
                }
            }
            placeLytWhatsapp?.setOnClickListener {
                if (!place?.website.isNullOrEmpty()) {
                    val whatsApp = place?.whatsapp.orEmpty().replace("+", "")
                    val messageFromApp = resources.getString(R.string.message_from_app)
                    logAnalyticsEvent(SELECT_PLACE_WHATSAPP, place?.name.orEmpty(), false)
                    ActionTools.directUrl(context, "$WHATSAPP_API_STRING$whatsApp$WHATSAPP_TEXT_STRING$messageFromApp", resources.getString(R.string.fail_open_whatsapp))
                } else {
                    Snackbar.make(parentView, R.string.fail_open_whatsapp, Snackbar.LENGTH_SHORT).show()
                }
            }
            placeLytInstagram?.setOnClickListener {
                if (!place?.instagram.isNullOrEmpty()) {
                    logAnalyticsEvent(SELECT_PLACE_INSTAGRAM, place?.name.orEmpty(), false)
                    ActionTools.directUrl(context, place?.instagram.orEmpty(), resources.getString(R.string.fail_open_website))
                } else {
                    Snackbar.make(parentView, R.string.fail_open_website, Snackbar.LENGTH_SHORT).show()
                }
            }
            placeLytFacebook?.setOnClickListener {
                if (!place?.website.isNullOrEmpty()) {
                    logAnalyticsEvent(SELECT_PLACE_FACEBOOK, place?.name.orEmpty(), false)
                    ActionTools.directUrl(context, place?.facebook.orEmpty(), resources.getString(R.string.fail_open_website))
                } else {
                    Snackbar.make(parentView, R.string.fail_open_website, Snackbar.LENGTH_SHORT).show()
                }
            }
            placeLytWebsite.setOnClickListener {
                if (!place?.website.isNullOrEmpty()) {
                    logAnalyticsEvent(SELECT_PLACE_WEB_SITE, place?.name.orEmpty(), false)
                    ActionTools.directUrl(context, place?.website.orEmpty(), resources.getString(R.string.fail_open_website))
                } else {
                    Snackbar.make(parentView, R.string.fail_open_website, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        binding.placeFab.setOnClickListener {
            if (db?.isFavoritesExist(place!!.place_id) == true) {
                db?.deleteFavorites(place!!.place_id)
                Snackbar.make(parentView, place!!.name + " " + getString(R.string.remove_favorite), Snackbar.LENGTH_SHORT).show()
                logAnalyticsEvent(SELECT_PLACE_FAVORITES_REMOVE, place?.name.orEmpty(), true)
                fabToggle(false)
            } else {
                db?.addFavorites(place!!.place_id)
                Snackbar.make(parentView, place!!.name + " " + getString(R.string.add_favorite), Snackbar.LENGTH_SHORT).show()
                logAnalyticsEvent(SELECT_PLACE_FAVORITES_ADD, place?.name.orEmpty(), true)
                fabToggle(true)
            }
        }
    }

    private fun setImageGallery(images: List<Images>) {
        val newImages = ArrayList<Images>()
        val newImagesStr = ArrayList<String>()
        newImages.add(Images(place!!.place_id, place!!.image))
        newImages.addAll(images)
        for (img in newImages) {
            newImagesStr.add(getURLimgPlace(img.name))
        }

        val adapter = AdapterImageList(this, newImages)
        binding.details.placeGalleryRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.details.placeGalleryRecycler.adapter = adapter

        adapter.setOnItemClickListener { view, viewModel, pos ->
            logAnalyticsEvent(AnalyticsConstants.SELECT_PLACE_PHOTO, place?.name, false)
            val i = Intent(this@ActivityPlaceDetail, ActivityFullScreenImage::class.java)
            i.putExtra(EXTRA_POS, pos)
            i.putStringArrayListExtra(EXTRA_IMGS, newImagesStr)
            startActivity(i)
        }
        place?.images = newImages
    }

    private fun fabToggle(isFavorite: Boolean) {
        if (isFavorite) {
            binding.placeFab.setImageResource(R.drawable.ic_nav_favorites)
        } else {
            binding.placeFab.setImageResource(R.drawable.ic_nav_favorites_outline)
        }
    }

    private fun configFab() {
        if (db?.isFavoritesExist(place!!.place_id) == true) {
            binding.placeFab.setImageResource(R.drawable.ic_nav_favorites)
        } else {
            binding.placeFab.setImageResource(R.drawable.ic_nav_favorites_outline)
        }
    }

    private fun setupToolbar(name: String?) {
        setSupportActionBar(binding.placeToolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = resources.getString(R.string.empty_string)
        binding.placeToolbarTitle.text = name
        binding.placeImage.apply {
            UITools.displayImage(context, this, Constant.getURLimgPlace(place!!.image))
        }
    }

    private fun setToolbarColor() {
        Handler().postDelayed({
            val bitmap = UITools.createBitmapFromView(binding.placeImage as View, 0, 0)
            binding.placeCollapsingToolbar.apply {
                bitmap.let {
                    val builder = Palette.Builder(bitmap)
                    val palette = builder.generate()
                    palette.vibrantSwatch?.let {
                        setToolbarColor(
                            this,
                            palette.getVibrantColor(
                                SharedPref(
                                    context
                                ).themeColorInt),
                            palette.getVibrantColor(resources.getColor(R.color.colorPrimaryDark))
                        )
                        return@apply
                    }
                    palette.darkVibrantSwatch?.let {
                        setToolbarColor(
                            this,
                            palette.getDarkVibrantColor(
                                SharedPref(
                                    context
                                ).themeColorInt),
                            palette.getDarkVibrantColor(resources.getColor(R.color.colorPrimaryDark))
                        )
                        return@apply
                    }
                    palette.lightVibrantSwatch?.let {
                        setToolbarColor(
                            this,
                            palette.getLightVibrantColor(
                                SharedPref(
                                    context
                                ).themeColorInt),
                            palette.getLightVibrantColor(resources.getColor(R.color.colorPrimaryDark))
                        )
                    }
                }
            }
        }, 800)
    }

    private fun setToolbarColor(toolbar: CollapsingToolbarLayout, color: Int, colorDark: Int) {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.statusBarColor = colorDark
        toolbar.setContentScrimColor(color)
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
            logAnalyticsEvent(AnalyticsConstants.SELECT_PLACE_SHARE, place?.name, false)
            logAnalyticsShare(CONTENT_PLACE, place?.name.orEmpty())
            ActionTools.methodShare(this@ActivityPlaceDetail, place!!)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        backAction()
    }

    override fun onPause() {
        super.onPause()
        binding.details.placeDescriptionWebView.onPause()
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
        place = db?.getPlace(place!!.place_id)
        displayData(place!!)
    }

    companion object {

        // give preparation animation activity transition
        fun navigate(activity: AppCompatActivity?, sharedView: View, p: Place, analyticsEvent: String) {
            logAnalyticsEvent(analyticsEvent, p.name, false)
            val intent = Intent(activity, ActivityPlaceDetail::class.java)
            intent.putExtra(EXTRA_OBJECT, p)
            activity?.let {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedView,
                    EXTRA_OBJECT
                )
                ActivityCompat.startActivity(activity, intent, options.toBundle())
            }
        }

        fun navigateBase(context: Context, obj: Place, from_notif: Boolean?): Intent {
            val i = Intent(context, ActivityPlaceDetail::class.java)
            i.putExtra(EXTRA_OBJECT, obj)
            i.putExtra(EXTRA_NOTIF_FLAG, from_notif)
            return i
        }
    }
}
