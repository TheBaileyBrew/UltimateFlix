package com.thebaileybrew.ultimateflix.database;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.thebaileybrew.ultimateflix.models.Movie;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class MovieSnapshotViewModel extends ViewModel {
    private List<Movie> mList = new ArrayList<>();
    private static final DatabaseReference MOVIE_SNAPSHOT_REF
            = FirebaseDatabase.getInstance().getReference("movies");
    private static final Query MOVIE_DETAIL_REF = FirebaseDatabase.getInstance().getReference().orderByChild("movieID");

    private final FirebaseLiveDataQuery liveDataQuery = new FirebaseLiveDataQuery(MOVIE_SNAPSHOT_REF);
    private static FirebaseLiveDataQuery liveDataInputQuery = new FirebaseLiveDataQuery(MOVIE_DETAIL_REF);
    private final LiveData<List<Movie>> movieLiveData = Transformations.map(liveDataQuery, new Deserializer());
    private final LiveData<Movie> singleMovieLiveData = Transformations.map(liveDataInputQuery, new MovieDeserializer());

    /*
    public static void loadQuery(Query inputQuery) {
        liveDataInputQuery = new FirebaseLiveDataQuery(inputQuery);
    }
    */


    private class Deserializer implements Function<DataSnapshot, List<Movie>> {

        @Override
        public List<Movie> apply(DataSnapshot input) {
            mList.clear();
            for(DataSnapshot snapshot: input.getChildren()) {
                Movie movie = snapshot.getValue(Movie.class);
                mList.add(movie);
            }
            return mList;
        }
    }

    private class MovieDeserializer implements Function<DataSnapshot, Movie> {

        @Override
        public Movie apply(DataSnapshot input) {
            return input.getValue(Movie.class);
        };
    }

    @NonNull
    public LiveData<List<Movie>> getDataSnapshotLiveData() {
        return movieLiveData;
    }

    @NonNull
    public LiveData<Movie> getMovieSnapshot() {return singleMovieLiveData; }

}
