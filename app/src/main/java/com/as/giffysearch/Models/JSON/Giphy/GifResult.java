package com.as.giffysearch.Models.JSON.Giphy;

/**
 * Created by Andrejs Skorinko on 12/3/2017.
 *
 */

// Show the individual GIF image
// returned from Giphy's API
public final class GifResult
{
    private String id;
    private GifUrlSet images;

    public String getId()
    {
        return id;
    }

    public GifUrlSet getImages()
    {
        return images;
    }

    @Override
    public String toString()
    {
        return "GifResult {" + "id='" + id + '\'' +
                ", images {" + images + '}';
    }
}


