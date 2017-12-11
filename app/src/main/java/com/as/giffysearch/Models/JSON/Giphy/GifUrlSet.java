package com.as.giffysearch.Models.JSON.Giphy;

/**
 * Created by Andrejs Skorinko on 12/3/2017.
 *
 */

// Show the individual GIF image's different sizes and dimensions with urls (JSON object)
// returned from Giphy's API
public final class GifUrlSet
{
    private GifImage original;
    private GifImage fixed_width;
    private GifImage fixed_height;

    public GifImage getOriginal()
    {
        return original;
    }

    public GifImage getFixedWidth()
    {
        return fixed_width;
    }

    public GifImage getFixedHeight()
    {
        return fixed_height;
    }

    @Override
    public String toString() {
        return "GifUrlSet {" + "original=" + original +
                ", fixed_width=" + fixed_width +
                ", fixed_height=" + fixed_height + '}';
    }
}


