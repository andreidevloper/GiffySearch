package com.as.giffysearch.Models.JSON.Giphy;

import java.util.Arrays;

/**
 * Created by Andrejs Skorinko on 12/3/2017.
 *
 */

// Show the top level result from JSON object
// returned from Giphy's API
public final class SearchResult
{
    private GifResult[] data;
    private Pagination pagination;

    public GifResult[] getData()
    {
        return data;
    }

    public Pagination getPagination()
    {
        return pagination;
    }

    @Override
    public String toString()
    {
        return "SearchResult { " + "data: [ " + Arrays.toString(data) +
                "], Pagination { " + pagination + '}';
    }
}


