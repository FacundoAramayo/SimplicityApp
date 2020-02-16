package com.simplicityapp.base.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.simplicityapp.R;

import static com.simplicityapp.base.data.Constant.APP_NAME_MIN;
import static com.simplicityapp.base.data.Constant.LOG_TAG;

public class SharedPref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences prefs;
    public static final int MAX_OPEN_COUNTER = 15;

    private static final String FCM_PREF_KEY = APP_NAME_MIN + ".data.FCM_PREF_KEY";
    private static final String SERVER_FLAG_KEY = APP_NAME_MIN + ".data.SERVER_FLAG_KEY";
    private static final String THEME_COLOR_KEY = APP_NAME_MIN + ".data.THEME_COLOR_KEY";
    private static final String LAST_PLACE_PAGE = "LAST_PLACE_PAGE_KEY";

    // need refresh
    public static final String REFRESH_PLACES = APP_NAME_MIN + ".data.REFRESH_PLACES";

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setFcmRegId(String gcmRegId) {
        sharedPreferences.edit().putString(FCM_PREF_KEY, gcmRegId).apply();
    }

    public String getFcmRegId() {
        return sharedPreferences.getString(FCM_PREF_KEY, null);
    }

    public boolean isFcmRegIdEmpty() {
        return TextUtils.isEmpty(getFcmRegId());
    }

    public void setRegisteredOnServer(boolean registered) {
        sharedPreferences.edit().putBoolean(SERVER_FLAG_KEY, registered).apply();
    }

    public boolean isRegisteredOnServer() {
        return sharedPreferences.getBoolean(SERVER_FLAG_KEY, false);
    }

    public boolean isNeedRegisterFcm() {
        return (isFcmRegIdEmpty() || !isRegisteredOnServer());
    }

    /**
     * For notifications flag
     */
    public boolean getNotification() {
        return prefs.getBoolean(context.getString(R.string.pref_key_notif), true);
    }

    public String getRingtone() {
        return prefs.getString(context.getString(R.string.pref_key_ringtone), "content://settings/system/notification_sound");
    }

    public boolean getVibration() {
        return prefs.getBoolean(context.getString(R.string.pref_key_vibrate), true);
    }

    /**
     * Refresh user data
     * When phone receive GCM notification this flag will be enable.
     * so when user open the app all data will be refresh
     */
    public boolean isRefreshPlaces() {
        return sharedPreferences.getBoolean(REFRESH_PLACES, false);
    }

    public void setRefreshPlaces(boolean need_refresh) {
        sharedPreferences.edit().putBoolean(REFRESH_PLACES, need_refresh).apply();
    }


    /**
     * For theme color
     */
    public void setThemeColor(String color) {
        sharedPreferences.edit().putString(THEME_COLOR_KEY, color).apply();
    }

    public String getThemeColor() {
        return sharedPreferences.getString(THEME_COLOR_KEY, "");
    }

    public int getThemeColorInt() {
        if (getThemeColor().equals("")) {
            return context.getResources().getColor(R.color.colorPrimary);
        }
        return Color.parseColor(getThemeColor());
    }


    /**
     * To save last state request
     */
    public void setLastPlacePage(int page) {
        sharedPreferences.edit().putInt(LAST_PLACE_PAGE, page).apply();
    }

    public int getLastPlacePage() {
        return sharedPreferences.getInt(LAST_PLACE_PAGE, 1);
    }


    /**
     * To save dialog permission state
     */
    public void setNeverAskAgain(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getNeverAskAgain(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    // when app open N-times it will update gcm RegID at server
    public boolean isOpenAppCounterReach() {
        int counter = sharedPreferences.getInt("OPEN_COUNTER_KEY", MAX_OPEN_COUNTER) + 1;
        setOpenAppCounter(counter);
        Log.e(LOG_TAG, "SharedPref, COUNTER " + counter);
        return (counter >= MAX_OPEN_COUNTER);
    }

    public void setOpenAppCounter(int val) {
        sharedPreferences.edit().putInt("OPEN_COUNTER_KEY", val).apply();
    }

}
