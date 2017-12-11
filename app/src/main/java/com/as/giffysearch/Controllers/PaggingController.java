package com.as.giffysearch.Controllers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.as.giffysearch.Adapters.RecyclerViewItems.RecyclerViewItem;
import com.as.giffysearch.Adapters.RecyclerViewItems.SearchRecyclerViewItemFactory;
import com.as.giffysearch.Models.JSON.Giphy.GifResult;
import com.as.giffysearch.Models.JSON.Giphy.Pagination;
import com.as.giffysearch.Models.JSON.Giphy.SearchResult;
import com.as.giffysearch.ThirdParty.Giphy.GiphyAPI;
import com.as.giffysearch.Utility.Debugging;

import java.util.LinkedList;

import static com.as.giffysearch.Adapters.RecyclerViewItems.SearchRecyclerViewItemFactory.SearchRecyclerViewItemType.VIEW_ITEM_GIF;

/**
 * Created by Andrejs Skorinko on 12/6/2017.
 *
 */

public class PaggingController
{
    private static final String LOG_TAG = PaggingController.class.getSimpleName();

    // Start page counting from 0
    // 1 page size = GiphyAPI.DEFAULT_LIMIT
    public static final int MAX_LOADED_PAGE = 1;

    public enum ScrollDirection {UNKNOWN, UP, DOWN, IN_PLACE }
    public static final int NEXT_PAGE = 1;
    public static final int PREVIOUS_PAGE = 2;

    private Context context_;

    private static final LinkedList<RecyclerViewItem> EMPTY_RECYCLE_VIEW_ITEMS = new LinkedList<>();
    private LinkedList<RecyclerViewItem> recyclerViewItems_ = EMPTY_RECYCLE_VIEW_ITEMS;

    // Search input
    private String currentQuery_;
    private int currentOffset_ = GiphyAPI.DEFAULT_OFFSET;
    private int currentLimit_ = GiphyAPI.DEFAULT_LIMIT;

    // one page size = GiphyAPI.DEFAULT_LIMIT
    private int currentPage_ = 0;
    private int currentMaxItemsCount_ = 0;
    private int lastSearchedGifPos_ = 0;

    private ScrollDirection currentScrollDirection_ = ScrollDirection.UNKNOWN;
    private boolean isReachEnd_ = false;
    // How many elements are in the end ?
    private int endElementsCount_ = 0;
    private boolean isReachTop_ = false;

    // Loader
    private boolean isLoading_ = false;
    private int loaderPosition_ = 0;

    // Failed response
    private boolean isFailedResponse_ = false;
    private int failedResponsePosition_ = 0;

    // Class helps get GIFS using GIPHY service through OkHttpAPI
    private GiphyAPI giphyAPI_ = GiphyAPI.INSTANCE;

    // Profiler
    private Debugging.Profiler profiler_;

    public PaggingController(Context context, int limit, int offset)
    {
        this.currentOffset_ = offset;
        this.currentLimit_ = limit;
        context_ = context;

        profiler_ = new Debugging.Profiler();
    }

    public void startSearch(String query)
    {
        if(!query.equals(currentQuery_))
        {
            currentQuery_ = query;
            sendSearchRequest();
        }
    }

    public void continueSearch(int offset)
    {
        if(currentOffset_ != offset)
        {
            currentOffset_ = offset;
            sendSearchRequest();
        }
    }

    public void restartSearch()
    {
        sendSearchRequest();
    }

    private void sendSearchRequest()
    {
        profiler_.startTimer();
        giphyAPI_.startSearch(currentQuery_, currentLimit_, currentOffset_);
    }

