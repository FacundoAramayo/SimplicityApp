package com.simplicityapp.modules.notifications.activity;

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
import com.simplicityapp.base.config.analytics.AnalyticsConstants;
import com.simplicityapp.base.utils.ActionTools;
import com.simplicityapp.base.utils.Tools;
import com.simplicityapp.base.connection.JsonAPI;
import com.simplicityapp.base.rest.RestAdapter;
import com.simplicityapp.base.connection.callbacks.CallbackListContentInfo;
import com.simplicityapp.base.config.Constant;
import com.simplicityapp.base.persistence.db.DatabaseHandler;
import com.simplicityapp.baseui.utils.UITools;
import com.simplicityapp.baseui.decorator.SpacingItemDecoration;
import com.simplicityapp.baseui.adapter.AdapterContentInfo;
import com.simplicityapp.modules.notifications.model.ContentInfo;
import com.simplicityapp.modules.settings.activity.ActivitySetting;
import com.simplicityapp.R;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityNotifications extends AppCompatActivity {

    public ActionBar actionBar;
    private View parentView;
    private RecyclerView recyclerView;
    private AdapterContentInfo mAdapter;
    private View lytProgress;
    private Call<CallbackListContentInfo> callbackCall = null;
    private DatabaseHandler db;

    private int post_total = 0;
    private int failed_page = 0;
    private Snackbar snackbarRetry = null;

    // can be, ONLINE or OFFLINE
    private String MODE = "ONLINE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.enter_slide_in, R.anim.enter_slide_out);
        setContentView(R.layout.activity_notifications);
        parentView = findViewById(android.R.id.content);
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
        lytProgress = findViewById(R.id.lyt_progress);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpacingItemDecoration(1, UITools.Companion.dpToPx(4), true));


        mAdapter = new AdapterContentInfo(this, recyclerView, new ArrayList<ContentInfo>());
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new AdapterContentInfo.OnItemClickListener() {
            @Override
            public void onItemClick(View v, ContentInfo obj, int position) {
                ActivityNotificationDetails.Companion.navigate(ActivityNotifications.this, obj, false, AnalyticsConstants.SELECT_NOTIFICATION_OPEN_LIST_ITEM);
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
        showProgress(false);
        if (items.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListContentInfo(final int page_no) {
        if (MODE.equals("ONLINE")) {
            JsonAPI api = RestAdapter.createAPI();
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
        showProgress(false);
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
            showProgress(true);
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
    public void onStop() {
        super.onStop();
        showProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
    }

    private void showFailedView(boolean show, String message) {
        if(snackbarRetry == null) {
            snackbarRetry = Snackbar.make(parentView, "", Snackbar.LENGTH_INDEFINITE);
        }
        snackbarRetry.setText(message);
        snackbarRetry.setAction(R.string.RETRY, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAction(failed_page);
            }
        });
        if (show) {
            snackbarRetry.show();
        } else {
            snackbarRetry.dismiss();
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

    private void showProgress(final boolean show) {
        if (show) {
            lytProgress.setVisibility(View.VISIBLE);
        } else {
            lytProgress.setVisibility(View.GONE);
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
            AnalyticsConstants.Companion.logAnalyticsEvent(AnalyticsConstants.SELECT_NOTIFICATION_ACTION, AnalyticsConstants.REFRESH_LIST, false);
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