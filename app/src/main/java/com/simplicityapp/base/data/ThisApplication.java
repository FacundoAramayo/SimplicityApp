package com.simplicityapp.base.data;

import android.app.Application;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import com.simplicityapp.R;
import com.simplicityapp.base.connection.API;
import com.simplicityapp.base.connection.RestAdapter;
import com.simplicityapp.base.connection.callbacks.CallbackDevice;
import com.simplicityapp.base.utils.Tools;
import com.simplicityapp.base.utils.UITools;
import com.simplicityapp.modules.settings.model.DeviceInfo;
import retrofit2.Call;
import retrofit2.Response;

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


    /**
     * --------------------------------------------------------------------------------------------
     * For Google Analytics
     */
    public synchronized FirebaseAnalytics getFirebaseAnalytics() {
        if (firebaseAnalytics == null && AppConfig.ENABLE_ANALYTICS) {
            // Obtain the Firebase Analytics.
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
        return firebaseAnalytics;
    }

    public void trackScreenView(String event, String item) {
        if (firebaseAnalytics != null || AppConfig.ENABLE_ANALYTICS) {
            Bundle params = new Bundle();
            event = event.replaceAll("[^A-Za-z0-9_]", "");
            params.putString("event", event);
            params.putString("item", item);
            firebaseAnalytics.logEvent(event, params);
        }
    }

    public void trackEvent(String category, String action, String label) {
        if (firebaseAnalytics != null || AppConfig.ENABLE_ANALYTICS) {
            Bundle params = new Bundle();
            category = category.replaceAll("[^A-Za-z0-9_]", "");
            action = action.replaceAll("[^A-Za-z0-9_]", "");
            label = label.replaceAll("[^A-Za-z0-9_]", "");
            params.putString("category", category);
            params.putString("action", action);
            params.putString("label", label);
            firebaseAnalytics.logEvent("EVENT", params);
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