    public void setSearchResult(SearchResult searchResult, boolean isSuccessful)
    {
        Pagination pagination = null;
        boolean isEmpty = false;
        if(searchResult != null)
        {
            pagination = searchResult.getPagination();
            int totalCount = pagination.getTotalCount();
            int count = pagination.getCount();
            if(totalCount == 0 && count == 0)
            {
                Toast.makeText(context_, "Type another search input! Nothing was searched!", Toast.LENGTH_LONG).show();
                isEmpty = true;
            }
        }

        if(searchResult != null && !isEmpty && isSuccessful)
        {
            if(currentPage_ == 0 && lastSearchedGifPos_ == 0)
            {
                // New search => get how total gifs we've found
                setMaxItems(pagination.getTotalCount());
            }
            else if(currentPage_ == 0 && lastSearchedGifPos_ > 0)
            {
                Toast.makeText(context_, "Top is reached!", Toast.LENGTH_LONG).show();
                isReachTop_ = true;
            }

            if(currentScrollDirection_ == ScrollDirection.UP)
            {
                setLastSearchedGifPos(lastSearchedGifPos_ + pagination.getCount());
            }
            else if(currentScrollDirection_ == ScrollDirection.DOWN)
            {
                setLastSearchedGifPos(lastSearchedGifPos_ - pagination.getCount());
            }
            else if(currentScrollDirection_ == ScrollDirection.IN_PLACE)
            {
                // start search
                setLastSearchedGifPos(lastSearchedGifPos_ + pagination.getCount());
            }

            setData(searchResult.getData());
        }

        profiler_.stopTimer();
        profiler_.logDiffTime(LOG_TAG);

        Debugging.logClass(Log.DEBUG, LOG_TAG, "recycler view size: " + recyclerViewItems_.size());
        Debugging.logClass(Log.DEBUG, LOG_TAG, "last element pos: " + lastSearchedGifPos_);
        Debugging.logClass(Log.DEBUG, LOG_TAG, "current page: " + currentPage_);
    }

    public LinkedList<RecyclerViewItem> getData()
    {
        return recyclerViewItems_;
    }

    private void setData(final GifResult[] gifsData)
    {
        if(currentPage_ > MAX_LOADED_PAGE || currentScrollDirection_ == ScrollDirection.DOWN)
        {
            if(!isReachEnd_ && currentScrollDirection_ == ScrollDirection.UP)
            {
                if(lastSearchedGifPos_ >= currentMaxItemsCount_)
                {
                    Toast.makeText(context_, "End is reached!", Toast.LENGTH_LONG).show();

                    isReachEnd_ = true;
                    endElementsCount_ = gifsData.length;

                    pushBackPage(gifsData);
                }
                else
                {
                    // DEQUE operations
                    popFrontPage();
                    pushBackPage(gifsData);
                }
            }
            else if(currentScrollDirection_ == ScrollDirection.DOWN)
            {
                // Debugging.logClass(Log.DEBUG, LOG_TAG, "LOADING DOWN - currentPage = " + currentPage_);

                // DEQUE operations
                popBackPage();
                pushFrontPage(gifsData);

                // Adjust current page
                // (due to PREVIOUS_PAGE subtraction from current page in EndlessRecyclerViewScrollListener class)
                currentPage_ += NEXT_PAGE;
            }
        }
        else
        {
            if(lastSearchedGifPos_ >= currentMaxItemsCount_)
            {
                isReachEnd_ = true;
            }

            pushBackPage(gifsData);
        }
    }

    private void popFrontPage()
    {
        int itemsPerPage = GiphyAPI.DEFAULT_LIMIT;
        for(int i = 0; i < itemsPerPage; i++)
        {
            this.recyclerViewItems_.pollFirst();
        }
        //Debugging.logClass(Log.DEBUG, LOG_TAG, "gifResult removed" + ", gifResults_ size=" + gifResults_.size());

        // Adjust loader position
        loaderPosition_ -= itemsPerPage;
    }

    private void pushBackPage(final GifResult[] gifsData)
    {
        if(isReachTop_)
        {
            isReachTop_ = false;
        }

        if (gifsData != null)
        {
            for(GifResult gif : gifsData)
            {
                this.recyclerViewItems_.offerLast(
                        SearchRecyclerViewItemFactory.createGifResultRecyclerViewItem(VIEW_ITEM_GIF, gif));
            }
        }
        else
        {
            this.recyclerViewItems_ = EMPTY_RECYCLE_VIEW_ITEMS;
        }
        //Debugging.logClass(Log.DEBUG, LOG_TAG, "gifResult added"  + ", gifResults_ size=" + gifResults_.size());
    }

    private void popBackPage()
    {
        if(isReachEnd_)
        {
            // First pop end items
            for(int i = 0; i < endElementsCount_; i++)
            {
                this.recyclerViewItems_.pollLast();
            }
            setLastSearchedGifPos(lastSearchedGifPos_ - endElementsCount_);
            resetReachEnd();
            popBackPage();
        }
        else
        {
            int itemsPerPage = GiphyAPI.DEFAULT_LIMIT;
            for(int i = 0; i < itemsPerPage; i++)
            {
                this.recyclerViewItems_.pollLast();
            }
        }
        //Debugging.logClass(Log.DEBUG, LOG_TAG, "gifResult removed" + ", gifResults_ size=" + gifResults_.size());
    }

