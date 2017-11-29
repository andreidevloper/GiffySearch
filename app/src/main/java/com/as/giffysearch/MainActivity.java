package com.as.giffysearch;

import android.app.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import android.view.WindowManager;

import com.as.giffysearch.Fragments.MainFragment;

/**
 * Created by Andrejs Skorinko on 11/27/2017.
 *
 */

public class MainActivity extends Activity
{
    public static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initMainView();
        initFragments();
    }

    private void initMainView()
    {
        setContentView(R.layout.main_layout);

        // Solve the problem, when keyboard is hiding and background screen is taking time to adjust
        // (e.x. keyboard shows black area behind it and slowly disappear)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void initFragments()
    {
        getFragmentManager().beginTransaction().add(R.id.main_frame_layout, new MainFragment(), MainFragment.FRAGMENT_TAG).commit();
    }
}
