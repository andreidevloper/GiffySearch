package com.as.giffysearch.Adapters;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import com.as.giffysearch.Adapters.RecyclerViewItems.SearchRecyclerViewItemFactory;
import com.as.giffysearch.Adapters.RecyclerViewItems.RecyclerViewItem;
import com.as.giffysearch.Adapters.RecyclerViewItems.SearchResultRecyclerViewItems.GifResultRecyclerViewItem;
import com.as.giffysearch.Adapters.RecyclerViewItems.SearchResultRecyclerViewItems.LoaderRecyclerViewItem;
import com.as.giffysearch.Adapters.RecyclerViewItems.SearchResultRecyclerViewItems.FailedResponseRecyclerViewItem;

import com.as.giffysearch.ThirdParty.Glide.GlideApp;
import com.as.giffysearch.Models.JSON.Giphy.GifResult;

import com.as.giffysearch.R;
import com.as.giffysearch.Utility.Debugging;

import com.as.giffysearch.Controllers.PaggingController;

/**
 * Created by Andrejs Skorinko on 11/30/2017.
 *
 */

public class SearchResultRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
                                             implements ListPreloader.PreloadModelProvider<GifResult>
{
    private static final String LOG_TAG = SearchResultRecyclerViewAdapter.class.getSimpleName();

    private Activity activity_;

    private final RequestBuilder<Drawable> requestBuilder_;
    private final ViewPreloadSizeProvider<GifResult> preloadSizeProvider_;

    // For pagging
    private static final int VIEW_TYPE_INVALID = -1;
    private static final int VIEW_TYPE_GIF = 0;
    public static final int VIEW_TYPE_LOADING = 1;
    private static final int VIEW_TYPE_RESPONSE_FAILED = 2;
    private PaggingController paggingController_;
    private LinkedList<RecyclerViewItem> recyclerViewItems_;

    // For failed response button
    private final FailedResponseViewHolder.ClickListener clickListener_;

    public SearchResultRecyclerViewAdapter(Activity activity,
                                           RequestBuilder<Drawable> requestBuilder,
                                           ViewPreloadSizeProvider<GifResult> preloadSizeProvider,
                                           PaggingController paggingController,
                                           LinkedList<RecyclerViewItem> recyclerViewItems,
                                           FailedResponseViewHolder.ClickListener clickListener
                                           )
    {
        activity_ = activity;
        requestBuilder_ = requestBuilder;
        preloadSizeProvider_ = preloadSizeProvider;

        paggingController_ = paggingController;
        recyclerViewItems_ = recyclerViewItems;

        clickListener_ = clickListener;
    }

    public void updateData()
    {
        notifyDataSetChanged();
    }

    public void addLoader()
    {
        SearchRecyclerViewItemFactory.createLoaderRecyclerViewItem(
                SearchRecyclerViewItemFactory.SearchRecyclerViewItemType.VIEW_ITEM_LOADER,
                this,
                paggingController_);
    }

    public void removeLoader()
    {
        SearchRecyclerViewItemFactory.removeLoaderRecyclerViewItem(
                SearchRecyclerViewItemFactory.SearchRecyclerViewItemType.VIEW_ITEM_LOADER,
                this,
                paggingController_);
    }

    public void addFailedResponse()
    {
        SearchRecyclerViewItemFactory.createFailedResponseRecyclerViewItem(
                SearchRecyclerViewItemFactory.SearchRecyclerViewItemType.VIEW_ITEM_FAILED_RESPONSE,
                this,
                paggingController_);
    }

    public void removeFailedResponse()
    {
        SearchRecyclerViewItemFactory.removeFailedResponseRecyclerViewItem(
                SearchRecyclerViewItemFactory.SearchRecyclerViewItemType.VIEW_ITEM_FAILED_RESPONSE,
                this,
                paggingController_);
    }

    @Override
    public int getItemViewType(int position)
    {
        if(position >= 0 && position < this.recyclerViewItems_.size())
        {
            RecyclerViewItem item = this.recyclerViewItems_.get(position);
            if(item instanceof GifResultRecyclerViewItem)
            {
                return VIEW_TYPE_GIF;
            }
            else if(item instanceof LoaderRecyclerViewItem)
            {
                return VIEW_TYPE_LOADING;
            }
            else if(item instanceof FailedResponseRecyclerViewItem)
            {
                return VIEW_TYPE_RESPONSE_FAILED;
            }
        }

        Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to getItemViewType at position" + position);
        return VIEW_TYPE_INVALID;
    }

    @Override
    public long getItemId(int position)
    {
        if(this.recyclerViewItems_ != null)
        {
            if(this.recyclerViewItems_.size() > 0)
            {
                RecyclerViewItem item = this.recyclerViewItems_.get(position);
                if(item instanceof GifResultRecyclerViewItem)
                {
                    GifResultRecyclerViewItem result = (GifResultRecyclerViewItem)item;
                    GifResult gifResult = result.getGifResult();
                    return gifResult.getId().hashCode();
                }
            }
        }
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (viewType == VIEW_TYPE_GIF)
        {
            return new GifViewHolder(LayoutInflater.from(activity_).
                   inflate(R.layout.rv_item_gif_view_layout, parent, false));
        }
        else if (viewType == VIEW_TYPE_LOADING)
        {
            return new LoadingViewHolder(LayoutInflater.from(activity_).
                    inflate(R.layout.rv_item_loader_layout, parent, false));
        }
        else if(viewType == VIEW_TYPE_RESPONSE_FAILED)
        {
            return new FailedResponseViewHolder(LayoutInflater.from(activity_).
                    inflate(R.layout.rv_item_failed_response_layout, parent, false),
                    clickListener_);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof GifViewHolder)
        {
            GifResultRecyclerViewItem gifResultItem = (GifResultRecyclerViewItem)this.recyclerViewItems_.get(position);
            final GifResult gifResult = gifResultItem.getGifResult();

            final GifViewHolder gifViewHolder = (GifViewHolder)holder;
            GlideApp.with(activity_.getApplicationContext())
                    .load(gifResult)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.color.colorBlack)
                    .placeholder(R.color.colorGrey)
                    .centerCrop()
                    .into(gifViewHolder.gifImageView_)
                    .clearOnDetach();

            preloadSizeProvider_.setView(gifViewHolder.gifImageView_);
        }
        else if (holder instanceof LoadingViewHolder)
        {
            LoadingViewHolder loadingHolder = (LoadingViewHolder)holder;
            loadingHolder.progressBar_.setIndeterminate(true);
        }
        //else if (holder instanceof FailedResponseViewHolder)
        //{
            // Do nothing, because FailedResponseViewHolder has only button.
            // Debugging.logClass(Log.DEBUG, LOG_TAG, "FailedResponseViewHolder is binding!");
        //}
    }

    @Override
    public int getItemCount()
    {
        return this.recyclerViewItems_.size();
    }

    @NonNull
    @Override
    public List<GifResult> getPreloadItems(int position)
    {
        int size = this.recyclerViewItems_.size();
        if(position >= 0 && position < size && size > 0)
        {
            RecyclerViewItem item = this.recyclerViewItems_.get(position);
            if(item instanceof GifResultRecyclerViewItem)
            {
                return Collections.singletonList(((GifResultRecyclerViewItem) item).getGifResult());
            }
        }
        return Collections.singletonList(null);
    }

    @Nullable
    @Override
    public RequestBuilder getPreloadRequestBuilder(GifResult item)
    {
        return requestBuilder_.load(item);
    }

    // Describe GIF element on display
    public static class GifViewHolder extends RecyclerView.ViewHolder
    {
        private final ImageView gifImageView_;

        private GifViewHolder(View itemView)
        {
            super(itemView);
            gifImageView_ = (ImageView)itemView.findViewById(R.id.gif_image_view);
        }

        public ImageView getImageView()
        {
            return gifImageView_;
        }
    }

    // Describe loading bar
    public static class LoadingViewHolder extends RecyclerView.ViewHolder
    {
        private ProgressBar progressBar_;

        private LoadingViewHolder(View itemView)
        {
            super(itemView);
            progressBar_ = (ProgressBar)itemView.findViewById(R.id.progressBar);
        }

        public ProgressBar getProgressBar()
        {
            return progressBar_;
        }
    }

    // Describe failed response
    public static class FailedResponseViewHolder extends RecyclerView.ViewHolder
                                                 implements View.OnClickListener
    {
        public interface ClickListener
        {
            void onPositionClicked(int position);
        }

        private Button tryAgainButton_;

        private WeakReference<ClickListener> listenerRef_;

        private FailedResponseViewHolder(View itemView, ClickListener listener)
        {
            super(itemView);
            listenerRef_ = new WeakReference<ClickListener>(listener);
            tryAgainButton_ = (Button) itemView.findViewById(R.id.try_again_button);
            tryAgainButton_.setOnClickListener(this);
        }

        public Button getTryAgainButton()
        {
            return tryAgainButton_;
        }

        // onClick Listener for view
        @Override
        public void onClick(View v)
        {
            if (v.getId() == tryAgainButton_.getId())
            {
                listenerRef_.get().onPositionClicked(getAdapterPosition());
            }
        }
    }

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration
    {
        private int bigSpace_;

        public SpacesItemDecoration(int bigSpace)
        {
            this.bigSpace_ = bigSpace;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent,
                                   RecyclerView.State state)
        {
            outRect.top = bigSpace_;
            outRect.bottom = bigSpace_;
            outRect.right = bigSpace_;
            outRect.left = bigSpace_;
        }
    }
}

