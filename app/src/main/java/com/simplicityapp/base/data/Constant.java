package com.simplicityapp.base.data;

public class Constant {

    /**
     * -------------------- EDIT THIS  -------------------------------------------------------------
     */

    // Edit WEB_URL with your url. Make sure you have backslash('/') in the end url
    public static String WEB_URL = "http://ciudadanovirtualdemo.smartsolutions.com.ar/";
    //public static String WEB_URL = "http://ciudadanovirtualdemo.smartsolutions.com.ar/";

    /*Córdoba*/
//    public static final double city_lat = -31.4200833;
//    public static final double city_lng = -64.1887761;
//    public static final float city_zoom = 12;

    /*Tartagal*/
    public static final double city_lat = -22.5167622;
    public static final double city_lng = -63.8056084;
    public static final float city_zoom = 13;

    /*
    TAGS
     */

    public static final String APP_NAME_MIN = "simplicityapp";
    // for search logs Tag
    public static final String LOG_TAG = APP_NAME_MIN + "_LOG";

    public static final String PREFS_NAME = "MAIN_PREF";
    public static final String PREF_VERSION_CODE_KEY = APP_NAME_MIN + ".build.VERSION_CODE_KEY";
    public static final int DOESNT_EXIST_CODE = -1;



    // Google analytics event category
    public enum AnalyticsEvent {
        FAVORITES,
        THEME,
        NOTIFICATION,
        REFRESH
    }

    // OAuth Credentials
    public static final String OAUTH_WEB_CLIENT_ID = "554896515826-iilaie2tmb9td9gv7ejgp4ea9b2hvlg0.apps.googleusercontent.com";

    //URL TO FORMS
    public static final String LINK_TO_SUBSCRIPTION_FORM = "http://bit.ly/2TZkzQN";
    public static final String LINK_TO_SUGGESTIONS_FORM = "http://bit.ly/36tYrkd";

    //PARAMS SEND EMAIL
    public static final String CONTACT_EMAIL = "contacto@simplicityapp.com.ar";
    public static final String SUBJECT_EMAIL = "CONTACTO MEDIANTE SIMPLICITY APP";

    /**
     * ------------------- DON'T EDIT THIS ---------------------------------------------------------
     */

    // image file url
    public static String getURLimgPlace(String file_name) {
        return WEB_URL + "uploads/place/" + file_name;
    }
    public static String getURLimgNews(String file_name) {
        return WEB_URL + "uploads/news/" + file_name;
    }

    // this limit value used for give pagination (request and display) to decrease payload
    public static final int LIMIT_PLACE_REQUEST = 40;
    public static final int LIMIT_LOADMORE = 40;

    public static final int LIMIT_NEWS_REQUEST = 40;

    // retry load image notification
    public static int LOAD_IMAGE_NOTIF_RETRY = 3;

}
