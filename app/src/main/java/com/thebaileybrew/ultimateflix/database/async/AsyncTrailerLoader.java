package com.thebaileybrew.ultimateflix.database.async;

import android.os.AsyncTask;
import android.util.Log;

import com.thebaileybrew.ultimateflix.BuildConfig;
import com.thebaileybrew.ultimateflix.adapters.VideosAdapter;
import com.thebaileybrew.ultimateflix.models.Videos;
import com.thebaileybrew.ultimateflix.utils.UrlUtils;
import com.thebaileybrew.ultimateflix.utils.jsonUtils;

import java.net.URL;
import java.util.List;

public class AsyncTrailerLoader extends AsyncTask<String, Void, List<Videos>> {
    private static final String TAG = AsyncTrailerLoader.class.getSimpleName();

    private final VideosAdapter videosAdapter;
    public AsyncTrailerLoader(VideosAdapter videosAdapter) {this.videosAdapter = videosAdapter;}

    @Override
    protected List<Videos> doInBackground(String... strings) {
        if (strings.length < 1 || strings[0] == null) {
            Log.e(TAG, "doInBackground: strings length = " + strings.length );
            return null;
        }
        String movieID = strings[0];
        Log.e(TAG, "doInBackground: movieID" + movieID);

        URL videoRequestUrl = UrlUtils.buildVideoUrl(BuildConfig.API_KEY, movieID);
        try {
            String jsonVideoResponse = jsonUtils.requestHttpsMovieVideos(videoRequestUrl);
            return jsonUtils.extractVideosDetails(jsonVideoResponse);
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: can't request videos", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Videos> videos) {
        if (videos != null) {
            videosAdapter.setVideoCollection(videos);
        }
        super.onPostExecute(videos);
    }
}
