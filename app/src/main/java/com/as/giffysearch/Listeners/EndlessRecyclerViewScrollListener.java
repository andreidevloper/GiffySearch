package com.as.giffysearch.Listeners;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;

import com.as.giffysearch.Controllers.PaggingController;
import com.as.giffysearch.Utility.Debugging;

/**
 * Created by Andrejs Skorinko on 12/6/2017.
 *
 */

// Initial source - https://gist.github.com/junfengren/f06c6f19e5832c62fe93b61ac8f72ff9
// Guide - https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews-and-RecyclerView

// Modified initial source code in order to load 2 pages (PaggingController.MAX_LOADED_PAGE) at a time
// during down/up scrolling
// 1 page size = GiphyAPI.DEFAULT_LIMIT
public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener
{
    private static final String LOG_TAG = EndlessRecyclerViewScrollListener.class.getSimpleName();

    private static final int NO_FOOTER_VIEW = Integer.MIN_VALUE;

    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int visibleThreshold_ = 3;

    // The current offset index of data you have loaded
    private int currentPage_ = 0;

    // Can load data when user scroll list down ?
    private boolean isCanLoadMoreScrollDown = false;

    // PaggingController helps to determine if data is still loading or not
    // Also helps to determine current page index and check if end of list was reached
    private PaggingController paggingController_;
    // True if we are still waiting for the last set of data to load.
    private boolean loading_ = false;

    // Sets the footerViewType
    private int defaultNoFooterViewType_ = NO_FOOTER_VIEW;
    private int footerViewType_ = NO_FOOTER_VIEW;

    // Detect scroll down/up
    private int firstVisiblePreviousPos_ = 0;
    private int lastVisiblePreviousPos_ = 0;

    // Help to find last and fist visible elements
    private RecyclerView.LayoutManager layoutManager_;

    public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager, PaggingController paggingController)
    {
        initScrollListener();
        this.layoutManager_ = layoutManager;
        this.paggingController_ = paggingController;
    }

    public EndlessRecyclerViewScrollListener(GridLayoutManager layoutManager)
    {
        initScrollListener();
        this.layoutManager_ = layoutManager;
        visibleThreshold_ = visibleThreshold_ * layoutManager.getSpanCount();
    }

    public EndlessRecyclerViewScrollListener(StaggeredGridLayoutManager layoutManager)
    {
        initScrollListener();
        this.layoutManager_ = layoutManager;
        visibleThreshold_ = visibleThreshold_ * layoutManager.getSpanCount();
    }

    // Init from self-define
    private void initScrollListener()
    {
        footerViewType_ = getFooterViewType(defaultNoFooterViewType_);
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    @Override
    public void onScrolled(final RecyclerView view, int dx, int dy)
    {
        int firstVisibleItemPosition = getFirstVisibleItemPosition();
        int lastVisibleItemPosition = getLastVisibleItemPosition();
        PaggingController.ScrollDirection scrollDirection = getScrollDirection(firstVisibleItemPosition, lastVisibleItemPosition);

        boolean isReachedEnd = paggingController_.isReachedEnd();
        if((isReachedEnd && scrollDirection == PaggingController.ScrollDirection.UP) ||
           (isReachedEnd && paggingController_.getCurrentPage() == 0))
        {
            return;
        }

        RecyclerView.Adapter adapter = view.getAdapter();
        int totalItemCount = adapter.getItemCount();
        boolean isAllowLoadMore = false;
        if(scrollDirection == PaggingController.ScrollDirection.UP &&
           (lastVisibleItemPosition + visibleThreshold_) >= totalItemCount)
        {
            isAllowLoadMore = true;
            // Debugging.logClass(Log.DEBUG, LOG_TAG, "ALLOW LOAD MORE - DIRECTION UP");
        }
        else if(paggingController_.getCurrentPage() != PaggingController.MAX_LOADED_PAGE &&
                isCanLoadMoreScrollDown &&
                scrollDirection == PaggingController.ScrollDirection.DOWN &&
                (firstVisibleItemPosition - visibleThreshold_) <= 0)
        {
            isAllowLoadMore = true;
            // Debugging.logClass(Log.DEBUG, LOG_TAG, "ALLOW LOAD MORE - DIRECTION DOWN");
        }


        if (isAllowLoadMore)
        {
            if (isUseFooterView())
            {
                if (!isFooterView(adapter))
                {
                    currentPage_ = paggingController_.getCurrentPage();
                    loading_ = paggingController_.isLoading();
                }
            }
            else
            {
                currentPage_ = paggingController_.getCurrentPage();
                loading_ = paggingController_.isLoading();
            }

            boolean isFailedResponse = paggingController_.isFailedResponse();

            if (!loading_ && !isFailedResponse)
            {
                // If it isn’t currently loading, we check to see if we have reached
                // the visibleThreshold and need to reload more data.
                // If we do need to reload some more data, we execute onLoadMore to fetch the data.
                // threshold should reflect how many total columns there are too
                if(scrollDirection == PaggingController.ScrollDirection.UP)
                {
                    currentPage_ += PaggingController.NEXT_PAGE;

                    if(currentPage_ > PaggingController.MAX_LOADED_PAGE)
                    {
                        isCanLoadMoreScrollDown = true;
                    }

                    // Debugging.logClass(Log.DEBUG, LOG_TAG, "LOADING UP - currentPage = " + currentPage_);
                }
                else if(scrollDirection == PaggingController.ScrollDirection.DOWN)
                {
                    // get page before previous page
                    currentPage_ -= PaggingController.PREVIOUS_PAGE;
                    if(currentPage_ < 0)
                    {
                        currentPage_ = 0;
                    }

                    if(currentPage_ == 0)
                    {
                        isCanLoadMoreScrollDown = false;
                    }

                    if(isReachedEnd)
                    {
                        // Due to popped out two pages in pagging controller
                        currentPage_ -= 1;
                    }

                    // Debugging.logClass(Log.DEBUG, LOG_TAG, "LOADING DOWN - currentPage = " + currentPage_);
                }

                onLoadMore(currentPage_, scrollDirection);

                loading_ = true;
            }
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState)
    {
        super.onScrollStateChanged(recyclerView, newState);
    }

    private boolean isUseFooterView()
    {
        return footerViewType_ != defaultNoFooterViewType_;
    }

    private boolean isFooterView(RecyclerView.Adapter adapter)
    {
        boolean isFooterView = false;
        int totalItemCount = adapter.getItemCount();

        if (totalItemCount > 0)
        {
            int lastPosition = totalItemCount - 1;
            int lastViewType = adapter.getItemViewType(lastPosition);

            int firstPosition = 0;
            int firstViewType = adapter.getItemViewType(firstPosition);
            // lastview is loader or firstview is loader ?
            isFooterView = lastViewType == footerViewType_ || firstViewType == footerViewType_;
        }

        // Log.i(mTag, "isFooterView:" + isFooterView);
        return isFooterView;
    }

    private int getFirstVisibleItemPosition()
    {
        int firstVisibleItemPosition = 0;

        if (layoutManager_ instanceof StaggeredGridLayoutManager)
        {
            int[] firstVisibleItemPositions = ((StaggeredGridLayoutManager)layoutManager_)
                    .findFirstVisibleItemPositions(null);
            // Get minimum element within the list
            firstVisibleItemPosition = getFirstVisibleItem(firstVisibleItemPositions);
        }
        else if (layoutManager_ instanceof LinearLayoutManager)
        {
            firstVisibleItemPosition = ((LinearLayoutManager)layoutManager_)
                    .findFirstVisibleItemPosition();
        }
        else if (layoutManager_ instanceof GridLayoutManager)
        {
            firstVisibleItemPosition = ((GridLayoutManager)layoutManager_)
                    .findFirstVisibleItemPosition();
        }

        return firstVisibleItemPosition;
    }

    private int getFirstVisibleItem(int[] firstVisibleItemPositions)
    {
        int minSize = 0;
        for (int i = 0; i < firstVisibleItemPositions.length; i++)
        {
            if (i == 0)
            {
                minSize = firstVisibleItemPositions[i];
            }
            else if (firstVisibleItemPositions[i] < minSize)
            {
                minSize = firstVisibleItemPositions[i];
            }
        }
        return minSize;
    }

    private int getLastVisibleItemPosition()
    {
        int lastVisibleItemPosition = 0;

        if (layoutManager_ instanceof LinearLayoutManager)
        {
            lastVisibleItemPosition = ((LinearLayoutManager)layoutManager_)
                                                .findLastVisibleItemPosition();
        }
        if (layoutManager_ instanceof StaggeredGridLayoutManager)
        {
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager)layoutManager_)
                                                .findLastVisibleItemPositions(null);
            // Get maximum element within the list
            lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);

        }
        else if (layoutManager_ instanceof GridLayoutManager)
        {
            lastVisibleItemPosition = ((GridLayoutManager)layoutManager_)
                    .findLastVisibleItemPosition();
        }


        return lastVisibleItemPosition;
    }

    private int getLastVisibleItem(int[] lastVisibleItemPositions)
    {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++)
        {
            if (i == 0)
            {
                maxSize = lastVisibleItemPositions[i];

            }
            else if (lastVisibleItemPositions[i] > maxSize)
            {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    private PaggingController.ScrollDirection getScrollDirection(int firstVisibleItemPosition, int lastVisibleItemPosition)
    {
        PaggingController.ScrollDirection direction = PaggingController.ScrollDirection.UNKNOWN;
        if(firstVisibleItemPosition > firstVisiblePreviousPos_ ||
                lastVisibleItemPosition > lastVisiblePreviousPos_)
        {
            direction = PaggingController.ScrollDirection.UP;
            //Debugging.logClass(Log.DEBUG, LOG_TAG,"SCROLLED UP!");
        }
        else if(firstVisibleItemPosition < firstVisiblePreviousPos_ ||
                lastVisibleItemPosition < lastVisiblePreviousPos_)
        {
            direction = PaggingController.ScrollDirection.DOWN;
            //Debugging.logClass(Log.DEBUG, LOG_TAG,"SCROLLED DOWN!");
        }

        firstVisiblePreviousPos_ = firstVisibleItemPosition;
        lastVisiblePreviousPos_ = lastVisibleItemPosition;

        return direction;
    }

    // set FooterView type
    // if don't use footview loadmore default: defaultNoFooterViewType
    public abstract int getFooterViewType(int defaultNoFooterViewType);

    // Defines the process for actually loading more data based on page
    public abstract void onLoadMore(int page, PaggingController.ScrollDirection scrollDirection);
}