package com.simplicityapp.base.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.List;

public abstract class PermissionUtil {

    private static final String STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    /* Permission required for application */
    private static final String[] PERMISSION_ALL = {
            STORAGE,
            LOCATION
    };

    public static String[] getDeniedPermission(Activity act) {
        List<String> permissions = new ArrayList<>();
        for (String permission : PERMISSION_ALL) {
            int status = act.checkSelfPermission(permission);
            if (status != PackageManager.PERMISSION_GRANTED) {
                permissions.add(permission);
            }
        }

        return permissions.toArray(new String[permissions.size()]);
    }


    private static boolean isGranted(Context ctx, String permission) {
        if (!Tools.Companion.needRequestPermission()) return true;
        return (ctx.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean isLocationGranted(Context ctx) {
        return isGranted(ctx, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean isStorageGranted(Context ctx) {
        return isGranted(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
}
