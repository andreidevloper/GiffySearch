package com.as.giffysearch.Adapters.RecyclerViewItems;

import android.util.Log;

import com.as.giffysearch.Adapters.RecyclerViewItems.SearchResultRecyclerViewItems.FailedResponseRecyclerViewItem;
import com.as.giffysearch.Adapters.RecyclerViewItems.SearchResultRecyclerViewItems.GifResultRecyclerViewItem;
import com.as.giffysearch.Adapters.RecyclerViewItems.SearchResultRecyclerViewItems.LoaderRecyclerViewItem;
import com.as.giffysearch.Adapters.SearchResultRecyclerViewAdapter;
import com.as.giffysearch.Controllers.PagingController;
import com.as.giffysearch.Models.JSON.Giphy.GifResult;
import com.as.giffysearch.Utility.Debugging;

import java.util.LinkedList;

/**
 * Created by Andrejs Skorinko on 12/9/2017.
 *
 */

public final class SearchRecyclerViewItemFactory
{
    private static final String LOG_TAG = SearchResultRecyclerViewAdapter.class.getSimpleName();
    public enum SearchRecyclerViewItemType { VIEW_ITEM_GIF, VIEW_ITEM_LOADER, VIEW_ITEM_FAILED_RESPONSE }

    static public RecyclerViewItem createGifResultRecyclerViewItem(SearchRecyclerViewItemType itemType,
                                                                   GifResult gifResult)
    {
        switch (itemType)
        {
            case VIEW_ITEM_GIF:
            {
                return new GifResultRecyclerViewItem(gifResult);
            }
            default:
            {
                Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to create GifResultRecyclerViewItem");
            }
        }
        return null;
    }


    static public void createLoaderRecyclerViewItem(SearchRecyclerViewItemType itemType,
                                                       SearchResultRecyclerViewAdapter adapter,
                                                    PagingController pagingController)
    {
        switch (itemType)
        {
            case VIEW_ITEM_LOADER:
            {
                PagingController.ScrollDirection direction = pagingController.getScrollDirection();
                LinkedList<RecyclerViewItem> recyclerViewItems = pagingController.getData();

                LoaderRecyclerViewItem loaderItem = new LoaderRecyclerViewItem();

                int position;
                if(direction == PagingController.ScrollDirection.UP)
                {
                    recyclerViewItems.offerLast(loaderItem);
                    position = recyclerViewItems.size() - 1;
                    pagingController.setLoaderPosition(position);
                    adapter.notifyItemInserted(position);
                }
                else if(direction == PagingController.ScrollDirection.DOWN)
                {
                    recyclerViewItems.offerFirst(loaderItem);
                    position = 0;
                    pagingController.setLoaderPosition(position);
                    adapter.notifyItemInserted(position);
                }
                else if(direction == PagingController.ScrollDirection.IN_PLACE)
                {
                    // new search
                    recyclerViewItems.offerFirst(loaderItem);
                    position = 0;
                    pagingController.setLoaderPosition(position);
                    adapter.notifyItemInserted(position);
                }
                else
                {
                    Debugging.logClass(Log.ERROR, LOG_TAG, "Unknown scroll direction. Can't create LoaderRecyclerViewItem!");
                }
                break;
            }
            default:
            {
                Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to create LoaderRecyclerViewItem");
                break;
            }
        }
    }

    static public void removeLoaderRecyclerViewItem(SearchRecyclerViewItemType itemType,
                                                       SearchResultRecyclerViewAdapter adapter,
                                                       PagingController pagingController)
    {
        switch (itemType)
        {
            case VIEW_ITEM_LOADER:
            {
                int position = pagingController.getLoaderPosition();
                LinkedList<RecyclerViewItem> recyclerViewItems = pagingController.getData();

                boolean inBounds = (position >= 0) && (position < recyclerViewItems.size());
                if(inBounds)
                {
                    RecyclerViewItem item = recyclerViewItems.get(position);
                    if(item instanceof LoaderRecyclerViewItem)
                    {
                        recyclerViewItems.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                    else
                    {
                        Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to remove LoaderRecyclerViewItem at pos: " + position);
                    }
                }
                else
                {
                    Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to remove LoaderRecyclerViewItem at pos: " + position);
                }
                break;
            }
            default:
            {
                Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to remove LoaderRecyclerViewItem !");
                break;
            }
        }
    }


    static public void createFailedResponseRecyclerViewItem(SearchRecyclerViewItemType itemType,
                                                               SearchResultRecyclerViewAdapter adapter,
                                                               PagingController pagingController)
    {
        switch (itemType)
        {
            case VIEW_ITEM_FAILED_RESPONSE:
            {
                PagingController.ScrollDirection direction = pagingController.getScrollDirection();
                LinkedList<RecyclerViewItem> recyclerViewItems = pagingController.getData();

                FailedResponseRecyclerViewItem failedResponseItem = new FailedResponseRecyclerViewItem();

                int position;
                if(direction == PagingController.ScrollDirection.UP)
                {
                    recyclerViewItems.offerLast(failedResponseItem);
                    position = recyclerViewItems.size() - 1;
                    pagingController.setFailedResponsePosition(position);
                    adapter.notifyItemInserted(position);
                }
                else if(direction == PagingController.ScrollDirection.DOWN)
                {
                    recyclerViewItems.offerFirst(failedResponseItem);
                    position = 0;
                    pagingController.setFailedResponsePosition(position);
                    adapter.notifyItemInserted(position);
                }
                else if(direction == PagingController.ScrollDirection.IN_PLACE)
                {
                    // new search
                    recyclerViewItems.offerFirst(failedResponseItem);
                    position = 0;
                    pagingController.setFailedResponsePosition(position);
                    adapter.notifyItemInserted(position);
                }
                else
                {
                    Debugging.logClass(Log.ERROR, LOG_TAG, "Unknown scroll direction. Can't create FailedResponseRecyclerViewItem!");
                    break;
                }
                break;
            }
            default:
            {
                Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to create FailedResponseRecyclerViewItem");
                break;
            }
        }
    }

    static public void removeFailedResponseRecyclerViewItem(SearchRecyclerViewItemType itemType,
                                                       SearchResultRecyclerViewAdapter adapter,
                                                       PagingController pagingController)
    {
        switch (itemType)
        {
            case VIEW_ITEM_FAILED_RESPONSE:
            {
                int position = pagingController.getFailedResponsePosition();
                LinkedList<RecyclerViewItem> recyclerViewItems = pagingController.getData();

                boolean inBounds = (position >= 0) && (position < recyclerViewItems.size());
                if(inBounds)
                {
                    RecyclerViewItem item = recyclerViewItems.get(position);
                    if(item instanceof FailedResponseRecyclerViewItem)
                    {
                        recyclerViewItems.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                    else
                    {
                        Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to remove FailedResponseRecyclerViewItem at pos: " + position);
                    }
                }
                else
                {
                    Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to remove FailedResponseRecyclerViewItem at pos: " + position);
                }
                break;

            }
            default:
            {
                Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to remove FailedResponseRecyclerViewItem !");
                break;
            }
        }
    }


}
