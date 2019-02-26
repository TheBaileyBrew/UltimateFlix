package com.thebaileybrew.ultimateflix.database.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.thebaileybrew.ultimateflix.DetailsActivity;
import com.thebaileybrew.ultimateflix.R;
import com.thebaileybrew.ultimateflix.models.Movie;
import com.thebaileybrew.ultimateflix.utils.UrlUtils;

import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_KEY;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_BACKDROP;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_FAVORITE;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_ID;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_LANGUAGE;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_OVERVIEW;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_POPULARITY;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_POSTER;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_RELEASE;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_TITLE;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_VOTE_AVG;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_VOTE_COUNT;

public class UltimateFlixWidget extends AppWidgetProvider {
    private static final String TAG = UltimateFlixWidget.class.getSimpleName();


    private Movie currentMovie;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int mMovieID = sharedPreferences.getInt(WIDGET_MOVIE_ID, 0);
        int mMovieVoteCount = sharedPreferences.getInt(WIDGET_MOVIE_VOTE_COUNT, 0);
        double mMovieVoteAverage = Double.parseDouble(sharedPreferences.getString(WIDGET_MOVIE_VOTE_AVG, "0"));
        String mMovieTitle = sharedPreferences.getString(WIDGET_MOVIE_TITLE, "MOVIE NAME");
        double mMoviePopularity = Double.parseDouble(sharedPreferences.getString(WIDGET_MOVIE_POPULARITY, "0"));
        String mMovieLanguage = sharedPreferences.getString(WIDGET_MOVIE_LANGUAGE, "LANGUAGE");
        String mMoviePoster = sharedPreferences.getString(WIDGET_MOVIE_POSTER, "POSTER");
        String mMovieBackdrop = sharedPreferences.getString(WIDGET_MOVIE_BACKDROP, "BACKDROP");
        String mMovieOverview = sharedPreferences.getString(WIDGET_MOVIE_OVERVIEW, "OVERVIEW");
        String mMovieReleaseDate = sharedPreferences.getString(WIDGET_MOVIE_RELEASE, "NA");
        int mMovieFavorite = sharedPreferences.getInt(WIDGET_MOVIE_FAVORITE, 0);

        Movie currentMovie = new Movie();
        currentMovie.setMovieID(mMovieID);
        currentMovie.setMovieVoteCount(mMovieVoteCount);
        currentMovie.setMovieVoteAverage(mMovieVoteAverage);
        currentMovie.setMovieTitle(mMovieTitle);
        currentMovie.setMoviePopularity(mMoviePopularity);
        currentMovie.setMovieLanguage(mMovieLanguage);
        currentMovie.setMoviePosterPath(mMoviePoster);
        currentMovie.setMovieBackdrop(mMovieBackdrop);
        currentMovie.setMovieOverview(mMovieOverview);
        currentMovie.setMovieReleaseDate(mMovieReleaseDate);
        currentMovie.setMovieFavorite(mMovieFavorite);

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.ultimate_flix_widget_item);
            remoteViews.setTextViewText(R.id.widget_movie_title, mMovieTitle);
            remoteViews.setTextViewText(R.id.widget_movie_release, mMovieReleaseDate);
            String moviePosterPath = UrlUtils.buildPosterPathUrl(mMoviePoster);
            Picasso.get()
                    .load(moviePosterPath)
                    .into(remoteViews, R.id.widget_image_view, new int[] { widgetId });

            Intent intent = new Intent(context, DetailsActivity.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            intent.putExtra(MOVIE_KEY, currentMovie);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    intent, PendingIntent.FLAG_ONE_SHOT);

            remoteViews.setOnClickPendingIntent(R.id.widget_image_view, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

}
