package com.as.giffysearch.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.as.giffysearch.R;
import com.as.giffysearch.Utility.ActivityHelper;

/**
 * Created by Andrejs Skorinko on 12/9/2017.
 *
 */

public class DisabledNetworkFragment extends Fragment
{
    public static final String FRAGMENT_TAG = DisabledNetworkFragment.class.getSimpleName();
    protected static final String LOG_TAG = FRAGMENT_TAG;

    private View disabledNetworkFragment_;
    private Button retryButton_;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        disabledNetworkFragment_ = inflater.inflate(R.layout.fragment_disabled_network, container, false);
        initRetryButton();
        return disabledNetworkFragment_;
    }

    private void initRetryButton()
    {
        retryButton_ = (Button)disabledNetworkFragment_.findViewById(R.id.retry_button);
        retryButton_.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                boolean isNetworkAvailable = ActivityHelper.isNetworkAvailable(getActivity());
                if(isNetworkAvailable)
                {
                    Toast.makeText(getActivity().getApplicationContext(), "Can start search !", Toast.LENGTH_LONG).show();

                    getFragmentManager().beginTransaction().remove(DisabledNetworkFragment.this).commit();
                }
            }
        });
    }
}
