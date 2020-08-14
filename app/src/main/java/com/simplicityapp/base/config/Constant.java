package com.simplicityapp.base.config;

import com.simplicityapp.BuildConfig;

public class Constant {

    //GUIDE TYPES
    public static final String GUIDE_TYPE = "GUIDE_TYPE";
    public static final String COMMERCIAL_GUIDE = "COMMERCIAL_GUIDE";
    public static final String JOBS_GUIDE = "JOBS_GUIDE";

    // for search logs Tag
    public static final String LOG_TAG = BuildConfig.APPLICATION_ID + "-";

    public static final String PREFS_NAME = "MAIN_PREF";
    public static final String PREF_VERSION_CODE_KEY = BuildConfig.APPLICATION_ID + ".build.VERSION_CODE_KEY";
    public static final int DOESNT_EXIST_CODE = -1;
    public static final int NO_REGION_SELECTED = -1;

    //WEB VIEW CONFIGS
    public static final String WEB_VIEW_HTML_CONFIG = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> ";
    public static final String WEB_VIEW_MIME_TYPE = "text/html; charset=UTF-8";

    //WhatsApp API string
    public static String WHATSAPP_API_STRING = "https://api.whatsapp.com/send?phone=";
    public static String WHATSAPP_TEXT_STRING = "&text=";

    // si la app abrió por primera vez, utilizar lo siguiente
    public static final int FIRST_RUN = -1;

    // si la app abrió de forma normal se usará lo siguiente
    public static final int NORMAL_RUN = 0;

    // si la app abrió luego de una actualización, utilizar lo siguiente
    public static final int UPGRADE_RUN = 1;

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

    public static String EXTRA_OBJECT = "key.EXTRA_OBJECT";
    public static String EXTRA_FROM_NOTIF = "key.EXTRA_FROM_NOTIF";
    public static String EXTRA_NOTIF_FLAG = "key.EXTRA_NOTIF_FLAG";
    public static String EXTRA_POS = "key.EXTRA_POS";
    public static String EXTRA_IMGS = "key.EXTRA_IMGS";
    public static String TAG_CATEGORY = "key.TAG_CATEGORY";
    public static String ARG_SECTION_NUMBER = "section_number";

}
