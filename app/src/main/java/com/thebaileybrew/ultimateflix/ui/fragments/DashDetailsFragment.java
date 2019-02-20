package com.thebaileybrew.ultimateflix.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thebaileybrew.ultimateflix.DetailsActivity;
import com.thebaileybrew.ultimateflix.R;
import com.thebaileybrew.ultimateflix.database.async.AsyncFilmLoader;
import com.thebaileybrew.ultimateflix.models.Movie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_KEY;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_RELEASE_DATE;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_SYNOPSIS;

public class DashDetailsFragment extends Fragment {

    private int movieID;
    private String movieSynopsis;
    private String movieRelease;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieID = getArguments().getInt(MOVIE_KEY,0);
        movieSynopsis = getArguments().getString(MOVIE_SYNOPSIS);
        movieRelease = getArguments().getString(MOVIE_RELEASE_DATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        //TODO: Setup view objects
        TextView overviewTextView = view.findViewById(R.id.synopsis_text);
        TextView genresTextView = view.findViewById(R.id.movie_genres);
        TextView taglineTextView = view.findViewById(R.id.movie_tagline);
        TextView releaseTextView = view.findViewById(R.id.release_detail);
        TextView runtimeTextView = view.findViewById(R.id.runtime_detail);
        AsyncFilmLoader filmLoader = new AsyncFilmLoader(taglineTextView, genresTextView, runtimeTextView);
        filmLoader.execute(String.valueOf(movieID));
        overviewTextView.setText(movieSynopsis);
        releaseTextView.setText(formatDate(movieRelease));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    private String formatDate(String movieReleaseDate) {
        String[] datestamps = movieReleaseDate.split("-");
        String dateYear = datestamps[0];
        String dateMonth = datestamps[1];
        String dateDay = datestamps[2];
        return dateMonth + getString(R.string.linebreak) + dateDay + getString(R.string.linebreak) + dateYear;
    }
}
