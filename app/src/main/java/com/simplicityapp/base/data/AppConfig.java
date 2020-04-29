package com.simplicityapp.base.data;

import com.simplicityapp.BuildConfig;

public class AppConfig {

    // flag for display ads
    public static final boolean ADS_MAIN_INTERSTITIAL = false;
    public static final boolean ADS_PLACE_DETAILS_BANNER = false;
    public static final boolean ADS_NEWS_DETAILS_BANNER = false;

    // if you not use ads you can set this to false
    public static final boolean ENABLE_GDPR = true;

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

    // flag for tracking analytics
    public static final boolean ENABLE_ANALYTICS = false;

    public static final String WEB_CLIENT_ID = "952435693298-e22spc0j5pnf1r5qsk3hv3k2lu5tas8q.apps.googleusercontent.com";

    public static final String WEB_CLIENT_ID_DEV = "738836934516-hgaduu8708t67ad92mohb671211g58mb.apps.googleusercontent.com";

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
