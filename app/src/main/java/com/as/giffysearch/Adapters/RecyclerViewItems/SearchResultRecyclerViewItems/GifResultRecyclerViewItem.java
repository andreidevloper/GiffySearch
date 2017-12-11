package com.as.giffysearch.Adapters.RecyclerViewItems.SearchResultRecyclerViewItems;

import com.as.giffysearch.Adapters.RecyclerViewItems.RecyclerViewItem;
import com.as.giffysearch.Models.JSON.Giphy.GifResult;

/**
 * Created by Andrejs Skorinko on 12/9/2017.
 *
 */

public final class GifResultRecyclerViewItem extends RecyclerViewItem
{
    private GifResult gifResult_;

    public GifResultRecyclerViewItem(GifResult gifResult)
    {
        gifResult_ = gifResult;
    }

    public GifResult getGifResult()
    {
        return gifResult_;
    }
}
