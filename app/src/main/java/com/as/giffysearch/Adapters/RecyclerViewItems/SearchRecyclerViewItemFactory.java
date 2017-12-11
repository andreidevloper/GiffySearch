package com.as.giffysearch.Adapters.RecyclerViewItems;

import android.util.Log;

import com.as.giffysearch.Adapters.RecyclerViewItems.SearchResultRecyclerViewItems.FailedResponseRecyclerViewItem;
import com.as.giffysearch.Adapters.RecyclerViewItems.SearchResultRecyclerViewItems.GifResultRecyclerViewItem;
import com.as.giffysearch.Adapters.RecyclerViewItems.SearchResultRecyclerViewItems.LoaderRecyclerViewItem;
import com.as.giffysearch.Adapters.SearchResultRecyclerViewAdapter;
import com.as.giffysearch.Controllers.PaggingController;
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
                                                       PaggingController paggingController)
    {
        switch (itemType)
        {
            case VIEW_ITEM_LOADER:
            {
                PaggingController.ScrollDirection direction = paggingController.getScrollDirection();
                LinkedList<RecyclerViewItem> recyclerViewItems = paggingController.getData();

                LoaderRecyclerViewItem loaderItem = new LoaderRecyclerViewItem();

                int position;
                if(direction == PaggingController.ScrollDirection.UP)
                {
                    recyclerViewItems.offerLast(loaderItem);
                    position = recyclerViewItems.size() - 1;
                    paggingController.setLoaderPosition(position);
                    adapter.notifyItemInserted(position);
                }
                else if(direction == PaggingController.ScrollDirection.DOWN)
                {
                    recyclerViewItems.offerFirst(loaderItem);
                    position = 0;
                    paggingController.setLoaderPosition(position);
                    adapter.notifyItemInserted(position);
                }
                else if(direction == PaggingController.ScrollDirection.IN_PLACE)
                {
                    // new search
                    recyclerViewItems.offerFirst(loaderItem);
                    position = 0;
                    paggingController.setLoaderPosition(position);
                    adapter.notifyItemInserted(position);
                }
                else
                {
                    Debugging.logClass(Log.ERROR, LOG_TAG, "Unknown scroll direction. Can't create LoaderRecyclerViewItem!");
                    break;
                }
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
                                                       PaggingController paggingController)
    {
        switch (itemType)
        {
            case VIEW_ITEM_LOADER:
            {
                int position = paggingController.getLoaderPosition();
                LinkedList<RecyclerViewItem> recyclerViewItems = paggingController.getData();

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
                                                               PaggingController paggingController)
    {
        switch (itemType)
        {
            case VIEW_ITEM_FAILED_RESPONSE:
            {
                PaggingController.ScrollDirection direction = paggingController.getScrollDirection();
                LinkedList<RecyclerViewItem> recyclerViewItems = paggingController.getData();

                FailedResponseRecyclerViewItem failedResponseItem = new FailedResponseRecyclerViewItem();

                int position;
                if(direction == PaggingController.ScrollDirection.UP)
                {
                    recyclerViewItems.offerLast(failedResponseItem);
                    position = recyclerViewItems.size() - 1;
                    paggingController.setFailedResponsePosition(position);
                    adapter.notifyItemInserted(position);
                }
                else if(direction == PaggingController.ScrollDirection.DOWN)
                {
                    recyclerViewItems.offerFirst(failedResponseItem);
                    position = 0;
                    paggingController.setFailedResponsePosition(position);
                    adapter.notifyItemInserted(position);
                }
                else if(direction == PaggingController.ScrollDirection.IN_PLACE)
                {
                    // new search
                    recyclerViewItems.offerFirst(failedResponseItem);
                    position = 0;
                    paggingController.setFailedResponsePosition(position);
                    adapter.notifyItemInserted(position);
                }
                else
                {
                    Debugging.logClass(Log.ERROR, LOG_TAG, "Unknown scroll direction. Can't create FailedResponseRecyclerViewItem!");
                    break;
                }
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
                                                       PaggingController paggingController)
    {
        switch (itemType)
        {
            case VIEW_ITEM_FAILED_RESPONSE:
            {
                int position = paggingController.getFailedResponsePosition();
                LinkedList<RecyclerViewItem> recyclerViewItems = paggingController.getData();

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

            }
            default:
            {
                Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to remove FailedResponseRecyclerViewItem !");
                break;
            }
        }
    }


}
