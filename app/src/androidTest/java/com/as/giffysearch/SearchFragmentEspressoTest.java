package com.as.giffysearch;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.action.ViewActions;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.as.giffysearch.Adapters.SearchResultRecyclerViewAdapter;
import com.as.giffysearch.Controllers.PagingController;
import com.as.giffysearch.Fragments.SearchFragment;
import com.as.giffysearch.ThirdParty.Giphy.GiphyAPI;

import static junit.framework.Assert.fail;

/**
 * Created by Andrejs Skorinko on 12/8/2017.
 *
 */

@RunWith(Parameterized.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchFragmentEspressoTest
{
    private static final String LOG_TAG = SearchFragmentEspressoTest.class.getSimpleName();

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[1][0]);
    }

    public SearchFragmentEspressoTest() {
    }

    private SearchFragment searchFragment_;

    // Max request's time for sending and receiving GIFs from GIPHY
    private static int MAX_REQUEST_TIME = 800;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception
    {
        //Before Test case execution
        if(BuildConfig.BUILD_TYPE.equals("debug"))
        {
            MAX_REQUEST_TIME = 3000;
        }
        else if(BuildConfig.BUILD_TYPE.equals("releaseWithTest"))
        {
            MAX_REQUEST_TIME = 2000;
        }
    }

    @Test
    public void test1SearchFragment()
    {
        onView(withId(R.id.first_search_view)).perform(ViewActions.click());

        searchGif("pigtail");
        searchGif("diggy");
    }

    @Test
    public void test2ManuallyFailedResponseSearchFragment()
    {
        onView(withId(R.id.first_search_view)).perform(ViewActions.click());

        GiphyAPI.INSTANCE.setTesting(true);

        searchGif("pigtail");
        searchGif("diggy");
    }

    private void scrollUpRecyclerView(SearchResultRecyclerViewAdapter adapter)
    {
        int halfSize = adapter.getItemCount() / 2 - 4;
        for(int i = halfSize; i < adapter.getItemCount(); i++)
        {
            onView(withId(R.id.search_result_recycler_view)).perform(RecyclerViewActions.scrollToPosition(i));
        }
    }

    private void scrollDownRecyclerView(SearchResultRecyclerViewAdapter adapter)
    {
        int halfSize = adapter.getItemCount() / 2 + 4;
        for(int i = halfSize; i >= 0; i--)
        {
            onView(withId(R.id.search_result_recycler_view)).perform(RecyclerViewActions.scrollToPosition(i));
        }
    }

    private void searchGif(String name)
    {
        onView(withId(R.id.search_bar_edit_view))
                .perform(ViewActions.clearText());

        onView(withId(R.id.search_bar_edit_view))
                .perform(ViewActions.typeText(name));

        onView(withId(R.id.search_bar_edit_view))
                .perform(pressImeActionButton())
                .check(matches(withText(getSearchBarQuery())));

        searchFragment_ = getSearchFragment();
        if(searchFragment_ != null)
        {
            PagingController paggingController = searchFragment_.getPaggingController();

            boolean isLoading = true;
            while(isLoading)
            {
                isLoading = paggingController.isLoading();
            }

            long requestTime = paggingController.getLastRequestTime();
            /*if(requestTime < MAX_REQUEST_TIME)
            {
                Debugging.logClass(Log.INFO, LOG_TAG, "Can search for " + MAX_REQUEST_TIME +
                                                               " ms GIFs from GIPHY service ! " +
                                                               " Current requestTime: " + requestTime);
            }
            else
            {
                fail("Couldn't search for " + MAX_REQUEST_TIME +
                     " ms GIFs from GIPHY service ! " +
                     " Current requestTime: " + requestTime);
            } */

            SearchResultRecyclerViewAdapter adapter = searchFragment_.getSearchResultRecyclerViewAdapter();

            boolean isReachEnd = false;
            while(!isReachEnd)
            {
                scrollUpRecyclerView(adapter);
                isReachEnd = paggingController.isReachedEnd();
                checkIsResponseFailed();
            }

            onView(withId(R.id.search_result_recycler_view)).perform(RecyclerViewActions.scrollToPosition(adapter.getItemCount() - 1));

            boolean isReachTop = false;
            while(!isReachTop)
            {
                scrollDownRecyclerView(adapter);
                isReachTop = paggingController.isReachedTop();
                checkIsResponseFailed();
            }
/*
            isReachEnd = false;
            while(!isReachEnd)
            {
                scrollUpRecyclerView(adapter);
                isReachEnd = paggingController.isReachedEnd();
                checkIsResponseFailed();
            }*/
        }
        else
        {
            fail("Couldn't find SearchFragment!");
        }
    }

    @After
    public void tearDown() throws Exception
    {
        //After Test case Execution
    }

    private SearchFragment getSearchFragment()
    {
        MainActivity activity = (MainActivity)UtilsEspressoTest.getActivityInstance();
        return activity.getSearchFragment();
    }

    private String getSearchBarQuery()
    {
        MainActivity activity = (MainActivity)UtilsEspressoTest.getActivityInstance();
        SearchFragment searchFragment = activity.getSearchFragment();
        if(searchFragment != null)
        {
            return searchFragment.getSearchQuery();
        }
        return "";
    }

    private void checkIsResponseFailed()
    {
        try
        {
            // View is in hierarchy
            onView(withId(R.id.try_again_button)).perform(ViewActions.click());
        }
        catch (NoMatchingViewException e)
        {
            // View is not in hierarchy
        }
    }
}
