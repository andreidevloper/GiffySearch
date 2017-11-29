package com.as.giffysearch.Fragments;

import android.content.Context;

import android.app.FragmentManager;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.SearchView;

import android.util.Log;

import com.as.giffysearch.R;
import com.as.giffysearch.Utility.Debugging;

/**
 * Created by Andrejs Skorinko on 11/28/2017.
 *
 */

public class MainFragment extends Fragment
{
    public static final String FRAGMENT_TAG = "MainFragment";
    protected static final String LOG_TAG = FRAGMENT_TAG;

    // Views
    private View mainFragmentView_;
    private SearchView searchView_;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        mainFragmentView_ = inflater.inflate(R.layout.main_fragment_layout, container, false);
        if(mainFragmentView_ == null)
        {
            Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to inflate layout by id:main_fragment_layout !");
        }
        else
        {
            // Initialize search bar from main_layout.xml
            initSearchView();
        }

        return mainFragmentView_;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        initSearchView();
    }


    private void initSearchView()
    {
        if(searchView_ == null) // onCreate
        {
            searchView_ = (SearchView)mainFragmentView_.findViewById(R.id.first_search_view);
            if(searchView_ == null)
            {
                Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to find SearchView by id:first_search_view !");
            }
            else
            {
                searchView_.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener()
                {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus)
                    {
                        // If SearchView is selected and user wants to enter input
                        if(hasFocus)
                        {
                            getFragmentManager().beginTransaction().replace(R.id.main_frame_layout, new SearchFragment(),
                                    SearchFragment.FRAGMENT_TAG).commit();
                        }
                    }
                });
            }
        }
        else // onResume
        {
            if(mainFragmentView_ != null)
            {
                searchView_.setQuery("", false);
                mainFragmentView_.requestFocus();
            }
            else
            {
                Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to initialize SearchView during onResume !");
            }
        }
    }
}
