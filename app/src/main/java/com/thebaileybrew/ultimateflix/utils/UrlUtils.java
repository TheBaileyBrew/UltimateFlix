package com.thebaileybrew.ultimateflix.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UrlUtils {
    private static final String TAG = UrlUtils.class.getSimpleName();

    private static final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/";
    private static final String BASE_IMAGE_LARGE = "w1280";

    private static final String BASE_MOVIE_URL = "https://api.themoviedb.org/3";
    private static final String BASE_MOVIE_PATH = "movie";
    private static final String BASE_MOVIE_PATH_DISCOVER = "discover";
    private static final String BASE_MOVIE_PATH_SEARCH = "search";
    private static final String BASE_CREDIT_PATH = "credits";
    private static final String BASE_VIDEO_PATH = "videos";
    private static final String BASE_REVIEW_PATH = "reviews";

    private static final String BASE_YOUTUBE_URL = "https://www.youtube.com/";
    private static final String BASE_YOUTUBE_WATCH = "watch?v=";
    private static final String BASE_YOUTUBE_IMAGE_URL = "https://img.youtube.com/vi/";
    private static final String BASE_YOUTUBE_IMAGE_FULLSIZE = "/0.jpg";

    private static final String API_KEY = "api_key";
    private static final String SORT_BY = "sort_by";
    private static final String VOTE_COUNT = "vote_count.gte";
    private static final String VOTE_MINIMUM = "1000";
    private static final String WITH_ORIG_LANGUAGE = "with_original_language";
    private static final String SORTING_ORDER = "popularity.desc";
    private static final String PAGE = "page";


    private static String baseMovieQueryUrl(String apiKey, String pageNumber) {
        Uri movieQuery = Uri.parse(BASE_MOVIE_URL).buildUpon()
                    .appendPath(BASE_MOVIE_PATH_DISCOVER)
                    .appendPath(BASE_MOVIE_PATH)
                    .appendQueryParameter(API_KEY, apiKey)
                    .appendQueryParameter(PAGE, pageNumber)
                    .build();
        return movieQuery.toString();
    }

    //Build the URL for querying all movies in the database
    public static URL buildMovieUrl(
            String apiKey, String pageNumber){
        Uri movieQueryUri;

        String baseMovie = baseMovieQueryUrl(apiKey, pageNumber);
        movieQueryUri = Uri.parse(baseMovie).buildUpon()
                .appendQueryParameter(SORT_BY, SORTING_ORDER)
                .build();

        URL movieQueryURL;
        try {
            movieQueryURL = new URL(movieQueryUri.toString());
            return movieQueryURL;
        } catch (MalformedURLException me) {
            Log.e(TAG, "buildMovieUrl: failed to build full db URL", me);
            return null;
        }
    }

    //Build the URL for querying a single movie in the database
    public static URL buildSingleMovieUrl(String apiKey, String movieID) {

        Uri singleMovieQuery = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendPath(BASE_MOVIE_PATH)
                .appendPath(movieID)
                .appendQueryParameter(API_KEY, apiKey)
                .build();
        URL singleMovieURL;
        try {
            singleMovieURL = new URL(singleMovieQuery.toString());
            return singleMovieURL;
        } catch (MalformedURLException me) {
            Log.e(TAG, "buildSingleMovieUrl: failed to build single URL", me);
            return null;
        }
    }

    //Build the poster path url
    public static String buildPosterPathUrl(String posterPath) {
        return BASE_IMAGE_URL + BASE_IMAGE_LARGE + posterPath;
    }
    //Build the backdrop path url
    public static String buildBackdropUrl(String backdropPath, String posterPath) {
        if (backdropPath.equals("null")) {
            return BASE_IMAGE_URL + BASE_IMAGE_LARGE + posterPath;
        } else {
            return BASE_IMAGE_URL + BASE_IMAGE_LARGE + backdropPath;
        }
    }

    //Build the credit details url
    public static URL buildCreditsMovieUrl(String apiKey, String movieID) {
        Uri movieCreditsQuery = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendPath(BASE_MOVIE_PATH)
                .appendPath(movieID)
                .appendPath(BASE_CREDIT_PATH)
                .appendQueryParameter(API_KEY, apiKey)
                .build();
        URL creditsQueryURL;
        try {
            creditsQueryURL = new URL(movieCreditsQuery.toString());
            return creditsQueryURL;
        } catch (MalformedURLException me) {
            Log.e(TAG, "buildCreditsMovieUrl: failed to build credits url", me);
            return null;
        }
    }

    //Build the credit image url
    public static String buildCreditImageUrl (String creditPath) {
        return BASE_IMAGE_URL + BASE_IMAGE_LARGE + creditPath;
    }

    //Build the Review Details
    public static URL buildReviewUrl (String apiKey, String movieID) {
        Uri reviewLinkQuery = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendEncodedPath(BASE_MOVIE_PATH)
                .appendPath(movieID)
                .appendPath(BASE_REVIEW_PATH)
                .appendQueryParameter(API_KEY, apiKey)
                .build();
        URL reviewQueryURL;
        try {
            reviewQueryURL = new URL(reviewLinkQuery.toString());
            return reviewQueryURL;
        } catch (MalformedURLException me) {
            Log.e(TAG, "buildReviewUrl: failed to build reviews url", me);
            return null;
        }
    }

    //Build the Video details
    public static URL buildVideoUrl (String apiKey, String movieID) {
        Uri youtubeLinkQuery = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendPath(BASE_MOVIE_PATH)
                .appendPath(movieID)
                .appendPath(BASE_VIDEO_PATH)
                .appendQueryParameter(API_KEY, apiKey)
                .build();
        URL videosQueryURL;
        try {
            videosQueryURL = new URL(youtubeLinkQuery.toString());
            return videosQueryURL;
        } catch (MalformedURLException mue) {
            Log.e(TAG, "buildVideoUrl: failed to build video url", mue);
            return null;
        }
    }

    //Build the Youtube Video url
    public static String buildYoutubeTrailerUrl (String youtubePath) {
        return BASE_YOUTUBE_URL + BASE_YOUTUBE_WATCH + youtubePath;
    }

    //Build the Youtube Image url
    public static String buildYoutubeImageUrl (String youtubeImage) {
        Log.e(TAG, "buildYoutubeImageUrl: returned value: " + BASE_YOUTUBE_IMAGE_URL + youtubeImage + BASE_YOUTUBE_IMAGE_FULLSIZE);
        return BASE_YOUTUBE_IMAGE_URL + youtubeImage + BASE_YOUTUBE_IMAGE_FULLSIZE;
    }
}
