package com.simplicityapp.baseui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.simplicityapp.R;
import com.simplicityapp.base.config.Constant;
import com.simplicityapp.base.utils.Tools;
import com.simplicityapp.baseui.utils.UITools;
import com.simplicityapp.modules.places.model.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterPlaceList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private boolean loading;

    private Context ctx;
    private List<Place> items = new ArrayList<>();

    private OnLoadMoreListener onLoadMoreListener;
    private OnItemClickListener onItemClickListener;

    private int lastPosition = -1;
    private boolean clicked = false;


    public interface OnItemClickListener {
        void onItemClick(View view, Place viewModel);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public ImageView image;
        public TextView distance;
        public LinearLayout lyt_distance;
        public TextView content;
        public MaterialRippleLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            image = (ImageView) v.findViewById(R.id.image);
            distance = (TextView) v.findViewById(R.id.distance);
            lyt_distance = (LinearLayout) v.findViewById(R.id.lyt_distance);
            content = v.findViewById(R.id.content);
            lyt_parent = (MaterialRippleLayout) v.findViewById(R.id.lyt_parent);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }

    public AdapterPlaceList(Context ctx, RecyclerView view, List<Place> items) {
        this.ctx = ctx;
        this.items = items;
        lastItemViewDetector(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_horizontal, parent, false);
            vh = new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder vItem = (ViewHolder) holder;
            final Place p = items.get(position);
            vItem.title.setText(p.getName());
            UITools.Companion.displayImageThumb(ctx, vItem.image, Constant.getURLimgPlace(p.getImage()), 0.5f);

            if (p.hasLatLngPosition() && (!Objects.equals(p.getAddress(), "")) && (p.getDistance() != -1)) {
                vItem.lyt_distance.setVisibility(View.VISIBLE);
                vItem.distance.setText(Tools.Companion.getFormattedDistance(p.getDistance()));
            }

            vItem.content.setText(p.getShort_description());

            // Here you apply the animation when the view is bound
            setAnimation(vItem.lyt_parent, position);

            vItem.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (!clicked && onItemClickListener != null) {
                        clicked = true;
                        onItemClickListener.onItemClick(v, p);
                    }
                }
            });
            clicked = false;
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.items.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Here is the key method to apply the animation
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.slide_in_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void insertData(List<Place> items, boolean shuffle) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        if (shuffle) {
            items = Tools.Companion.shuffleItems(items);
        }
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
        }
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = layoutManager.findLastVisibleItemPosition();
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        int current_page = getItemCount() / Constant.LIMIT_LOADMORE;
                        onLoadMoreListener.onLoadMore(current_page);
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int last_idx = into[0];
        for (int i : into) {
            if (last_idx < i) last_idx = i;
        }
        return last_idx;
    }

}