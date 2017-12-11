package com.as.giffysearch;

import android.app.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import android.view.Menu;
import android.view.WindowManager;

import com.as.giffysearch.Fragments.MainFragment;
import com.as.giffysearch.Fragments.SearchFragment;
import com.as.giffysearch.Controllers.HistoryController;

/**
 * Created by Andrejs Skorinko on 11/27/2017.
 *
 */

public class MainActivity extends Activity
{
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private MainFragment mainFragment_;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initMainView();
        initFragments();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
       return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        HistoryController.saveHistoryItems(this);
    }

    private void initMainView()
    {
        setContentView(R.layout.main_activity_layout);

        // Solve the problem, when keyboard is hiding and background screen is taking time to adjust
        // (e.x. keyboard shows black area behind it and slowly disappear)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void initFragments()
    {
        mainFragment_ = new MainFragment();
        getFragmentManager().beginTransaction().add(R.id.main_activity_frame_layout, mainFragment_, MainFragment.FRAGMENT_TAG).commit();
    }

    public MainFragment getMainFragment()
    {
        return mainFragment_;
    }

    public SearchFragment getSearchFragment()
    {
        return (SearchFragment)getFragmentManager().findFragmentByTag(SearchFragment.FRAGMENT_TAG);
    }
}
