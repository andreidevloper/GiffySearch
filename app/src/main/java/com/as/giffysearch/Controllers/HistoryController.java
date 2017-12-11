package com.as.giffysearch.Controllers;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;

import com.as.giffysearch.R;
import com.as.giffysearch.Utility.Debugging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by Andrejs Skorinko on 12/9/2017.
 */

public final class HistoryController
{
    private static final String LOG_TAG = HistoryController.class.getSimpleName();
    private static final String HISTORY_PREFS_NAME = "HistoryPrefsFile";
    public static final String HISTORY_BUNDLE_KEY = "HistoryBundle";

    private static final List<String> EMPTY_LIST = new ArrayList<>(0);
    private static List<String> historyList_ = EMPTY_LIST;

    private static int maxElements_;

    public static List<String> getHistory(Activity activity, ListView historyListView)
    {
        if(activity == null)
        {
            return EMPTY_LIST;
        }

        View view = historyListView.getChildAt(0);
        if(view != null)
        {
            int px = view.getHeight();
            int height = historyListView.getHeight();
            maxElements_ =  height / px;
        }

        if(historyList_.size() == 0)
        {
            SharedPreferences historySettings = activity.getSharedPreferences(HISTORY_PREFS_NAME, 0);
            Map<String, ?> allHistoryEntries = historySettings.getAll();

            for(int i = 0; i < allHistoryEntries.size(); i++)
            {
                if(historyList_.size() <= maxElements_)
                {
                    String historyValue = historySettings.getString(String.valueOf(i), null);
                    if(historyValue != null)
                    {
                        historyList_.add(historyValue);
                    }
                }
            }
        }

        return historyList_;
    }

    public static void pushHistoryItem(String item)
    {
        if(!historyList_.contains(item))
        {
            if(historyList_.size() >= maxElements_)
            {
                historyList_.remove(historyList_.size() - 1);
                historyList_.add(0, item);
            }
            else
            {
                historyList_.add(0, item);
            }
        }
    }

    public static void saveHistoryItems(Activity activity)
    {
        SharedPreferences settings = activity.getSharedPreferences(HISTORY_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        for(int i = 0; i < historyList_.size(); i++)
        {
            editor.putString(String.valueOf(i), historyList_.get(i));
        }

        // Commit the edits!
        editor.commit();
    }
}
