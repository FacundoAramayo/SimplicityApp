package com.simplicityapp.modules.places.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.simplicityapp.base.config.analytics.AnalyticsConstants;
import com.simplicityapp.base.config.Constant;
import com.simplicityapp.base.persistence.db.DatabaseHandler;
import com.simplicityapp.base.utils.Tools;
import com.simplicityapp.base.utils.PermissionUtil;
import com.simplicityapp.baseui.utils.UITools;
import com.simplicityapp.modules.places.model.Category;
import com.simplicityapp.modules.places.model.Place;
import com.simplicityapp.R;
import java.util.HashMap;
import java.util.List;

public class ActivityMaps extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_OBJ = "key.EXTRA_OBJ";

    private GoogleMap mMap;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private DatabaseHandler db;
    private ClusterManager<Place> mClusterManager;
    private View parent_view;
    private int cat[];
    private PlaceMarkerRenderer placeMarkerRenderer;

    // for single place
    private Place extPlace = null;
    private boolean isSinglePlace;
    HashMap<String, Place> hashMapPlaces = new HashMap<>();

    // id category
    private int catId = -1;

    private Category currentCategory;

    // view for custom marker
    private ImageView icon, imageView;
    private View markerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.enter_slide_in, R.anim.enter_slide_out);
        setContentView(R.layout.activity_maps);
        parent_view = findViewById(android.R.id.content);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        markerView = inflater.inflate(R.layout.maps_marker, null);
        icon = (ImageView) markerView.findViewById(R.id.marker_icon);
        imageView = (ImageView) markerView.findViewById(R.id.marker_bg);

        extPlace = (Place) getIntent().getSerializableExtra(EXTRA_OBJ);
        isSinglePlace = (extPlace != null);

        db = new DatabaseHandler(this);
        initMapFragment();
        initToolbar();

        cat = getResources().getIntArray(R.array.id_category);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = Tools.Companion.configActivityMaps(googleMap);
        CameraUpdate location;
        if (isSinglePlace) {
            imageView.setColorFilter(getResources().getColor(R.color.colorMarker));
            MarkerOptions markerOptions = new MarkerOptions().title(extPlace.getName()).position(extPlace.getPosition());
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(UITools.Companion.createBitmapFromView(ActivityMaps.this, markerView)));
            mMap.addMarker(markerOptions);
            location = CameraUpdateFactory.newLatLngZoom(extPlace.getPosition(), 12);
            actionBar.setTitle(extPlace.getName());
        } else {
            location = CameraUpdateFactory.newLatLngZoom(new LatLng(Constant.city_lat, Constant.city_lng), Constant.city_zoom);
            mClusterManager = new ClusterManager<>(this, mMap);
            placeMarkerRenderer = new PlaceMarkerRenderer(this, mMap, mClusterManager);
            mClusterManager.setRenderer(placeMarkerRenderer);
            mMap.setOnCameraChangeListener(mClusterManager);
            loadClusterManager(db.getAllPlace());
        }
        mMap.animateCamera(location);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Place place;
                if (hashMapPlaces.get(marker.getId()) != null) {
                    place = (Place) hashMapPlaces.get(marker.getId());
                } else {
                    place = extPlace;
                }
                ActivityPlaceDetail.Companion.navigate(ActivityMaps.this, parent_view, place, AnalyticsConstants.SELECT_MAP_PLACE);
            }
        });

        showMyLocation();
    }

    private void showMyLocation() {
        if (PermissionUtil.isLocationGranted(this)) {
            // Enable / Disable my location button
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    try {
                        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            showAlertDialogGps();
                        } else {
                            Location loc = Tools.Companion.getLastKnownLocation(ActivityMaps.this);
                            CameraUpdate myCam = CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 12);
                            mMap.animateCamera(myCam);
                        }
                    } catch (Exception e) {
                    }
                    return true;
                }
            });
        }
    }

    private void loadClusterManager(List<Place> places) {
        mClusterManager.clearItems();
        mClusterManager.addItems(places);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.activity_title_maps);
        //UITools.Companion.setActionBarColor(this, actionBar);
    }

    private void initMapFragment() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    private class PlaceMarkerRenderer extends DefaultClusterRenderer<Place> {
        public PlaceMarkerRenderer(Context context, GoogleMap map, ClusterManager<Place> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(Place item, MarkerOptions markerOptions) {
            if (catId == -1) { // all place
                icon.setImageResource(R.drawable.round_shape);
            } else {
                icon.setImageResource(currentCategory.getIcon());
            }
            imageView.setColorFilter(getResources().getColor(R.color.colorPrimary));
            markerOptions.title(item.getName());
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(UITools.Companion.createBitmapFromView(ActivityMaps.this, markerView)));
            if (extPlace != null && extPlace.getPlace_id() == item.getPlace_id()) {
                markerOptions.visible(false);
            }
        }

        @Override
        protected void onClusterItemRendered(Place item, Marker marker) {
            hashMapPlaces.put(marker.getId(), item);
            super.onClusterItemRendered(item, marker);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            String category_text;
            if (item.getItemId() != R.id.menu_category) {
                AnalyticsConstants.Companion.logAnalyticsEvent(AnalyticsConstants.SELECT_MAP_CATEGORY, item.getTitle().toString(), false);
                category_text = item.getTitle().toString();
                switch (item.getItemId()) {
                    case R.id.nav_all:
                        catId = -1;
                        break;
                    case R.id.nav_featured:
                        catId = cat[0];
                        break;
                    case R.id.nav_shopping:
                        catId = cat[1];
                        break;
                    case R.id.nav_pharmacy:
                        catId = cat[2];
                        break;
                    case R.id.nav_gym:
                        catId = cat[3];
                        break;
                    case R.id.nav_food:
                        catId = cat[4];
                        break;
                    case R.id.nav_bar:
                        catId = cat[5];
                        break;
                    case R.id.nav_fast_food:
                        catId = cat[6];
                        break;
                    case R.id.nav_delivery:
                        catId = cat[7];
                        break;
                    case R.id.nav_ice_cream_store:
                        catId = cat[8];
                        break;
                    case R.id.nav_hotels:
                        catId = cat[9];
                        break;
                    case R.id.nav_temporary_rent:
                        catId = cat[10];
                        break;
                    case R.id.nav_tour:
                        catId = cat[11];
                        break;
                    case R.id.nav_money:
                        catId = cat[12];
                        break;
                    case R.id.nav_bill_payments:
                        catId = cat[13];
                        break;
                    case R.id.nav_apartment_rental:
                        catId = cat[14];
                        break;
                    case R.id.nav_taxi:
                        catId = cat[15];
                        break;
                    case R.id.nav_gas_station:
                        catId = cat[16];
                        break;
                    case R.id.nav_transport:
                        catId = cat[17];
                        break;
                }

                // get category object when menu click
                currentCategory = db.getCategory(catId);

                if (isSinglePlace) {
                    isSinglePlace = false;
                    mClusterManager = new ClusterManager<>(this, mMap);
                    mMap.setOnCameraChangeListener(mClusterManager);
                }

                List<Place> places = db.getAllPlaceByCategory(catId);
                loadClusterManager(places);
                if (places.size() == 0) {
                    Snackbar.make(parent_view, getString(R.string.no_item_at) + " " + item.getTitle().toString(), Snackbar.LENGTH_LONG).show();
                }
                placeMarkerRenderer = new PlaceMarkerRenderer(this, mMap, mClusterManager);
                mClusterManager.setRenderer(placeMarkerRenderer);

                actionBar.setTitle(category_text);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAlertDialogGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_content_gps);
        builder.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.exit_slide_in, R.anim.exit_slide_out);

    }

}