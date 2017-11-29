package com.as.giffysearch.Utility;

import android.util.Log;

/**
 * Created by Andrejs Skorinko on 11/29/2017.
 *
 */

public class Debugging
{
    public static void logClass(int level, String logTag, String outputText)
    {
        switch (level)
        {
            case Log.DEBUG:
            {
                Log.d(logTag, outputText);
                break;
            }

            case Log.INFO:
            {
                Log.i(logTag, outputText);
                break;
            }
            case Log.WARN:
            {
                Log.w(logTag, outputText);
                break;
            }

            case Log.ERROR:
            {
                Log.e(logTag, outputText);
                break;
            }

            case Log.VERBOSE:
            {
                Log.v(logTag, outputText);
                break;
            }

            default:
            {
                Log.v(logTag, outputText);
                break;
            }
        }
    }
}

