package com.as.giffysearch.Fragments;

import android.app.Fragment;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import android.util.Log;

import com.as.giffysearch.Adapters.HistoryViewAdapter;
import com.as.giffysearch.Controllers.HistoryController;
import com.as.giffysearch.R;
import com.as.giffysearch.Utility.Debugging;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrejs Skorinko on 11/28/2017.
 *
 */

public class MainFragment extends Fragment
{
    public static final String FRAGMENT_TAG = MainFragment.class.getSimpleName();
    protected static final String LOG_TAG = FRAGMENT_TAG;

    // Views
    private View mainFragmentView_;
    private SearchView searchView_;

    // History list
    private ListView historyListView_;
    private HistoryViewAdapter historyViewAdapter_;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        mainFragmentView_ = inflater.inflate(R.layout.fragment_main_layout, container, false);
        if(mainFragmentView_ == null)
        {
            Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to inflate layout by id:main_fragment_layout !");
        }
        else
        {
            // Initialize search bar from main_layout.xml
            initSearchView();
            initHistoryListView();
        }

        return mainFragmentView_;
    }

    @Override
    public void onStart()
    {
        super.onStart();
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
                            //ActivityHelper.hideKeyboard(getContext(), )
                            startSearchFragment(null);
                        }
                    }
                });
            }

            searchView_.setQuery("", false);
            mainFragmentView_.requestFocus();
        }
    }

    private void startSearchFragment(Bundle historyBundle)
    {
        SearchFragment searchFragment = new SearchFragment();
        if(historyBundle != null)
        {
            searchFragment.setArguments(historyBundle);
        }

        getFragmentManager().beginTransaction().replace(R.id.main_activity_frame_layout, searchFragment,
                SearchFragment.FRAGMENT_TAG).commit();
    }

    void initHistoryListView()
    {
        historyListView_ = (ListView)mainFragmentView_.findViewById(R.id.history_list_view);

        List<String> firstElement = new ArrayList<String>();
        firstElement.add("CheckSize");
        HistoryViewAdapter checkSizeAdapter = new HistoryViewAdapter(getActivity().getApplicationContext(), firstElement);
        historyListView_.setAdapter(checkSizeAdapter);

        ViewTreeObserver viewTreeObserver = historyListView_.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                List<String> historyListItems = HistoryController.getHistory(getActivity(), historyListView_);

                historyViewAdapter_ = new HistoryViewAdapter(getActivity().getApplicationContext(), historyListItems);
                historyListView_.setAdapter(historyViewAdapter_);
                historyViewAdapter_.notifyDataSetChanged();

                ViewTreeObserver viewTreeObserver = historyListView_.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                {
                    viewTreeObserver.removeOnGlobalLayoutListener(this);
                }
                else
                {
                    viewTreeObserver.removeGlobalOnLayoutListener(this);
                }
            }

        });

        historyListView_.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String historyValue = parent.getItemAtPosition(position).toString();
                if(!historyValue.isEmpty())
                {
                    Bundle historyBundle = new Bundle();
                    historyBundle.putString(HistoryController.HISTORY_BUNDLE_KEY, historyValue);
                    startSearchFragment(historyBundle);
                }
            }
        });
    }


}
