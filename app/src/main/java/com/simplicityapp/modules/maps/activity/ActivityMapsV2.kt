package com.simplicityapp.modules.maps.activity

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.simplicityapp.R
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.persistence.db.DatabaseHandler
import com.simplicityapp.base.persistence.preferences.SharedPref
import com.simplicityapp.base.utils.PermissionUtil
import com.simplicityapp.base.utils.Tools.Companion.configActivityMaps
import com.simplicityapp.base.utils.Tools.Companion.getLastKnownLocation
import com.simplicityapp.baseui.utils.UITools.Companion.createBitmapFromView
import com.simplicityapp.databinding.ActivityMapsBinding
import com.simplicityapp.modules.places.activity.ActivityPlaceDetail.Companion.navigate
import com.simplicityapp.modules.places.model.Category
import com.simplicityapp.modules.places.model.Place
import java.util.*

class ActivityMapsV2 : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
    private lateinit var db: DatabaseHandler
    private lateinit var sharedPref: SharedPref
    private var mClusterManager: ClusterManager<Place?>? = null
    private var placeMarkerRenderer: PlaceMarkerRenderer? = null

    private var parent_view: View? = null
    // for single place
    private var extPlace: Place? = null
    private var isSinglePlace = false
    var hashMapPlaces = HashMap<String, Place>()
    // id category
    private var catId = -1
    private var currentCategory: Category? = null
    // view for custom marker
    private var icon: ImageView? = null
    private var imageView: ImageView? = null
    private var markerView: View? = null

    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        parent_view = findViewById<View>(android.R.id.content)
        overridePendingTransition(R.anim.enter_slide_in, R.anim.enter_slide_out)
        sharedPref = SharedPref(this)
        db = DatabaseHandler(this)

        (intent.getSerializableExtra(EXTRA_OBJ) as? Place)?.let { extPlace = it }
        isSinglePlace = extPlace != null

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        markerView = inflater.inflate(R.layout.maps_marker, null)
        icon = markerView?.findViewById<View>(R.id.marker_icon) as ImageView
        imageView = markerView?.findViewById<View>(R.id.marker_bg) as ImageView
        initMapFragment()
        initToolbar()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = configActivityMaps(googleMap)
        var location: CameraUpdate? = null
        if (isSinglePlace) {
            imageView?.setColorFilter(resources.getColor(R.color.colorMarker))
            extPlace?.let {
                val markerOptions = MarkerOptions().title(it.name).position(it.position)
                markerOptions.icon(
                    BitmapDescriptorFactory.fromBitmap(
                        createBitmapFromView(
                            this@ActivityMapsV2,
                            markerView!!
                        )
                    )
                )
                mMap?.addMarker(markerOptions)
                location = CameraUpdateFactory.newLatLngZoom(it.position, 12f)
                actionBar?.setTitle(it.name)
            }
        } else {
            location = CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    sharedPref.regionLat.toDouble(),
                    sharedPref.regionLon.toDouble()
                ), Constant.city_zoom
            )
            mClusterManager = ClusterManager(this, mMap)
            placeMarkerRenderer =
                PlaceMarkerRenderer(
                    this,
                    mMap,
                    mClusterManager
                )
            mClusterManager!!.setRenderer(placeMarkerRenderer)
            mMap!!.setOnCameraChangeListener(mClusterManager)
            loadClusterManager(db.allPlace)
        }
        location?.let { mMap?.animateCamera(it) }

        mMap?.setOnInfoWindowClickListener { marker ->
            val place: Place? = if (hashMapPlaces[marker.id] != null) {
                hashMapPlaces[marker.id]
            } else {
                extPlace
            }
            navigate(
                this@ActivityMapsV2,
                parent_view!!,
                place!!,
                AnalyticsConstants.SELECT_MAP_PLACE
            )
        }
        showMyLocation()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showMyLocation() {
        if (PermissionUtil.isLocationGranted(this)) { // Enable / Disable my location button
            mMap?.uiSettings?.isMyLocationButtonEnabled = true
            mMap?.isMyLocationEnabled = true
            mMap?.setOnMyLocationButtonClickListener {
                try {
                    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        showAlertDialogGps()
                    } else {
                        val loc = getLastKnownLocation(this@ActivityMapsV2)
                        val myCam = CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                loc!!.latitude,
                                loc.longitude
                            ), 12f
                        )
                        mMap!!.animateCamera(myCam)
                    }
                } catch (e: Exception) {
                }
                true
            }
        }
    }

    private fun loadClusterManager(places: List<Place>) {
        mClusterManager?.clearItems()
        val placesCleaned = cleanPlaces(places)
        mClusterManager?.addItems(placesCleaned)
    }

    private fun initToolbar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setTitle(R.string.activity_title_maps)
    }

    private fun initMapFragment() { // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private inner class PlaceMarkerRenderer(
        context: Context?,
        map: GoogleMap?,
        clusterManager: ClusterManager<Place?>?
    ) : DefaultClusterRenderer<Place>(context, map, clusterManager) {

        override fun onBeforeClusterItemRendered(
            item: Place,
            markerOptions: MarkerOptions
        ) {
            if (catId == -1) { // all place
                icon?.setImageResource(R.drawable.round_shape)
            } else {
                icon?.setImageResource(currentCategory!!.icon)
            }
            imageView?.setColorFilter(resources.getColor(R.color.colorPrimary))
            markerOptions.title(item.name)
            markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(
                    createBitmapFromView(
                        this@ActivityMapsV2,
                        markerView!!
                    )
                )
            )
            if (extPlace != null && extPlace!!.place_id == item.place_id) {
                markerOptions.visible(false)
            }
        }

        override fun onClusterItemRendered(item: Place, marker: Marker) {
            hashMapPlaces[marker.id] = item
            super.onClusterItemRendered(item, marker)
        }

    }

    private fun showAlertDialogGps() {
        val builder =
            AlertDialog.Builder(this)
        builder.setMessage(R.string.dialog_content_gps)
        builder.setPositiveButton(R.string.YES) { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
        builder.setNegativeButton(R.string.NO) { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.exit_slide_in, R.anim.exit_slide_out)
    }

    private fun cleanPlaces(places: List<Place>): List<Place> {
        val listPlace: MutableList<Place> = mutableListOf()
        places.forEach {
            if (it.hasLatLngPosition()) {
                listPlace.add(it)
            }
        }
        return listPlace.toList()
    }

    companion object {
        const val EXTRA_OBJ = "key.EXTRA_OBJ"
    }
}