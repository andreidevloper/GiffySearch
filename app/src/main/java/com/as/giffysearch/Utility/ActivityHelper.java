package com.as.giffysearch.Utility;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.as.giffysearch.MainActivity;

/**
 * Created by Andrejs Skorinko on 11/29/2017.
 *
 */

public class ActivityHelper
{
    public static boolean hideKeyboard(Context context, Activity activity, int flag)
    {
        if(context == null || activity == null)
        {
            Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Can't hide keybaord. Context or/and activity are null !");
            return false;
        }

        InputMethodManager inputManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);

        View currentFocus = activity.getCurrentFocus();
        if(currentFocus != null)
        {
            if(!inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), flag))
            {
                Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Failed to hide keyboard !");
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            Debugging.logClass(Log.WARN, MainActivity.LOG_TAG, "Can't hide keybaord. Nothing has focus !");
            return false;
        }
    }

    public static boolean hideKeyboard(Activity activity, View view, int flag)
    {
        if(view == null || activity == null)
        {
            Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Can't hide keybaord. View or/and activity are null !");
            return false;
        }

        InputMethodManager inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        if(!inputManager.hideSoftInputFromWindow(view.getWindowToken(), flag))
        {
            Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Failed to hide keyboard !");
            return false;
        }
        else
        {
            return true;
        }
    }
}

