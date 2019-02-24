package com.thebaileybrew.ultimateflix.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thebaileybrew.ultimateflix.R;
import com.thebaileybrew.ultimateflix.adapters.ReviewAdapter;
import com.thebaileybrew.ultimateflix.database.async.AsyncReviewLoader;
import com.thebaileybrew.ultimateflix.models.Review;
import com.thebaileybrew.ultimateflix.ui.UltimateFlix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_KEY;

public class DashReviewsFragment extends Fragment {
    private static final String TAG = DashReviewsFragment.class.getSimpleName();

    private RecyclerView reviewRecycler;
    private int movieID;
    private List<Review> reviews = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieID = getArguments().getInt(MOVIE_KEY,0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_reviews, container, false);
        ConstraintLayout noDataView = rootView.findViewById(R.id.no_reviews_constraint_layout);
        reviewRecycler = rootView.findViewById(R.id.review_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UltimateFlix.getContext());
        reviewRecycler.setLayoutManager(linearLayoutManager);
        ReviewAdapter reviewAdapter = new ReviewAdapter(UltimateFlix.getContext(), reviews, new ReviewAdapter.ReviewClickHandler() {
            @Override
            public void onClick(View view, Review review) {
                String reviewLink = review.getReviewLink();
                Intent reviewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(reviewLink));
                if (reviewIntent.resolveActivity(UltimateFlix.getContext().getPackageManager()) != null) {
                    startActivity(reviewIntent);
                }
            }
        });
        reviewRecycler.setAdapter(reviewAdapter);
        AsyncReviewLoader reviewLoader = new AsyncReviewLoader(reviewAdapter);
        try {
            reviews = reviewLoader.execute(String.valueOf(movieID)).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(TAG, "onCreateView: error", e);;
        }
        if (reviews.isEmpty()) {
            noDataView.setVisibility(View.VISIBLE);
        } else {
            noDataView.setVisibility(View.INVISIBLE);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
