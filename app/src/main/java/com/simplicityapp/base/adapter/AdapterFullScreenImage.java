package com.simplicityapp.base.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.chrisbanes.photoview.PhotoView;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

import com.simplicityapp.R;
import com.simplicityapp.base.utils.UITools;

public class AdapterFullScreenImage extends PagerAdapter {

    private Activity act;
    private Context ctx;
    private List<String> imagePaths;
    private LayoutInflater inflater;

    // constructor
    public AdapterFullScreenImage(Activity activity, Context ctx, List<String> imagePaths) {
        this.act = activity;
        this.ctx = ctx;
        this.imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return this.imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView image;
        inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.item_fullscreen_image, container, false);

        image = (PhotoView) viewLayout.findViewById(R.id.image);
        UITools.Companion.displayImage(ctx, image, imagePaths.get(position));
        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }

}