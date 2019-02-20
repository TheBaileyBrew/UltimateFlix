package com.thebaileybrew.ultimateflix.ui;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thebaileybrew.ultimateflix.R;
import com.thebaileybrew.ultimateflix.database.async.AsyncCreditsLoader;
import com.thebaileybrew.ultimateflix.database.async.AsyncMovieLoader;
import com.thebaileybrew.ultimateflix.models.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;

public class UltimateFlix extends Application {
    private static final String TAG = UltimateFlix.class.getSimpleName();

    private static UltimateFlix mContext;
    private static List<Movie> allMovies = new ArrayList<>();
    private static AsyncMovieLoader asyncLoader;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMovieDatabaseReference;
    private DatabaseReference mCreditDatabaseReference;
    private DatabaseReference mTrailerDatabaseReference;
    private DatabaseReference mReviewDatabaseReference;

    private FirebaseAuth mAuthUser;

    private SharedPreferences sharedPrefs;
    private String queryResult = "";
    private String sorting, language, filterYear;




    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        getSharedPreferences();
        updateAsyncParameters(sorting, language, filterYear, queryResult);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.setPersistenceEnabled(true);
        mAuthUser = FirebaseAuth.getInstance();
        mMovieDatabaseReference = mFirebaseDatabase.getReference("/movies");

        Log.e(TAG, "UltimateFlix: size: " + allMovies.size() );
        for (int m = 0; m < allMovies.size(); m++) {
            final Movie addMovie = allMovies.get(m);
            Log.e(TAG, "UltimateFlix: moviepath: " +addMovie.getMoviePosterPath());
            mMovieDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mMovieDatabaseReference.child(String.valueOf(addMovie.getMovieID())).setValue(addMovie);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }

    public static UltimateFlix getContext() {
        return mContext;
    }

    public static void updateAsyncParameters(String sort, String language, String year, String query) {
        asyncLoader = new AsyncMovieLoader();
        try {
            allMovies = asyncLoader.execute(sort, language, year, query).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void getSharedPreferences() {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(UltimateFlix.getContext());
        //Get the sorting method from shared prefs
        String sortingKey = getString(R.string.preference_sort_key);
        String sortingDefault = getString(R.string.preference_sort_popular);
        sorting = sharedPrefs.getString(sortingKey, sortingDefault);
        //Get the langauge default from shared prefs
        String languageKey = getString(R.string.preference_sort_language_key);
        String languageDefault = getString(R.string.preference_sort_language_all);
        language = sharedPrefs.getString(languageKey, languageDefault);
        //Get filter year from shared prefs
        String filterYearKey = getString(R.string.preference_year_key);
        String filterYearDefault = getString(R.string.preference_year_default);
        filterYear = sharedPrefs.getString(filterYearKey, filterYearDefault);
    }


}
