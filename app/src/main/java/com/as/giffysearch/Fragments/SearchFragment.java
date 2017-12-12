package com.as.giffysearch.Fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.view.ActionMode;

import android.util.Log;
import android.widget.Toast;

import com.as.giffysearch.Controllers.HistoryController;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import com.as.giffysearch.Listeners.EndlessRecyclerViewScrollListener;
import com.as.giffysearch.Controllers.PagingController;
import com.as.giffysearch.ActionModes.SearchAMCallback;

import com.as.giffysearch.Adapters.SearchResultRecyclerViewAdapter;
import com.as.giffysearch.Models.JSON.Giphy.SearchResult;
import com.as.giffysearch.Models.JSON.Giphy.GifResult;
import com.as.giffysearch.R;

import com.as.giffysearch.ThirdParty.Giphy.GiphyAPI;
import com.as.giffysearch.ThirdParty.Glide.GlideApp;

import com.as.giffysearch.Utility.ActivityHelper;
import com.as.giffysearch.Utility.Debugging;


/**
 * Created by Andrejs Skorinko on 11/28/2017.
 *
 */

public class SearchFragment extends Fragment implements SearchAMCallback.ISearchAMCallbackObserver,
                                                        GiphyAPI.ICallObserver
{
    public static final String FRAGMENT_TAG = SearchFragment.class.getSimpleName();
    protected static final String LOG_TAG = FRAGMENT_TAG;

    // For caching
    private FragmentManager fragmentManager_;
    private Context applicationContext_;

    // Views
    private View searchFragmentView_;

    // ActionMode
    private ActionMode actionMode_;
    private ActionMode.Callback aMCallback_;

    // Typed in the search bar (Action Mode)
    private String searchInput_ = "";

    // RecyclerView's data
    private RecyclerView searchResultRecyclerView_;
    private SearchResultRecyclerViewAdapter searchResultRecyclerViewAdapter_;
    private ViewPreloadSizeProvider<GifResult> preloadSizeProvider_;

    // Class helps get GIFS using GIPHY service through OkHttpAPI
    private GiphyAPI giphyAPI_ = GiphyAPI.INSTANCE;

    // Pagging
    private PagingController pagingController_;

    // Failed response from GIPHY API
    private boolean isFailedResponse_ = false;

    public SearchFragment()
    {
        giphyAPI_.addCallObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        searchFragmentView_ = inflater.inflate(R.layout.fragment_search_layout, container, false);
        if(searchFragmentView_ == null)
        {
            Debugging.logClass(Log.ERROR, LOG_TAG, "Failed to inflate layout by id:search_fragment_layout !");
        }
        else
        {
            initRefsForCaching();
            initPaggingController();
            initSearchActionMode();
            initSearchResultRecyclerView();
        }

        return searchFragmentView_;
    }

    private void initRefsForCaching()
    {
        fragmentManager_ = getFragmentManager();
        applicationContext_ = getActivity().getApplicationContext();
    }

    private void initPaggingController()
    {
        pagingController_ = new PagingController(applicationContext_, GiphyAPI.DEFAULT_LIMIT, GiphyAPI.DEFAULT_OFFSET);
    }

    // ------- Search Bar Action Mode -------
    private void initSearchActionMode()
    {
        if(actionMode_ == null)
        {
            Bundle historyBundle = getArguments();
            String historyValue = "";
            if(historyBundle != null)
            {
                historyValue = historyBundle.getString(HistoryController.HISTORY_BUNDLE_KEY);

            }

            aMCallback_ = new SearchAMCallback(applicationContext_, this, historyValue);
            actionMode_ = getActivity().startActionMode(aMCallback_);
        }
    }

    @Override
    public void handleInputAM(String text)
    {
        // When user is entered input through Search Bar, need to manually hide keyboard after
        ActivityHelper.hideKeyboard(applicationContext_, getView());

        boolean isNetworkAvailable = checkNetworkConnection();
        if(isNetworkAvailable)
        {
            if(!searchInput_.equals(text))
            {
                searchInput_ = text;
                HistoryController.pushHistoryItem(searchInput_);

                resetSearch();
                showStartLoading();

                pagingController_.startSearch(searchInput_);
            }
        }
    }

    private void resetSearch()
    {
        pagingController_.clear();
        searchResultRecyclerViewAdapter_.updateData();
    }

    private void showStartLoading()
    {
        pagingController_.setScrollDirection(PagingController.ScrollDirection.IN_PLACE);
        pagingController_.setLoading(true);
        searchResultRecyclerViewAdapter_.addLoader();
    }

    private void hideLoading()
    {
        searchResultRecyclerViewAdapter_.removeLoader();
    }

    @Override
    public void destroyAM()
    {
        actionMode_ = null;
        if(fragmentManager_.findFragmentByTag(SearchFragment.FRAGMENT_TAG) != null)
        {
            fragmentManager_.beginTransaction().replace(R.id.main_activity_frame_layout, new MainFragment()).commit();
        }
    }

    // ------- 2. Results are displayed in RecyclerView -------
    private void initSearchResultRecyclerView()
    {
        searchResultRecyclerView_ = (RecyclerView)searchFragmentView_.findViewById(R.id.search_result_recycler_view);
        searchResultRecyclerView_.setHasFixedSize(true);
        RecyclerView.ItemAnimator animator = searchResultRecyclerView_.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
            ((SimpleItemAnimator) animator).setChangeDuration(0);
        }

        searchResultRecyclerView_.addItemDecoration(getItemDecoration());
        searchResultRecyclerView_.setLayoutManager(new LinearLayoutManager(applicationContext_));
        searchResultRecyclerView_.setAdapter(getAdapter());
        searchResultRecyclerView_.addOnScrollListener(getGIFPreloaderScrollListener());
        searchResultRecyclerView_.addOnScrollListener(getEndlessRecyclerViewScrollListener());

        searchResultRecyclerView_.setRecyclerListener(new RecyclerView.RecyclerListener()
        {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder holder)
            {
                // This is an optimization to reduce the memory usage of RecyclerView's recycled view pool
                // and good practice when using Glide with RecyclerView.
                if(holder instanceof SearchResultRecyclerViewAdapter.GifViewHolder)
                {
                    SearchResultRecyclerViewAdapter.GifViewHolder gifHolder = (SearchResultRecyclerViewAdapter.GifViewHolder)holder;
                    GlideApp.with(applicationContext_).clear(gifHolder.getImageView());
                }
            }
        });
    }

    private RecyclerView.ItemDecoration getItemDecoration()
    {
        int bigSpaceInPixels = getResources().getDimensionPixelSize(R.dimen.big_space);
        return new SearchResultRecyclerViewAdapter.SpacesItemDecoration(bigSpaceInPixels);
    }

    private RecyclerView.Adapter getAdapter()
    {
        RequestBuilder<Drawable> gifItemRequest = GlideApp.with(applicationContext_).asDrawable();
        preloadSizeProvider_ = new ViewPreloadSizeProvider<>();

        searchResultRecyclerViewAdapter_ = new SearchResultRecyclerViewAdapter(
                getActivity(),
                gifItemRequest,
                preloadSizeProvider_,
                pagingController_,
                pagingController_.getData(),
                new SearchResultRecyclerViewAdapter.FailedResponseViewHolder.ClickListener()
                {
                    @Override public void onPositionClicked(int position)
                    {
                        // callback performed on click
                        pagingController_.restartSearch();
                    }
                });

        searchResultRecyclerViewAdapter_.setHasStableIds(true);
        return searchResultRecyclerViewAdapter_;
    }

    private RecyclerView.OnScrollListener getGIFPreloaderScrollListener()
    {
        return new RecyclerViewPreloader<>(
                GlideApp.with(applicationContext_),
                searchResultRecyclerViewAdapter_,
                preloadSizeProvider_,
                4);
    }

    private RecyclerView.OnScrollListener getEndlessRecyclerViewScrollListener()
    {
        return new EndlessRecyclerViewScrollListener((LinearLayoutManager) searchResultRecyclerView_.getLayoutManager(), pagingController_)
        {
            @Override
            public int getFooterViewType(int defaultNoFooterViewType)
            {
                return SearchResultRecyclerViewAdapter.VIEW_TYPE_LOADING;
            }

            @Override
            public void onLoadMore(final int page,
                                   final PagingController.ScrollDirection scrollDirection)
            {
                if(!pagingController_.isLoading() &&
                   checkNetworkConnection() &&
                   !isFailedResponse_)
                {

                    pagingController_.setCurrentPage(page);
                    pagingController_.setScrollDirection(scrollDirection);
                    pagingController_.setLoading(true);

                    searchResultRecyclerView_.post(new Runnable()
                    {
                        public void run()
                        {
                            searchResultRecyclerViewAdapter_.addLoader();
                            pagingController_.continueSearch(
                                    page * GiphyAPI.DEFAULT_LIMIT);
                        }
                    });
                }
            }
        };
    }

    public void onSearchComplete(final SearchResult result)
    {
        // 1. If previous was failed response, then delete failed response
        // Because now we have success response
        if(isFailedResponse_)
        {
            Toast.makeText(applicationContext_, "Can continue search !", Toast.LENGTH_LONG).show();
            hideFailedResponse();
        }

        pagingController_.setSearchResult(result, true);
        postSearch(true);
    }

    private void hideFailedResponse()
    {
        isFailedResponse_ = false;
        pagingController_.setIsFailedResponse(false);
        searchResultRecyclerViewAdapter_.removeFailedResponse();
    }

    public void onSearchFailed()
    {
        Toast.makeText(applicationContext_, "Response failed. Can't continue search !", Toast.LENGTH_LONG).show();
        pagingController_.setSearchResult(null, false);
        postSearch(false);
    }

    private void postSearch(boolean isSuccessful)
    {
        if(pagingController_.isLoading())
        {
            hideLoading();
        }

        if(isSuccessful)
        {
            // 2. Update recycler view data after successful setSearchResult
            searchResultRecyclerViewAdapter_.updateData();
        }
        else
        {
            // 1. If previous were not failed responses, then
            // Create new failed response
            if(!isFailedResponse_)
            {
                showFailedResponse();
            }
        }

        // 3. Scroll to the half of the recycle view's list if user scrolls UP or DOWN
        scrollToCenter();

        pagingController_.setLoading(false);
    }

    private void showFailedResponse()
    {
        searchResultRecyclerViewAdapter_.addFailedResponse();
        isFailedResponse_ = true;
        pagingController_.setIsFailedResponse(true);
    }

    private void scrollToCenter()
    {
        PagingController.ScrollDirection scrollDirection = pagingController_.getScrollDirection();
        int size = pagingController_.getData().size();
        int currentPage = pagingController_.getCurrentPage();
        if (currentPage > PagingController.MAX_LOADED_PAGE && size > GiphyAPI.DEFAULT_LIMIT && !isFailedResponse_)
        {
            if (scrollDirection == PagingController.ScrollDirection.UP)
            {
                searchResultRecyclerView_.getLayoutManager().scrollToPosition((size / 2) - 4);
            }
            else if (scrollDirection == PagingController.ScrollDirection.DOWN)
            {
                searchResultRecyclerView_.getLayoutManager().scrollToPosition((size / 2) + 4);
            }

            pagingController_.resetScrollDirection();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        giphyAPI_.removeCallObserver(this);

        resetSearch();

        searchResultRecyclerView_.clearOnScrollListeners();
        searchInput_ = "";

        destroyDisabledNetworkFragment();

        pagingController_.logAverageRequestTime();
    }

    private boolean checkNetworkConnection()
    {
        boolean isNetworkAvailable_ = false;

        Fragment disabledNetworkFragment = fragmentManager_.findFragmentByTag(DisabledNetworkFragment.FRAGMENT_TAG);
        if(disabledNetworkFragment == null)
        {
            isNetworkAvailable_ = ActivityHelper.isNetworkAvailable(applicationContext_);
            if(!isNetworkAvailable_)
            {
                fragmentManager_.beginTransaction().add
                        (R.id.disabled_network_frame_layout,
                                new DisabledNetworkFragment(),
                                DisabledNetworkFragment.FRAGMENT_TAG)
                        .commit();
            }
        }

        return isNetworkAvailable_;
    }

    private void destroyDisabledNetworkFragment()
    {
        Fragment disabledNetworkFragment = fragmentManager_.findFragmentByTag(DisabledNetworkFragment.FRAGMENT_TAG);
        if(disabledNetworkFragment != null)
        {
            fragmentManager_.beginTransaction().remove(disabledNetworkFragment).commit();
        }

        isFailedResponse_ = false;
    }

    public String getSearchQuery()
    {
        return searchInput_;
    }

    public PagingController getPaggingController()
    {
        return pagingController_;
    }

    public SearchResultRecyclerViewAdapter getSearchResultRecyclerViewAdapter()
    {
        return searchResultRecyclerViewAdapter_;
    }

}
