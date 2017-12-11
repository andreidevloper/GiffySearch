package com.as.giffysearch.Utility;

import android.content.Context;

import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import android.net.ConnectivityManager;

import android.util.Log;

import com.as.giffysearch.MainActivity;

/**
 * Created by Andrejs Skorinko on 11/29/2017.
 *
 */

public class ActivityHelper
{
    public static void hideKeyboard(Context applicationContext, View view)
    {
        if(view == null || applicationContext == null)
        {
            Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Can't hide keyboard. View or/and applicationContext are null !");
            return;
        }

        InputMethodManager inputManager = (InputMethodManager)applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputManager != null)
        {
            if(!inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0))
            {
                Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Failed to hide keyboard !");
            }
        }
        else
        {
            Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Failed to hide keyboard: InputMethodManager is null !");
        }
    }

    public static void showKeyboard(Context applicationContext)
    {
        if(applicationContext == null)
        {
            Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Can't hide keyboard. View or/and applicationContext are null !");
            return;
        }

        InputMethodManager inputManager = (InputMethodManager)applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputManager != null)
        {
            inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        else
        {
            Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Failed to show keyboard: InputMethodManager is null ! ");
        }
    }

    public static boolean isNetworkAvailable(Context applicationContext)
    {
        if(applicationContext == null)
        {
            Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Can't check network availability. ApplicationContext is null !");
            return false;
        }
        ConnectivityManager manager = (ConnectivityManager)applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(manager != null)
        {
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if(networkInfo != null)
            {
                return networkInfo.isConnected();
            }
            else
            {
                Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Failed to check network availability: NetworkInfo is null!");
                return false;
            }
        }
        else
        {
            Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "Failed to check network availability: ConnectivityManager is null!");
            return false;
        }
    }
}

