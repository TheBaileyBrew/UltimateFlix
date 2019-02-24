package com.thebaileybrew.ultimateflix.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thebaileybrew.ultimateflix.R;
import com.thebaileybrew.ultimateflix.adapters.VideosAdapter;
import com.thebaileybrew.ultimateflix.database.async.AsyncTrailerLoader;
import com.thebaileybrew.ultimateflix.models.Videos;
import com.thebaileybrew.ultimateflix.ui.UltimateFlix;
import com.thebaileybrew.ultimateflix.utils.UrlUtils;

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

public class DashTrailersFragment extends Fragment {
    private static final String TAG = DashTrailersFragment.class.getSimpleName();

    private RecyclerView trailerRecycler;
    private List<Videos> videos = new ArrayList<>();
    private int movieID;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieID = getArguments().getInt(MOVIE_KEY,0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_trailers, container, false);
        //TODO: Setup view objects
        ConstraintLayout noDataView = rootView.findViewById(R.id.no_trailers_constraint_layout);
        //DO STUFF HERE
        trailerRecycler = rootView.findViewById(R.id.trailer_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UltimateFlix.getContext());
        trailerRecycler.setLayoutManager(linearLayoutManager);
        VideosAdapter videosAdapter = new VideosAdapter(UltimateFlix.getContext(), videos, new VideosAdapter.VideoClickHandler() {
            @Override
            public void onClick(View view, Videos video) {
                //Open Intent to view the video
                String trailerUrl = UrlUtils.buildYoutubeTrailerUrl(video.getVideoKey());

                Intent videoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                if(videoIntent.resolveActivity(UltimateFlix.getContext().getPackageManager()) != null) {
                    startActivity(videoIntent);
                }
            }
        });
        trailerRecycler.setAdapter(videosAdapter);
        AsyncTrailerLoader videosLoader = new AsyncTrailerLoader(videosAdapter);
        try {
            videos = videosLoader.execute(String.valueOf(movieID)).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(TAG, "onCreateView: error", e);;
        }
        if (videos.isEmpty()) {
            Log.e(TAG, "onCreateView: vids empty");
            noDataView.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, "onCreateView: vids not empty");
            noDataView.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
