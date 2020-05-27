package com.simplicityapp.base.utils

import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.ImageView
import com.simplicityapp.R
import com.simplicityapp.base.config.AppConfig
import com.simplicityapp.base.config.SharedPref
import com.simplicityapp.base.config.ThisApplication
import com.simplicityapp.modules.places.model.Place
import com.simplicityapp.modules.settings.model.DeviceInfo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Collections

class Tools {

    companion object {
        /*
        ------------------------------------ Device Tools ------------------------------------------
        */

        fun needRequestPermission(): Boolean {
            return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
        }

        fun checkConnection(context: Context): Boolean {
            val conn = ConnectionDetector(context)
            return conn.isConnectingToInternet
        }

        fun getDeviceName(): String {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                model
            } else {
                "$manufacturer $model"
            }
        }

        fun getAndroidVersion(): String {
            return Build.VERSION.RELEASE + ""
        }

        fun getDeviceInfo(context: Context): DeviceInfo {
            var phoneID = Build.SERIAL
            try {
                phoneID = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: Exception) {
            }

            val deviceInfo = DeviceInfo()
            deviceInfo.device = getDeviceName()
            deviceInfo.email = phoneID
            deviceInfo.version = getAndroidVersion()
            deviceInfo.regid = SharedPref(context).fcmRegId
            deviceInfo.date_create = System.currentTimeMillis()

            return deviceInfo
        }

        fun getFormattedDateSimple(dateTime: Long?): String {
            val newFormat = SimpleDateFormat("MMM dd, yyyy")
            return newFormat.format(Date(dateTime!!))
        }

        fun getFormattedDate(dateTime: Long?): String {
            val newFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm")
            return newFormat.format(Date(dateTime!!))
        }



        fun configStaticMap(act: Activity, googleMap: GoogleMap, place: Place): GoogleMap {
            // set map type
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            // Enable / Disable zooming controls
            googleMap.uiSettings.isZoomControlsEnabled = false
            // Enable / Disable my location button
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            // Enable / Disable Compass icon
            googleMap.uiSettings.isCompassEnabled = false
            // Enable / Disable Rotate gesture
            googleMap.uiSettings.isRotateGesturesEnabled = false
            // Enable / Disable zooming functionality
            googleMap.uiSettings.isZoomGesturesEnabled = false
            // enable traffic layer
            googleMap.isTrafficEnabled
            googleMap.isTrafficEnabled = false
            googleMap.uiSettings.isScrollGesturesEnabled = false
            googleMap.uiSettings.isMapToolbarEnabled = false

            val inflater = act.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val marker_view = inflater.inflate(R.layout.maps_marker, null)
            (marker_view.findViewById(R.id.marker_bg) as ImageView).setColorFilter(act.resources.getColor(R.color.colorMarker))

            val cameraPosition = CameraPosition.Builder().target(place.position).zoom(12f).build()
            val markerOptions = MarkerOptions().position(place.position)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(UITools.createBitmapFromView(act, marker_view)))
            googleMap.addMarker(markerOptions)
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            return googleMap
        }

        fun configActivityMaps(googleMap: GoogleMap): GoogleMap {
            // set map type
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            // Enable / Disable zooming controls
            googleMap.uiSettings.isZoomControlsEnabled = true

            // Enable / Disable Compass icon
            googleMap.uiSettings.isCompassEnabled = true
            // Enable / Disable Rotate gesture
            googleMap.uiSettings.isRotateGesturesEnabled = true
            // Enable / Disable zooming functionality
            googleMap.uiSettings.isZoomGesturesEnabled = true

            googleMap.uiSettings.isScrollGesturesEnabled = true
            googleMap.uiSettings.isMapToolbarEnabled = true

            return googleMap
        }

        /*
        -------------------------------------- List Tools ------------------------------------------
        */
        fun shuffleItems(items: List<Place>): List<Place> {
            return items.shuffled()
        }

        /*
        -------------------------------------- Location Tools --------------------------------------
        */

        private fun calculateDistance(from: LatLng, to: LatLng): Float {
            val start = Location("")
            start.latitude = from.latitude
            start.longitude = from.longitude

            val end = Location("")
            end.latitude = to.latitude
            end.longitude = to.longitude

            val distInMeters = start.distanceTo(end)
            var resultDist = 0f
            resultDist = if (AppConfig.DISTANCE_METRIC_CODE == "KILOMETER") {
                distInMeters / 1000
            } else {
                (distInMeters * 0.000621371192).toFloat()
            }
            return resultDist
        }

        fun filterItemsWithDistance(act: Activity, items: List<Place>): List<Place> {
            if (AppConfig.SORT_BY_DISTANCE) { // checking for distance sorting
                val curLoc = getCurrentLocation(act)
                if (curLoc != null) {
                    return getSortedDistanceList(items, curLoc)
                }
            }
            return items
        }

        fun itemsWithDistance(ctx: Context, items: List<Place>): List<Place> {
            if (AppConfig.SORT_BY_DISTANCE) { // checking for distance sorting
                val curLoc = getCurrentLocation(ctx)
                if (curLoc != null) {
                    return getDistanceList(items, curLoc)
                }
            }
            return items
        }

        fun getDistanceList(places: List<Place>, curLoc: LatLng): List<Place> {
            if (places.size > 0) {
                for (p in places) {
                    p.distance = calculateDistance(curLoc, p.position)
                }
            }
            return places
        }

        fun getSortedDistanceList(places: List<Place>, curLoc: LatLng): List<Place> {
            val result = ArrayList<Place>()
            if (places.size > 0) {
                for (i in places.indices) {
                    val p = places[i]
                    p.distance = calculateDistance(curLoc, p.position)
                    result.add(p)
                }
                Collections.sort(result) { p1, p2 -> java.lang.Float.compare(p1.distance, p2.distance) }
            } else {
                return places
            }
            return result
        }

        fun getCurrentLocation(ctx: Context): LatLng? {
            if (PermissionUtil.isLocationGranted(ctx)) {
                val manager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    var loc: Location? = ThisApplication.instance?.location
                    if (loc == null) {
                        loc = getLastKnownLocation(ctx)
                        ThisApplication.instance?.location = loc
                    }
                    if (loc != null) {
                        return LatLng(loc.latitude, loc.longitude)
                    }
                }
            }
            return null
        }

        fun getLastKnownLocation(ctx: Context): Location? {
            val mLocationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationListener = requestLocationUpdate(mLocationManager)
            val providers = mLocationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = mLocationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    // Found best last known location: %s", l);
                    bestLocation = l
                }
            }
            mLocationManager.removeUpdates(locationListener)
            return bestLocation
        }

        private fun requestLocationUpdate(manager: LocationManager): LocationListener {
            // Define a listener that responds to location updates
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {}

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {}
            }

            // Register the listener with the Location Manager to receive location updates
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
            return locationListener
        }

        fun getFormatedDistance(distance: Float): String {
            val df = DecimalFormat()
            df.maximumFractionDigits = 1
            return df.format(distance.toDouble()) + " " + AppConfig.DISTANCE_METRIC_STR
        }
    }
}