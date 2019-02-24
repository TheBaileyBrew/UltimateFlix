package com.thebaileybrew.ultimateflix.ui.fragments;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.thebaileybrew.ultimateflix.DetailsActivity;
import com.thebaileybrew.ultimateflix.R;
import com.thebaileybrew.ultimateflix.adapters.StaticProgressBar;
import com.thebaileybrew.ultimateflix.database.async.AsyncFilmLoader;
import com.thebaileybrew.ultimateflix.models.Movie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.thebaileybrew.ultimateflix.database.ConstantUtils.*;

public class DashDetailsFragment extends Fragment {

    private int movieID, movieVoteCount;
    private String movieSynopsis;
    private double movieVoteAverage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieID = getArguments().getInt(MOVIE_KEY,0);
        movieSynopsis = getArguments().getString(MOVIE_SYNOPSIS);
        movieVoteAverage = getArguments().getDouble(MOVIE_AVERAGE);
        movieVoteCount = getArguments().getInt(MOVIE_VOTE_COUNT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        //TODO: Setup view objects
        TextView overviewTextView = view.findViewById(R.id.synopsis_text);
        TextView genresTextView = view.findViewById(R.id.movie_genres);
        TextView taglineTextView = view.findViewById(R.id.movie_tagline);
        TextView runtimeTextView = view.findViewById(R.id.runtime_detail);
        TextView languageTextView = view.findViewById(R.id.language_detail);
        TextView budgetTextView = view.findViewById(R.id.budget_detail);
        TextView revenueTextView = view.findViewById(R.id.revenue_detail);
        AsyncFilmLoader filmLoader = new AsyncFilmLoader(taglineTextView, genresTextView,
                runtimeTextView, languageTextView, revenueTextView, budgetTextView);
        filmLoader.execute(String.valueOf(movieID));
        overviewTextView.setText(movieSynopsis);

        StaticProgressBar popularitySpinnerView = view.findViewById(R.id.popularity_details);
        setupPopularitySpinnerViews(popularitySpinnerView);

        return view;
    }

    private void setupPopularitySpinnerViews(StaticProgressBar popularitySpinnerView) {
        popularitySpinnerView.setMax(100);
        popularitySpinnerView.setSuffixText("");
        float value = (float) movieVoteAverage;
        value = value * 10;
        ObjectAnimator animation = ObjectAnimator.ofFloat(popularitySpinnerView,
                "progress", 0, value);
        animation.setDuration(2000);
        animation.setInterpolator(new OvershootInterpolator());
        animation.start();

        if(movieVoteAverage >= 7.00) {
            popularitySpinnerView.setFinishedStrokeColor(Color.parseColor("#25cc00"));
            popularitySpinnerView.setUnfinishedStrokeColor(Color.parseColor("#b8ffc3"));
            if (movieVoteAverage >= 8.00) {
                popularitySpinnerView.setBottomText("GREAT");
            } else if (movieVoteAverage >= 9.00) {
                popularitySpinnerView.setBottomText("BEST");
            } else {
                popularitySpinnerView.setBottomText("GOOD");
            }
        } else if (movieVoteAverage >= 4.25) {
            popularitySpinnerView.setFinishedStrokeColor(Color.parseColor("#f5c400"));
            popularitySpinnerView.setUnfinishedStrokeColor(Color.parseColor("#ffe7ab"));
            if (movieVoteAverage >= 6.00) {
                popularitySpinnerView.setBottomText("AVERAGE");
            } else if (movieVoteAverage >=4.75) {
                popularitySpinnerView.setBottomText("OKAY");
            } else {
                popularitySpinnerView.setBottomText("MEH..");
            }
        } else {
            popularitySpinnerView.setFinishedStrokeColor(Color.parseColor("#dc0202"));
            popularitySpinnerView.setUnfinishedStrokeColor(Color.parseColor("#ffa1a1"));
            if (movieVoteAverage >= 3.5) {
                popularitySpinnerView.setBottomText("MEH");
            } else if (movieVoteAverage >= 2.00) {
                popularitySpinnerView.setBottomText("AVOID");
            } else if (movieVoteAverage == 0.00) {
                popularitySpinnerView.setBottomText("NO SCORE");
            } else {
                popularitySpinnerView.setBottomText("HARD PASS");
            }
        }
    }

    private String setupVotesViews() {
        return movieVoteAverage + "%" + " / " + movieVoteCount + " votes";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
