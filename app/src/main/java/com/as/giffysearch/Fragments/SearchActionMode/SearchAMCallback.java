package com.as.giffysearch.Fragments.SearchActionMode;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.as.giffysearch.MainActivity;
import com.as.giffysearch.R;
import com.as.giffysearch.Utility.Debugging;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Andrejs Skorinko on 11/27/2017.
 *
 */

public class SearchAMCallback implements ActionMode.Callback
{
    public interface ISearchAMCallbackOwner
    {
        void handleInputAM(String text);
        void destroyAM();
    }

    private ISearchAMCallbackOwner actionModeOwner_;

    // Views
    private EditText searchAMView_;

    public SearchAMCallback(@NotNull ISearchAMCallbackOwner actionModeOwner)
    {
        if(actionModeOwner != null)
        {
            // Who is responsible for created action mode callback ? (for e.x. SearchFragment)
            actionModeOwner_ = actionModeOwner;
        }
        else
        {
            Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "SearchAMCallback constructor can't take null actionModeOwner !");
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, final Menu menu)
    {
        mode.getMenuInflater().inflate(R.menu.search_action_mode_menu, menu);
        RelativeLayout searchAMLayout = (RelativeLayout)menu.findItem(R.id.search_bar_menu_item).
                getActionView();
        searchAMView_ = (EditText) searchAMLayout.findViewById(R.id.search_bar_edit_view);

        searchAMView_.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count)
            {
                Debugging.logClass(Log.DEBUG, MainActivity.LOG_TAG, s.toString());
                int length = s.length();
                if(length > 0)
                {
                    searchAMView_.setCompoundDrawables(null, null, null, null);
                }
                else if (length == 0)
                {
                    searchAMView_.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search, 0, 0, 0);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // search here
            }
        });

        searchAMView_.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                boolean result = false;
                if((actionModeOwner_ == null) || (searchAMView_ == null))
                {
                    Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "actionModeOwner_ || searchAMView_ is null. Can't call onKey() !");
                }
                else
                {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                    {
                        actionModeOwner_.handleInputAM(searchAMView_.getText().toString());
                        searchAMView_.clearFocus();
                        result = true;
                    }
                }

                return result;
            }
        });

        searchAMView_.requestFocus();

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        Debugging.logClass(Log.DEBUG, MainActivity.LOG_TAG, "onPrepareActionMode");
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        Debugging.logClass(Log.DEBUG, MainActivity.LOG_TAG, "onActionItemClicked");
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        if(actionModeOwner_ == null)
        {
            Debugging.logClass(Log.ERROR, MainActivity.LOG_TAG, "actionModeOwner_ is null. Can't call DestroyActionMode() !");
        }
        else
        {
            actionModeOwner_.destroyAM();
        }
    }
}

