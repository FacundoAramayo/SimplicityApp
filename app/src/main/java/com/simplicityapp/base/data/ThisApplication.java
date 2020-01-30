package com.simplicityapp.base.data;

import android.app.Application;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import com.simplicityapp.base.connection.API;
import com.simplicityapp.base.connection.RestAdapter;
import com.simplicityapp.base.connection.callbacks.CallbackDevice;
import com.simplicityapp.base.utils.Tools;
import com.simplicityapp.modules.places.model.Place;
import com.simplicityapp.modules.settings.model.DeviceInfo;
import retrofit2.Call;
import retrofit2.Response;

import static com.simplicityapp.base.analytics.AnalyticsConstants.PLACE_ACTION;
import static com.simplicityapp.base.analytics.AnalyticsConstants.SCREEN_VIEW;

public class ThisApplication extends Application {

    private Call<CallbackDevice> callback = null;
    private static ThisApplication mInstance;
    private FirebaseAnalytics firebaseAnalytics;
    private Location location = null;
    private SharedPref sharedPref;
    private int fcm_count = 0;
    private final int FCM_MAX_COUNT = 10;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        sharedPref = new SharedPref(this);

        // initialize firebase
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);

        // obtain regId & registering device to server
        obtainFirebaseToken(firebaseApp);

        // activate analytics tracker
        getFirebaseAnalytics();
    }

    public static synchronized ThisApplication getInstance() {
        return mInstance;
    }


    private void obtainFirebaseToken(final FirebaseApp firebaseApp) {
        if (!Tools.Companion.checkConnection(this)) return;
        fcm_count++;

        Task<InstanceIdResult> resultTask = FirebaseInstanceId.getInstance().getInstanceId();
        resultTask.addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String regId = instanceIdResult.getToken();
                if (!TextUtils.isEmpty(regId)) sendRegistrationToServer(regId);
            }
        });

        resultTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(Constant.LOG_TAG, "Failed obtain fcmID : " + e.getMessage());
                if (fcm_count > FCM_MAX_COUNT) return;
                obtainFirebaseToken(firebaseApp);
            }
        });
    }

    /**
     * --------------------------------------------------------------------------------------------
     * For Firebase Cloud Messaging
     */
    private void sendRegistrationToServer(String token) {
        if (Tools.Companion.checkConnection(this) && !TextUtils.isEmpty(token) && sharedPref.isOpenAppCounterReach()) {
            API api = RestAdapter.createAPI();
            DeviceInfo deviceInfo = Tools.Companion.getDeviceInfo(this);
            deviceInfo.setRegid(token);

            callback = api.registerDevice(deviceInfo);
            callback.enqueue(new retrofit2.Callback<CallbackDevice>() {
                @Override
                public void onResponse(Call<CallbackDevice> call, Response<CallbackDevice> response) {
                    CallbackDevice resp = response.body();
                    if (resp.getStatus().equals("success")) {
                        sharedPref.setOpenAppCounter(0);
                    }
                }

                @Override
                public void onFailure(Call<CallbackDevice> call, Throwable t) {
                }
            });
        }
    }


    /*
     * --------------------------------------------------------------------------------------------
     * For Google Analytics
     */
    private Bundle getGoogleUserInformation() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        Bundle params = new Bundle();
        if (acct != null) {
            params.putString("personName", acct.getDisplayName());
            params.putString("personGivenName", acct.getGivenName());
            params.putString("personFamilyName", acct.getFamilyName());
            params.putString("personEmail", acct.getEmail());
            params.putString("personId", acct.getId());
            params.putString("personPhoto", acct.getPhotoUrl().toString());
        }
        return params;
    }

    public synchronized FirebaseAnalytics getFirebaseAnalytics() {
        getGoogleUserInformation();
        if (firebaseAnalytics == null && AppConfig.ENABLE_ANALYTICS) {
            // Obtain the Firebase Analytics.
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
        return firebaseAnalytics;
    }

    public void trackScreenView(String screenName, String category, String item) {
        getGoogleUserInformation();
        if (firebaseAnalytics != null || AppConfig.ENABLE_ANALYTICS) {
            Bundle params = new Bundle();
            screenName = screenName.replaceAll("[^A-Za-z0-9_]", "");
            category = category.replaceAll("[^A-Za-z0-9_]", "");
            params.putString("screenName", screenName);
            params.putString("category", category);
            params.putString("item", item);
            params.putBundle("userInformation", getGoogleUserInformation());
            firebaseAnalytics.logEvent(SCREEN_VIEW, params);
        }
    }

    public void trackEvent(String event, String label) {
        if (firebaseAnalytics != null || AppConfig.ENABLE_ANALYTICS) {
            Bundle params = new Bundle();
            //label = label.replaceAll("[^A-Za-z0-9_]", "");
            params.putString("label", label);
            params.putBundle("userInformation", getGoogleUserInformation());
            firebaseAnalytics.logEvent(event, params);
        }
    }

    public void trackPlaceAction(Place place, String action) {
        if (firebaseAnalytics != null || AppConfig.ENABLE_ANALYTICS) {
            Bundle params = new Bundle();
            action = action.replaceAll("[^A-Za-z0-9_]", "");
            params.putString("action", action);
            params.putString("placeName", place.getName());
            params.putString("placeAddress", place.getAddress());
            params.putString("placeLat", String.valueOf(place.getLat()));
            params.putString("placeLng", String.valueOf(place.getLng()));

            params.putBundle("userInformation", getGoogleUserInformation());
            firebaseAnalytics.logEvent(PLACE_ACTION, params);
        }

    }

    /**
     * ---------------------------------------- End of analytics ---------------------------------
     */

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
