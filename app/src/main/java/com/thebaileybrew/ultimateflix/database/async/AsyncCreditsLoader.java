package com.thebaileybrew.ultimateflix.database.async;

import android.os.AsyncTask;
import android.util.Log;

import com.thebaileybrew.ultimateflix.BuildConfig;
import com.thebaileybrew.ultimateflix.models.Credit;
import com.thebaileybrew.ultimateflix.utils.UrlUtils;
import com.thebaileybrew.ultimateflix.utils.jsonUtils;

import java.net.URL;
import java.util.List;

public class AsyncCreditsLoader extends AsyncTask<String, Void, List<Credit>> {
    private static final String TAG = AsyncCreditsLoader.class.getSimpleName();

    public AsyncCreditsLoader() {}

    @Override
    protected List<Credit> doInBackground(String... strings) {
        if (strings.length <1 || strings[0] == null) {
            Log.e(TAG, "doInBackground: credits null");
            return null;
        }

        String movieID = strings[0];

        URL creditRequest = UrlUtils.buildCreditsMovieUrl(BuildConfig.API_KEY, movieID);
        try {
            String jsonCreditResponse = jsonUtils.requestHttpsMovieCredits(creditRequest);
            return jsonUtils.extractCreditDetails(jsonCreditResponse);
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: failed to request credits", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Credit> credits) {
        super.onPostExecute(credits);
    }
}
