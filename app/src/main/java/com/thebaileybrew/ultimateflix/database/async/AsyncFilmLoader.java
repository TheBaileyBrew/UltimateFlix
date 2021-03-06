package com.thebaileybrew.ultimateflix.database.async;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.thebaileybrew.ultimateflix.BuildConfig;
import com.thebaileybrew.ultimateflix.R;
import com.thebaileybrew.ultimateflix.models.Film;
import com.thebaileybrew.ultimateflix.utils.UrlUtils;
import com.thebaileybrew.ultimateflix.utils.jsonUtils;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static com.thebaileybrew.ultimateflix.database.ConstantUtils.TIME_FORMAT;

public class AsyncFilmLoader extends AsyncTask<String, Void, List<Film>> {
    private static final String TAG = AsyncFilmLoader.class.getSimpleName();

    private final TextView movieTag;
    private final TextView movieGenre;
    private final TextView movieRuntime;
    private final TextView movieLanguage;
    private final TextView movieBudget;
    private final TextView movieRevenue;

    public AsyncFilmLoader(TextView movieTag, TextView movieGenre, TextView movieRuntime, TextView movieLanguage, TextView movieRevenue, TextView movieBudget) {
        this.movieTag = movieTag;
        this.movieGenre = movieGenre;
        this.movieRuntime = movieRuntime;
        this.movieLanguage = movieLanguage;
        this.movieRevenue = movieRevenue;
        this.movieBudget = movieBudget;
    }

    @Override
    protected List<Film> doInBackground(String... strings) {
        if (strings.length <1 || strings[0] == null) {
            Log.e(TAG, "doInBackground: credits null");
            return null;
        }

        String movieID = strings[0];

        URL filmRequest = UrlUtils.buildSingleMovieUrl(BuildConfig.API_KEY, movieID);
        try {
            String jsonFilmResponse = jsonUtils.requestHttpsSingleFilm(filmRequest);
            return jsonUtils.extractSingleFilmData(jsonFilmResponse);
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: failed to request film details", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Film> film) {
        if (film != null) {
            Film currentFilm = film.get(0);
            movieTag.setText(currentFilm.getMovieTagLine());
            movieGenre.setText(currentFilm.getMovieGenre());
            if (currentFilm.getMovieRuntime() == 0) {
                movieRuntime.setText(R.string.unknown_time);
            } else {
                movieRuntime.setText(convertTime(currentFilm.getMovieRuntime()));
            }
            movieLanguage.setText(currentFilm.getMovieLanguage());
            movieBudget.setText(convertDollars(currentFilm.getMovieBudget()));
            movieRevenue.setText(convertDollars(currentFilm.getMovieRevenue()));
        }

        super.onPostExecute(film);
    }

    private String convertDollars(int movieCurrency) {
        NumberFormat formatter = new DecimalFormat("#,###");
        return "$ " + formatter.format((double) movieCurrency);
    }

    private String convertTime(int runTime) {
        int hours = runTime / 60;
        int minutes = runTime % 60;
        return String.format(Locale.US, TIME_FORMAT, hours, minutes);
    }
}
