package com.simplicityapp.base.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

import com.simplicityapp.base.utils.UITools;
import com.simplicityapp.modules.notifications.ui.ActivityNotificationDetails;
import com.simplicityapp.modules.places.ui.ActivityPlaceDetail;
import com.simplicityapp.modules.start.ui.ActivitySplash;
import com.simplicityapp.R;
import com.simplicityapp.base.data.AppConfig;
import com.simplicityapp.base.data.Constant;
import com.simplicityapp.base.data.database.DatabaseHandler;
import com.simplicityapp.base.data.SharedPref;
import com.simplicityapp.modules.notifications.model.FcmNotification;
import com.simplicityapp.modules.notifications.model.ContentInfo;
import com.simplicityapp.modules.places.model.Place;
import com.simplicityapp.base.utils.PermissionUtil;

public class FcmMessagingService extends FirebaseMessagingService {

    private static int VIBRATION_TIME = 500; // in millisecond
    private SharedPref sharedPref;
    private int retry_count = 0;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        sharedPref = new SharedPref(this);
        sharedPref.setFcmRegId(s);
        sharedPref.setOpenAppCounter(SharedPref.MAX_OPEN_COUNTER);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sharedPref = new SharedPref(this);

        sharedPref.setRefreshPlaces(true);
        if (AppConfig.REFRESH_IMG_NOTIF) {
            UITools.Companion.clearImageCacheOnBackground(this);
        }

        if (sharedPref.getNotification() && PermissionUtil.isStorageGranted(this)) {
            final FcmNotification fcmNotif = new FcmNotification();
            if (remoteMessage.getData().size() > 0) {
                Map<String, String> data = remoteMessage.getData();
                fcmNotif.setTitle(data.get("title"));
                fcmNotif.setContent(data.get("content"));
                fcmNotif.setType(data.get("type"));

                // load data place if exist
                String place_str = data.get("place");
                fcmNotif.setPlace(place_str != null ? new Gson().fromJson(place_str, Place.class) : null);

                // load data news_info if exist
                String news_str = data.get("news");
                fcmNotif.setNews(news_str != null ? new Gson().fromJson(news_str, ContentInfo.class) : null);

            } else if (remoteMessage.getNotification() != null) {
                RemoteMessage.Notification rn = remoteMessage.getNotification();
                fcmNotif.setTitle(rn.getTitle());
                fcmNotif.setContent(rn.getBody());
            }

            loadRetryImageFromUrl(this, fcmNotif, new CallbackImageNotif() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    displayNotificationIntent(fcmNotif, bitmap);
                }

                @Override
                public void onFailed(String string) {
                    displayNotificationIntent(fcmNotif, null);
                }
            });
        }
    }

    private void displayNotificationIntent(FcmNotification fcmNotification, Bitmap bitmap) {
        playRingtoneVibrate(this);
        Intent intent = new Intent(this, ActivitySplash.class);

        if (fcmNotification.getPlace() != null) {
            intent = ActivityPlaceDetail.Companion.navigateBase(this, fcmNotification.getPlace(), true);
        } else if (fcmNotification.getNews() != null) {
            new DatabaseHandler(this).refreshTableContentInfo();
            intent = ActivityNotificationDetails.navigateBase(this, fcmNotification.getNews(), true);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(fcmNotification.getTitle());
        builder.setContentText(fcmNotification.getContent());
        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        if (bitmap != null) {
            builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).setSummaryText(fcmNotification.getContent()));
        } else {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(fcmNotification.getContent()));
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        int unique_id = (int) System.currentTimeMillis();
        notificationManager.notify(unique_id, builder.build());
    }

    private void playRingtoneVibrate(Context context) {
        try {
            // play vibration
            if (sharedPref.getVibration()) {
                ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(VIBRATION_TIME);
            }
            RingtoneManager.getRingtone(context, Uri.parse(sharedPref.getRingtone())).play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRetryImageFromUrl(final Context ctx, final FcmNotification fcmNotification, final CallbackImageNotif callback) {
        String url = "";
        if (fcmNotification.getPlace() != null) {
            url = Constant.getURLimgPlace(fcmNotification.getPlace().getImage());
        } else if (fcmNotification.getNews() != null) {
            url = Constant.getURLimgNews(fcmNotification.getNews().getImage());
        } else {
            callback.onFailed("");
            return;
        }

        glideLoadImageFromUrl(ctx, url, new CallbackImageNotif() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                callback.onSuccess(bitmap);
            }

            @Override
            public void onFailed(String string) {
                Log.e("FcmMessagingService", "on Failed");
                if (retry_count <= Constant.LOAD_IMAGE_NOTIF_RETRY) {
                    retry_count++;
                    loadRetryImageFromUrl(ctx, fcmNotification, callback);
                } else {
                    callback.onFailed("");
                }
            }
        });
    }

    // load image with callback
    Handler mainHandler = new Handler(Looper.getMainLooper());
    Runnable myRunnable;

    private void glideLoadImageFromUrl(final Context ctx, final String url, final CallbackImageNotif callback) {
        myRunnable = new Runnable() {
            @Override
            public void run() {
                Glide.with(ctx).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        callback.onSuccess(bitmap);
                        mainHandler.removeCallbacks(myRunnable);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        callback.onFailed("On Load Failed");
                        mainHandler.removeCallbacks(myRunnable);
                    }
                });
            }
        };
        mainHandler.post(myRunnable);
    }

    private interface CallbackImageNotif {
        void onSuccess(Bitmap bitmap);

        void onFailed(String string);
    }
}
