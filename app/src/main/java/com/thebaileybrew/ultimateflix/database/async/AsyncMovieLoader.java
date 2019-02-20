package com.thebaileybrew.ultimateflix.database.async;

import android.os.AsyncTask;
import android.util.Log;

import com.thebaileybrew.ultimateflix.BuildConfig;
import com.thebaileybrew.ultimateflix.models.Movie;
import com.thebaileybrew.ultimateflix.utils.UrlUtils;
import com.thebaileybrew.ultimateflix.utils.jsonUtils;

import java.net.URL;
import java.util.List;

public class AsyncMovieLoader extends AsyncTask<String, Void, List<Movie>> {
    private final String ASYNCTAG = AsyncMovieLoader.class.getCanonicalName();

    public AsyncMovieLoader() {
    }

    @Override
    protected List<Movie> doInBackground(String... strings) {
        if (strings.length < 3 || strings[0] == null) {
            return null;
        }
        String sortingOrder = strings[0];
        Log.e(ASYNCTAG, "doInBackground: sortOrder" + sortingOrder );
        if (sortingOrder.equals("favorite")) {
            //TODO: Get films that have a DB value of FAVORITE

        } else if (sortingOrder.equals("watchlist")) {
            //TODO: Get films that have a DB value of WATCHLIST

        }
        String languageFilter = strings[1];
        Log.e(ASYNCTAG, "doInBackground: language" + languageFilter);
        String filterYear = strings[2];
        Log.e(ASYNCTAG, "doInBackground: year" + filterYear);
        String searchQuery = strings[3];
        Log.e(ASYNCTAG, "doInBackground: searching" + searchQuery);
        URL moviesRequestUrl = UrlUtils.buildMovieUrl(
                BuildConfig.API_KEY,
                languageFilter,
                sortingOrder,
                filterYear,
                searchQuery);
        try {
            String jsonMoviesResponse = jsonUtils.makeHttpsRequest(moviesRequestUrl);

            return jsonUtils.extractMoviesFromJson(jsonMoviesResponse);
        } catch (Exception e) {
            Log.e(ASYNCTAG, "doInBackground: can't make http req", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Movie> movies) {
        super.onPostExecute(movies);

    }

}
