package com.simplicityapp.modules.notifications.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import com.simplicityapp.R;
import com.simplicityapp.base.analytics.AnalyticsConstants;
import com.simplicityapp.base.utils.ActionTools;
import com.simplicityapp.base.utils.Tools;
import com.simplicityapp.base.utils.UITools;
import com.simplicityapp.modules.settings.ui.ActivitySetting;
import com.simplicityapp.base.adapter.AdapterContentInfo;
import com.simplicityapp.base.connection.API;
import com.simplicityapp.base.connection.RestAdapter;
import com.simplicityapp.base.connection.callbacks.CallbackListContentInfo;
import com.simplicityapp.base.data.Constant;
import com.simplicityapp.base.data.database.DatabaseHandler;
import com.simplicityapp.modules.notifications.model.ContentInfo;
import com.simplicityapp.base.widget.SpacingItemDecoration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityNotifications extends AppCompatActivity {

    public ActionBar actionBar;
    private View parent_view;
    private RecyclerView recyclerView;
    private AdapterContentInfo mAdapter;
    private View lyt_progress;
    private Call<CallbackListContentInfo> callbackCall = null;
    private DatabaseHandler db;

    private int post_total = 0;
    private int failed_page = 0;
    private Snackbar snackbar_retry = null;

    // can be, ONLINE or OFFLINE
    private String MODE = "ONLINE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.enter_slide_in, R.anim.enter_slide_out);
        setContentView(R.layout.activity_notifications);
        parent_view = findViewById(android.R.id.content);
        db = new DatabaseHandler(this);

        initToolbar();
        iniComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.title_nav_news);
    }

    public void iniComponent() {
        lyt_progress = findViewById(R.id.lyt_progress);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpacingItemDecoration(1, UITools.Companion.dpToPx(this, 4), true));


        mAdapter = new AdapterContentInfo(this, recyclerView, new ArrayList<ContentInfo>());
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new AdapterContentInfo.OnItemClickListener() {
            @Override
            public void onItemClick(View v, ContentInfo obj, int position) {
                ActivityNotificationDetails.Companion.navigate(ActivityNotifications.this, obj, false);
            }
        });

        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(new AdapterContentInfo.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (post_total > mAdapter.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    mAdapter.setLoaded();
                }
            }
        });

        // if already have data news at db, use mode OFFLINE
        if (db.getContentInfoSize() > 0) {
            MODE = "OFFLINE";
        }
        requestAction(1);
    }

    private void displayApiResult(final List<ContentInfo> items) {
        mAdapter.insertData(items);
        firstProgress(false);
        if (items.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListContentInfo(final int page_no) {
        if (MODE.equals("ONLINE")) {
            API api = RestAdapter.createAPI();
            callbackCall = api.getContentInfoByPage(page_no, Constant.LIMIT_NEWS_REQUEST);
            callbackCall.enqueue(new Callback<CallbackListContentInfo>() {
                @Override
                public void onResponse(Call<CallbackListContentInfo> call, Response<CallbackListContentInfo> response) {
                    CallbackListContentInfo resp = response.body();
                    if (resp != null && resp.getStatus().equals("success")) {
                        if (page_no == 1) {
                            mAdapter.resetListData();
                            db.refreshTableContentInfo();
                        }
                        post_total = resp.getCount_total();
                        db.insertListContentInfo(resp.getNews_infos());
                        displayApiResult(resp.getNews_infos());
                    } else {
                        onFailRequest(page_no);
                    }
                }

                @Override
                public void onFailure(Call<CallbackListContentInfo> call, Throwable t) {
                    if (!call.isCanceled()) onFailRequest(page_no);
                }

            });
        } else {
            if (page_no == 1) mAdapter.resetListData();
            int limit = Constant.LIMIT_NEWS_REQUEST;
            int offset = (page_no * limit) - limit;
            post_total = db.getContentInfoSize();
            List<ContentInfo> items = db.getContentInfoByPage(limit, offset);
            displayApiResult(items);
        }
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        mAdapter.setLoaded();
        firstProgress(false);
        if (Tools.Companion.checkConnection(this)) {
            showFailedView(true, getString(R.string.refresh_failed));
        } else {
            showFailedView(true, getString(R.string.no_internet));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            firstProgress(true);
        } else {
            mAdapter.setLoading();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestListContentInfo(page_no);
            }
        }, MODE.equals("OFFLINE") ? 50 : 1000);
    }

    @Override
    protected void onResume() {
        if (Tools.Companion.checkConnection(this)) {
            refreshNotifications();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        firstProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
    }

    private void showFailedView(boolean show, String message) {
        if(snackbar_retry == null) {
            snackbar_retry = Snackbar.make(parent_view, "", Snackbar.LENGTH_INDEFINITE);
        }
        snackbar_retry.setText(message);
        snackbar_retry.setAction(R.string.RETRY, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAction(failed_page);
            }
        });
        if (show) {
            snackbar_retry.show();
        } else {
            snackbar_retry.dismiss();
        }
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = (View) findViewById(R.id.lyt_no_item);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void firstProgress(final boolean show) {
        if (show) {
            lyt_progress.setVisibility(View.VISIBLE);
        } else {
            lyt_progress.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activiy_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_refresh) {
            AnalyticsConstants.Companion.logAnalyticsEvent(AnalyticsConstants.SELECT_NOTIFICATIONS_LIST_REFRESH, null, true, false);
            refreshNotifications();
        } else if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), ActivitySetting.class);
            startActivity(i);
        } else if (id == R.id.action_rate) {
            ActionTools.Companion.rateAction(ActivityNotifications.this);
        } else if (id == R.id.action_about) {
            ActionTools.Companion.aboutAction(ActivityNotifications.this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshNotifications() {
        if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
        showFailedView(false, "");
        MODE = "ONLINE";
        post_total = 0;
        requestAction(1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.exit_slide_in, R.anim.exit_slide_out);
    }
}
