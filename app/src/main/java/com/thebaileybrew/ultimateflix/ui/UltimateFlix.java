package com.thebaileybrew.ultimateflix.ui;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thebaileybrew.ultimateflix.database.async.AsyncMovieLoader;
import com.thebaileybrew.ultimateflix.models.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.thebaileybrew.ultimateflix.database.ConstantUtils.CURRENT_FILM_ID;

public class UltimateFlix extends Application {
    private static final String TAG = UltimateFlix.class.getSimpleName();

    private static UltimateFlix mContext;
    private static List<Movie> allMovies = new ArrayList<>();
    private static AsyncMovieLoader asyncLoader;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMoviesReference;

    private FirebaseAuth mAuthUser;

    private String currentPage = "1";
    private int primaryKey = 0;

    private SharedPreferences sharedPrefs;




    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(UltimateFlix.getContext());
        primaryKey = sharedPrefs.getInt(CURRENT_FILM_ID,0);
        updateAsyncParameters(currentPage);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMoviesReference = mFirebaseDatabase.getReference().child("movies");
        loadFirebaseDatabase();



    }

    public void loadFirebaseDatabase() {
        for (int m = 0; m < allMovies.size(); m++) {
            final Movie addMovie = allMovies.get(m);
            primaryKey++;
            sharedPrefs.edit().putInt(CURRENT_FILM_ID, primaryKey).apply();
            mMoviesReference.push().setValue(addMovie);
        }
    }

    public static UltimateFlix getContext() {
        return mContext;
    }

    public static void updateAsyncParameters(String pageNumber) {
        asyncLoader = new AsyncMovieLoader();
        try {
            allMovies = asyncLoader.execute(pageNumber).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void updateFirebase(String currentPage) {
        this.currentPage = currentPage;
        UltimateFlix.updateAsyncParameters(this.currentPage);
        loadFirebaseDatabase();
    }


}
