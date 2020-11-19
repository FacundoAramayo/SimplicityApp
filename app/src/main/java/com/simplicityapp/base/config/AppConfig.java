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

    //Zoom to show map in lat/lng position
    public static final float CITY_ZOOM = 13;

    // si la DB cuenta con menos de este valor, actualizar siempre que inicie para traer nuevos contenidos
    public static final int LIMIT_PLACES_TO_UPDATE = 100;

    //URL TO FORMS
    public static final String LINK_TO_SUBSCRIPTION_FORM = BuildConfig.SUBSCRIPTION_LINK;
    public static final String LINK_TO_SUGGESTIONS_FORM = BuildConfig.SUGGESTIONS_LINK;

    //PARAMS SEND EMAIL
    public static final String CONTACT_EMAIL = "contacto@simplicityapp.com.ar";
    public static final String SUBJECT_EMAIL = "CONTACTO - APLICACIÓN";
    public static final String FOOTER_MESSAGE = "\n\n--\nMensaje enviado desde Simplicity App.";

    //FONT-FAMILY STYLES
    public static final String FONT_FAMILY = "<style>@import url('https://fonts.googleapis.com/css?family=Raleway:400,700&display=swap');</style>";

    public static String getWebURL() { return BuildConfig.SERVER_URL; }

    public static final int DATABASE_VERSION = BuildConfig.DATABASE_VERSION;

    public static final String DATABASE_NAME = BuildConfig.DATABASE_NAME;

    public static final Boolean SHOW_CATEGORY_NAME = BuildConfig.SHOW_CATEGORY_NAME;

}
