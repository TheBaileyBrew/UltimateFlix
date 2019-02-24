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
        if (strings.length < 1 || strings[0] == null) {
            return null;
        }
        String pageNumber = strings[0];
        URL moviesRequestUrl = UrlUtils.buildMovieUrl(
                BuildConfig.API_KEY,
                pageNumber);
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
