package com.simplicityapp.base.config;

import com.simplicityapp.BuildConfig;

public class AppConfig {

    // if we want use profile and register
    public static final boolean ENABLE_USER_PROFILE = false;

    // this flag if you want to hide menu news info
    public static final boolean ENABLE_CONTENT_INFO = true;

    // if we want hide empty categories in menu
    public static final boolean ENABLE_EMPTY_CATEGORIES = false;

    // flag for save image offline
    public static final boolean IMAGE_CACHE = true;

    // if you place data more than 200 items please set TRUE
    public static final boolean LAZY_LOAD = false;

    // clear image cache when receive push notifications
    public static final boolean REFRESH_IMG_NOTIF = true;

    // when user enable gps, places will sort by distance
    public static final boolean SORT_BY_DISTANCE = true;

    // distance metric, fill with KILOMETER or MILE only
    public static final String DISTANCE_METRIC_CODE = "KILOMETER";

    // related to UI display string
    public static final String DISTANCE_METRIC_STR = "Km";

    // flag for enable disable theme color chooser, in Setting
    public static final boolean THEME_COLOR = false;

    // flag for enable or disable activate or deactivate notifications, in Setting
    public static final boolean NOTIFICATION_SELECTOR = false;

    // vibrate duration in millisecond in FCM notification
    public static int VIBRATION_TIME = 500;

    //WhatsApp API string
    public static String WHATSAPP_API_STRING = "https://api.whatsapp.com/send?phone=";
    public static String WHATSAPP_TEXT_STRING = "&text=";


    /**************************CONSTANTS****************************/

    // si la app abrió por primera vez, utilizar lo siguiente
    public static final int FIRST_RUN = -1;

    // si la app abrió de forma normal se usará lo siguiente
    public static final int NORMAL_RUN = 0;

    // si la app abrió luego de una actualización, utilizar lo siguiente
    public static final int UPGRADE_RUN = 1;

    // si la DB cuenta con menos de este valor, actualizar siempre que inicie para traer nuevos contenidos
    public static final int LIMIT_PLACES_TO_UPDATE = 100;

    public static String getWebURL() {
        return BuildConfig.SERVER_URL;
    }

}
