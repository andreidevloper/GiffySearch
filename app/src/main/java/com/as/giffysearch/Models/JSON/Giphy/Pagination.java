package com.as.giffysearch.Models.JSON.Giphy;

/**
 * Created by Andrejs Skorinko on 12/3/2017.
 *
 */

// Show the bottom level result from JSON object
// returned from Giphy's API
public final class Pagination
{
    private int total_count;
    private int count;
    private int offset;

    public int getTotalCount()
    {
        return total_count;
    }
    public int getCount()
    {
        return count;
    }
    public int getOffset()
    {
        return offset;
    }

    @Override
    public String toString()
    {
        return "{ total_count='" + total_count + '\'' +
                ", count= " + count +
                ", offset= " + offset + '}';
    }
}


