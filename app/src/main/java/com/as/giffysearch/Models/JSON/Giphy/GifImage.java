package com.as.giffysearch.Models.JSON.Giphy;

/**
 * Created by Andrejs Skorinko on 12/3/2017.
 *
 */

// Show the one individual GIF image with one particular url, size and dimension (JSON object)
// returned from Giphy's API
public final class GifImage
{
    private String url;
    private int width;
    private int height;

    public String getUrl()
    {
        return url;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    @Override
    public String toString()
    {
        return "GifImage { " + "url='" + url + '\'' +
               ", width= " + width +
               ", height= " + height + '}';
    }
}