    private void pushFrontPage(final GifResult[] gifsData)
    {
        if (gifsData != null)
        {
            int size = gifsData.length;
            if(size > 0)
            {
                for(int counter = size - 1; counter >= 0; counter--)
                {
                    GifResult gif = gifsData[counter];
                    this.recyclerViewItems_.offerFirst(
                            SearchRecyclerViewItemFactory.createGifResultRecyclerViewItem(VIEW_ITEM_GIF, gif));
                }

                int itemsPerPage = GiphyAPI.DEFAULT_LIMIT;
                // Adjust loader position
                loaderPosition_ += itemsPerPage;
            }
        }
        else
        {
            this.recyclerViewItems_ = EMPTY_RECYCLE_VIEW_ITEMS;
        }
        //  Debugging.logClass(Log.DEBUG, LOG_TAG, "gifResult added"  + ", gifResults_ size=" + gifResults_.size());
    }

    public void clear()
    {
        int size = this.recyclerViewItems_.size();
        if (size > 0)
        {
            for (int i = 0; i < size; i++)
            {
                this.recyclerViewItems_.remove(0);
            }
        }

        currentOffset_ = 0;

        setCurrentPage(0);
        resetScrollDirection();

        isLoading_ = false;
        setLoaderPosition(0);

        setMaxItems(0);
        setLastSearchedGifPos(0);

        resetReachTop();
        resetReachEnd();

        currentQuery_ = "";
    }

    // --------------- PAGE OPERATIONS ---------------
    public void setCurrentPage(int page)
    {
        currentPage_ = page;
    }

    public int getCurrentPage()
    {
        return currentPage_;
    }

    private void unrollCurrentPage()
    {
        if(currentPage_ != 0)
        {
            currentPage_--;
        }
    }

    // --------------- LOADER OPERATIONS ---------------

    public void setLoaderPosition(int position)
    {
        loaderPosition_ = position;
    }

    public int getLoaderPosition()
    {
        return loaderPosition_;
    }

    public void setLoading(boolean loading)
    {
        isLoading_ = loading;
    }

    public boolean isLoading()
    {
        return isLoading_;
    }

    // --------------- FAILED RESPONSE OPERATIONS ---------------

    public void setFailedResponsePosition(int position)
    {
        failedResponsePosition_ = position;
    }

    public int getFailedResponsePosition()
    {
        return failedResponsePosition_;
    }

    public void setIsFailedResponse(boolean isFailed)
    {
        isFailedResponse_ = isFailed;
    }

    public boolean isFailedResponse()
    {
        return isFailedResponse_;
    }


    // --------------- SCROLL DIRECTION OPERATIONS ---------------

    public void setScrollDirection(ScrollDirection direction)
    {
        currentScrollDirection_ = direction;
    }

    public ScrollDirection getScrollDirection()
    {
        return currentScrollDirection_;
    }

    public void resetScrollDirection()
    {
        setScrollDirection(ScrollDirection.UNKNOWN);
    }

    // --------------- END/TOP OPERATIONS ---------------

    public boolean isReachedEnd()
    {
        return isReachEnd_;
    }

    public boolean isReachedTop()
    {
        return isReachTop_;
    }

    private void resetReachEnd()
    {
        endElementsCount_ = 0;
        isReachEnd_ = false;
    }

    private void resetReachTop()
    {
        isReachTop_ = false;
    }

    // --------------- LAST SEARCHED/MAX ITEMS OPERATIONS ---------------

    private void setMaxItems(int maxItems)
    {
        currentMaxItemsCount_ = maxItems;
    }

    private void setLastSearchedGifPos(int lastSearchedGifPos)
    {
        lastSearchedGifPos_ = lastSearchedGifPos;
        if(lastSearchedGifPos_ < 0)
        {
            lastSearchedGifPos_ = 0;
        }
    }

    // --------------- PROFILERS OPERATIONS ---------------
    public long getLastRequestTime() { return profiler_.getDiffTime(); }
    public void logLastRequestTime() { profiler_.logDiffTime(LOG_TAG); }
    public void logAverageRequestTime() { profiler_.logAverageDiffTime(LOG_TAG); }
}
