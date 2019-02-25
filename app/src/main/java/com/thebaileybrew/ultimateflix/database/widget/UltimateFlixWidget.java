package com.thebaileybrew.ultimateflix.database.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.thebaileybrew.ultimateflix.DetailsActivity;
import com.thebaileybrew.ultimateflix.MovieSelectActivity;
import com.thebaileybrew.ultimateflix.R;
import com.thebaileybrew.ultimateflix.utils.UrlUtils;

import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_KEY;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_NAME;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_POSTER_PATH;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_RELEASE_DATE;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_ID;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_POSTER;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_RELEASE;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_TITLE;

public class UltimateFlixWidget extends AppWidgetProvider {

    private String mMoviePoster;
    private int mMovieID;
    private String mMovieTitle;
    private String mMovieReleaseDate;
    private SharedPreferences sharedPreferences;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mMovieID = sharedPreferences.getInt(WIDGET_MOVIE_ID, 0);
        mMoviePoster = sharedPreferences.getString(WIDGET_MOVIE_POSTER, "POSTER");
        mMovieTitle = sharedPreferences.getString(WIDGET_MOVIE_TITLE, "MOVIE NAME");
        mMovieReleaseDate = sharedPreferences.getString(WIDGET_MOVIE_RELEASE, "NA");

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.ultimate_flix_widget_item);
            remoteViews.setTextViewText(R.id.widget_movie_title, mMovieTitle);
            remoteViews.setTextViewText(R.id.widget_movie_release, mMovieReleaseDate);
            String moviePosterPath = UrlUtils.buildPosterPathUrl(mMoviePoster);
            Picasso.get()
                    .load(moviePosterPath)
                    .into(remoteViews, R.id.widget_image_view, new int[] { widgetId });

            Intent intent = new Intent(context, MovieSelectActivity.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.widget_image_view, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
