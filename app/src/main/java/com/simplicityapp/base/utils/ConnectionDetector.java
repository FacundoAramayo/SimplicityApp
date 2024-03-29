package com.simplicityapp.base.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {
 
    private Context context;
 
    public ConnectionDetector(Context context){
        this.context = context;
    }
 
    /**
     * Checking for all possible internet providers
     * **/
    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connectivity != null) {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null) {
                  for (NetworkInfo networkInfo : info)
                      if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                          return true;
                      }
              }
          }
          return false;
    }
}
