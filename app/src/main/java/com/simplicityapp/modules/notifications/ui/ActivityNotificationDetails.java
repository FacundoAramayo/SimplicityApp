package com.simplicityapp.modules.notifications.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.balysv.materialripple.MaterialRippleLayout;

import java.util.ArrayList;

import com.simplicityapp.base.utils.ActionTools;
import com.simplicityapp.base.utils.Tools;
import com.simplicityapp.base.utils.UITools;
import com.simplicityapp.modules.settings.ui.ActivityFullScreenImage;
import com.simplicityapp.base.data.Constant;
import com.simplicityapp.base.data.ThisApplication;
import com.simplicityapp.modules.main.ui.ActivityMain;
import com.simplicityapp.modules.start.ui.ActivitySplash;
import com.simplicityapp.modules.notifications.model.ContentInfo;
import com.simplicityapp.R;

import static com.simplicityapp.base.analytics.AnalyticsConstants.VIEW_CONTENT;

public class ActivityNotificationDetails extends AppCompatActivity {

    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";
    private static final String EXTRA_FROM_NOTIF = "key.EXTRA_FROM_NOTIF";

    // activity transition
    public static void navigate(Activity activity, ContentInfo obj, Boolean from_notif) {
        Intent i = navigateBase(activity, obj, from_notif);
        activity.startActivity(i);
    }

    public static Intent navigateBase(Context context, ContentInfo obj, Boolean from_notif) {
        Intent i = new Intent(context, ActivityNotificationDetails.class);
        i.putExtra(EXTRA_OBJECT, obj);
        i.putExtra(EXTRA_FROM_NOTIF, from_notif);
        return i;
    }

    private Boolean from_notif;

    // extra obj
    private ContentInfo contentInfo;

    private Toolbar toolbar;
    private ActionBar actionBar;
    private View parent_view;
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.enter_slide_in, R.anim.enter_slide_out);
        setContentView(R.layout.activity_notifications_details);

        contentInfo = (ContentInfo) getIntent().getSerializableExtra(EXTRA_OBJECT);
        from_notif = getIntent().getBooleanExtra(EXTRA_FROM_NOTIF, false);

        initComponent();
        initToolbar();
        displayData();

        // analytics tracking
        ThisApplication.getInstance().trackScreenView(VIEW_CONTENT, contentInfo.getTitle());
    }

    private void initComponent() {
        parent_view = findViewById(android.R.id.content);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(Html.fromHtml(contentInfo.getTitle()));
    }

    private void displayData() {

        webview = (WebView) findViewById(R.id.content);
        String html_data = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> ";
        html_data += contentInfo.getFull_content();
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings();
        webview.getSettings().setBuiltInZoomControls(true);
        webview.setBackgroundColor(Color.TRANSPARENT);
        webview.setWebChromeClient(new WebChromeClient());
        webview.loadData(html_data, "text/html; charset=UTF-8", null);

        // disable scroll on touch
        webview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        ((TextView) findViewById(R.id.date)).setText(Tools.Companion.getFormattedDate(contentInfo.getLast_update()));
        UITools.Companion.displayImage(this, (ImageView) findViewById(R.id.image), Constant.getURLimgNews(contentInfo.getImage()));

        ((MaterialRippleLayout) findViewById(R.id.lyt_image)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> images_list = new ArrayList<>();
                images_list.add(Constant.getURLimgNews(contentInfo.getImage()));
                Intent i = new Intent(ActivityNotificationDetails.this, ActivityFullScreenImage.class);
                i.putStringArrayListExtra(ActivityFullScreenImage.Companion.getEXTRA_IMGS(), images_list);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webview != null) webview.onPause();
    }

    @Override
    protected void onResume() {
        if (webview != null) webview.onResume();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_notification_details, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackAction();
            return true;
        } else if (id == R.id.action_share) {
            ActionTools.Companion.methodShareNews(this, contentInfo);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        onBackAction();
    }

    private void onBackAction() {
        if (from_notif) {
            if (ActivityMain.Companion.getActive()) {
                finish();
            } else {
                Intent intent = new Intent(getApplicationContext(), ActivitySplash.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.exit_slide_in, R.anim.exit_slide_out);
        }
    }

}
