package com.as.giffysearch.Fragments;

import android.content.Context;

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.view.ActionMode;

import android.util.Log;

import com.as.giffysearch.Fragments.SearchActionMode.SearchAMCallback;
import com.as.giffysearch.R;
import com.as.giffysearch.Utility.ActivityHelper;
import com.as.giffysearch.Utility.Debugging;

/**
 * Created by Andrejs Skorinko on 11/28/2017.
 *
 */

public class SearchFragment extends Fragment implements SearchAMCallback.ISearchAMCallbackOwner
{
    public static final String FRAGMENT_TAG = "SearchFragment";
    protected static final String LOG_TAG = FRAGMENT_TAG;

    // Views
    private View searchFragmentView_;

    // ActionMode
    private ActionMode actionMode_;
    private ActionMode.Callback aMCallback_ = new SearchAMCallback(this);

    // Typed in the search view
    private String searchInput_;
    // Selected from the history list view
    private String historyInput_;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        searchFragmentView_ = inflater.inflate(R.layout.search_fragment_layout, container, false);
        if(searchFragmentView_ == null)
        {
            Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to inflate layout by id:search_fragment_layout !");
        }
        else
        {
            initSearchActionMode();
        }

        return searchFragmentView_;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        initSearchActionMode();
    }

    private void initSearchActionMode()
    {
        if(actionMode_ == null)
        {
            actionMode_ = getActivity().startActionMode(aMCallback_);
        }
    }

    @Override
    public void handleInputAM(String text)
    {
        // When user is entered input through Search Bar, need to manually hide keyboard after
        ActivityHelper.hideKeyboard(getActivity(), getView(), 0);

    }

    @Override
    public void destroyAM()
    {
        actionMode_ = null;
        getFragmentManager().beginTransaction().replace(R.id.main_frame_layout, new MainFragment()).commit();
    }
}
