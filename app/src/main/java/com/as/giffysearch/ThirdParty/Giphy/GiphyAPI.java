package com.as.giffysearch.ThirdParty.Giphy;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.as.giffysearch.Models.JSON.Giphy.SearchResult;
import com.as.giffysearch.Utility.Debugging;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Andrejs Skorinko on 12/3/2017.
 *
 */

public final class GiphyAPI
{
    // Not lazy singleton in Java
    // (sources)
    // 1. https://habrahabr.ru/post/129494/
    // 2. https://www.journaldev.com/171/thread-safety-in-java-singleton-classes-with-example-code
    public static final GiphyAPI INSTANCE = new GiphyAPI();

    // Giphy data for Endpoints
    private static final String INVALID_URL = "INVALID_URL";
    private static final String BETA_API_KEY = "OM9CMcBOHA37f4WtCU6pdBjJA0naaFFc";
    private static final String PRODUCTION_API_KEY = "NOT_CREATED_YET";

    // Set Search Endpoint
    private static final String HOST = "http://api.giphy.com/";
    private static final String SEARCHING_PATH = "v1/gifs/search";
    private static final String API_KEY_PATH = "?api_key=";
    private static final String LIMIT_PATH = "&limit=";
    private static final String OFFSET_PATH = "&offset=";
    private static final String QUERY_PATH = "&q=";

    public static final int DEFAULT_LIMIT = 9;
    public static final int DEFAULT_OFFSET = 0;

    private StringBuilder searchURLBuilder_ = new StringBuilder();

    // OkHttpApi (subject) notify all observers about particular event
    public interface ICallObserver
    {
        void onSearchComplete(SearchResult result);
        void onSearchFailed();
    }
    private OkHttpAPI okHttpAPI_ = OkHttpAPI.INSTANCE;

    private GiphyAPI()
    {
        // Singleton
    }

    public void startSearch(String query, int limit, int offset)
    {
        String searchingUrl = getSearchingUrl(query, limit, offset);
        query(searchingUrl);
    }

    private void query(final String apiUrl)
    {
        Request request = new Request.Builder()
                                     .url(apiUrl)
                                     .build();
        okHttpAPI_.makeCall(request);
    }

    // e.x. http://api.giphy.com/v1/gifs/search?api_key={YOUR_API_KEY}&limit=25&offset=0&q=funny+cat
    // host = ttp://api.giphy.com/
    // search_path = v1/gifs/search?
    // api_key = {YOUR_API_KEY}
    // limit = 25
    // offset = 0
    // query = funny+cat
    private String getSearchingUrl(String query, int limit, int offset)
    {
        searchURLBuilder_.setLength(0);
        searchURLBuilder_.append(HOST)
                      .append(SEARCHING_PATH)
                      .append(getAPIKey())
                      .append(getLimit(limit))
                      .append(getOffset(offset))
                      .append(getQuery(query));

        return searchURLBuilder_.toString();
    }

    private String getAPIKey()
    {
        return API_KEY_PATH + BETA_API_KEY;
    }

    private String getLimit(int limit)
    {
        return LIMIT_PATH + limit;
    }

    private String getOffset(int offset)
    {
        return OFFSET_PATH + offset;
    }

    private String getQuery(String query)
    {
        return QUERY_PATH + makeGiphyQuery(query);
    }

    private String makeGiphyQuery(String query)
    {
        return query.replace(" ", "+");
    }

    public void addCallObserver(ICallObserver observer)
    {
        okHttpAPI_.addCallObserver(observer);
    }

    public void removeCallObserver(ICallObserver observer)
    {
        okHttpAPI_.removeCallObserver(observer);
    }

    // ----------------------------
    // For ESPRESSO testing (setTesting is called from AndroidTest e.x. SearchFragmentEspressoTest)
    static private boolean IS_TESTING = false;
    public void setTesting(boolean isTesting) { IS_TESTING = isTesting; }
    public boolean isTesting() { return IS_TESTING; }
    // ----------------------------

    // OkHttpApi class helps to send request to GIPHY service,
    // and get result as SearchResult (GIFs)
    private static final class OkHttpAPI
    {
        private static final String LOG_TAG = OkHttpAPI.class.getSimpleName();

        // Observers
        private HashSet<ICallObserver> observers_ = new HashSet<>();

        // OkHttp client
        private OkHttpClient client_ = null;
        private static final OkHttpAPI INSTANCE = new OkHttpAPI();

        private final Handler mainHandler_;

        private int counter_ = 0;

        private OkHttpAPI()
        {
            // Singleton
            client_ = new OkHttpClient();
            mainHandler_ = new Handler(Looper.getMainLooper());
        }

        private void makeCall(Request request)
        {
            Call call = client_.newCall(request);
            call.enqueue(new Callback()
            {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e)
                {
                    Debugging.logClass(Log.ERROR, LOG_TAG, "Request Failure");
                    notifyAllFailedResponse();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
                {
                    try
                    {
                        boolean isSuccessful = response.isSuccessful();

                        if(GiphyAPI.IS_TESTING)
                        {
                            counter_ += 1;
                            if(counter_ % 3 == 0)
                            {
                                isSuccessful = false;
                            }
                        }

                        if (isSuccessful)
                        {
                            ResponseBody responseBody = response.body();
                            if(responseBody != null)
                            {
                                String jsonData = responseBody.string();
                                SearchResult result = new Gson().fromJson(jsonData, SearchResult.class);
                                // Debugging.logClass(Log.INFO, LOG_TAG, "Giphy Gif Data from Response: " + result.toString());

                                if(result.getData().length < 0)
                                {
                                    Debugging.logClass(Log.ERROR, LOG_TAG,
                                            "SearchResult data (response) length " + result.getData().length + " < 0 " );
                                    notifyAllFailedResponse();
                                }
                                else
                                {
                                    final SearchResult finalResult = result;
                                    // Only the original thread that created a view hierarchy can touch its views
                                    mainHandler_.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            // Notify all observers about successful response from GIPHY service
                                            for(ICallObserver observer : observers_)
                                            {
                                                observer.onSearchComplete(finalResult);
                                            }
                                        }
                                    });
                                }
                            }
                            else
                            {
                                Debugging.logClass(Log.ERROR, LOG_TAG, "Response unsuccessful: ResponseBody is null !");
                                notifyAllFailedResponse();
                            }
                        }
                        else
                        {
                            Debugging.logClass(Log.ERROR, LOG_TAG, "Response Unsuccessful !");
                            notifyAllFailedResponse();
                        }
                    }
                    catch (IOException e)
                    {
                        Debugging.logClassThrowable(Log.ERROR, LOG_TAG, "Exception Caught: ", e);
                        notifyAllFailedResponse();

                    }
                    catch (JsonSyntaxException e)
                    {
                        Debugging.logClassThrowable(Log.ERROR, LOG_TAG, "Json Exception Caught: ", e);
                        notifyAllFailedResponse();
                    }

                    closeResponse(response);
                }
            });
        }

        private void notifyAllFailedResponse()
        {
            mainHandler_.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // Notify all observers about failed response from GIPHY service
                    for(ICallObserver observer : observers_)
                    {
                        observer.onSearchFailed();
                    }
                }
            });
        }

        private void closeResponse(Response response)
        {
            ResponseBody body = response.body();
            if(body != null)
            {
                body.close();
            }
        }

        private void addCallObserver(ICallObserver observer)
        {
            observers_.add(observer);
        }

        private void removeCallObserver(ICallObserver observer)
        {
            observers_.remove(observer);
        }
    }
}

