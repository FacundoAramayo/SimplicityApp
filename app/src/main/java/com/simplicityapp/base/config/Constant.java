package com.simplicityapp.base.config;

import com.simplicityapp.BuildConfig;

public class Constant {

    /**
     * -------------------- EDIT THIS  -------------------------------------------------------------
     */

    public static final float city_zoom = 13;

    public static final int NO_REGION_SELECTED = -1;

    /*
    TAGS
     */

    // for search logs Tag
    public static final String LOG_TAG = BuildConfig.APPLICATION_ID + "-";

    public static final String PREFS_NAME = "MAIN_PREF";
    public static final String PREF_VERSION_CODE_KEY = BuildConfig.APPLICATION_ID + ".build.VERSION_CODE_KEY";
    public static final int DOESNT_EXIST_CODE = -1;

    //URL TO FORMS
    public static final String LINK_TO_SUBSCRIPTION_FORM = "http://bit.ly/2TZkzQN";
    public static final String LINK_TO_SUGGESTIONS_FORM = "http://bit.ly/36tYrkd";

    //PARAMS SEND EMAIL
    public static final String CONTACT_EMAIL = "contacto@simplicityapp.com.ar";
    public static final String SUBJECT_EMAIL = "CONTACTO - APLICACIÓN";


    //WEB VIEW CONFIGS
    public static final String WEB_VIEW_HTML_CONFIG = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> ";
    public static final String WEB_VIEW_MIME_TYPE = "text/html; charset=UTF-8";

    //FONT-FAMILY STYLES
    public static final String FONT_FAMILY_RALEWAY = "<style>@import url('https://fonts.googleapis.com/css?family=Raleway:400,700&display=swap');</style>";

    //GUIDE TYPES
    public static final String GUIDE_TYPE = "GUIDE_TYPE";

    public static final String COMMERCIAL_GUIDE = "COMMERCIAL_GUIDE";
    public static final String JOBS_GUIDE = "JOBS_GUIDE";


    //RESPONSE
    public static final String SUCCESS_RESPONSE = "success";


    /**QUICK ACCESS CATEGORIES ID*/
    public static final int GASTRONOMY_ID = 1000;
    public static final int TAXI_ID = 4001;
    public static final int PHARMACY_OPENED_ID = 3002;


    /**
     * ------------------- DON'T EDIT THIS ---------------------------------------------------------
     */

    // image file url
    public static String getURLimgPlace(String file_name) {
        return AppConfig.getWebURL() + "uploads/place/" + file_name;
    }
    public static String getURLimgNews(String file_name) {
        return AppConfig.getWebURL() + "uploads/news/" + file_name;
    }

    // this limit value used for give pagination (request and display) to decrease payload
    public static final int LIMIT_PLACE_REQUEST = 40;
    public static final int LIMIT_LOADMORE = 40;

    public static final int LIMIT_NEWS_REQUEST = 40;

    // retry load image notification
    public static int LOAD_IMAGE_NOTIF_RETRY = 3;

    //Bundle keys
    public static String IS_FIRST_OPEN = "IS_FIRST_OPEN";

    public static String IS_FROM_HOME = "IS_FROM_HOME";

}
