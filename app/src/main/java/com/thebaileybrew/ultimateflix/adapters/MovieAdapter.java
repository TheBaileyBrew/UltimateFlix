package com.thebaileybrew.ultimateflix.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.thebaileybrew.ultimateflix.R;
import com.thebaileybrew.ultimateflix.models.Movie;
import com.thebaileybrew.ultimateflix.utils.UrlUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private static final String TAG = MovieAdapter.class.getSimpleName();

    private final LayoutInflater layoutInflater;
    private List<Movie> movieCollection = new ArrayList<>();
    final private MovieAdapterClickHandler adapterClickHandler;

    public interface MovieAdapterClickHandler {
        void onClick(View view, Movie movie);
        void onLongClick(View view, Movie movie, ImageView hiddenStar);
    }

    //Create the recycler
    public MovieAdapter(Context context, MovieAdapterClickHandler clicker) {
        this.layoutInflater = LayoutInflater.from(context);
        this.adapterClickHandler = clicker;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.movie_card_view, parent, false);
        return new ViewHolder(view);
    }

    //Bind the Arraydata to the layoutview
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.e(TAG, "onBindViewHolder: current position" + position );
        Log.e(TAG, "onBindViewHolder: size of collection " + movieCollection.size() );
        final Movie currentMovie = movieCollection.get(position);
        Log.e(TAG, "onBindViewHolder: current poster" + currentMovie.getMoviePosterPath() );
        String moviePosterPath = UrlUtils.buildPosterPathUrl(currentMovie.getMoviePosterPath());
        Log.e(TAG, "onBindViewHolder: poster path: " + moviePosterPath );

        Picasso.get()
                .load(moviePosterPath)
                .placeholder(R.drawable.flix_logo)
                .into(holder.moviePoster);
    }

    @Override
    public int getItemCount() {
        if (movieCollection == null) {
            return 0;
        } else {
            return movieCollection.size();
        }
    }

    public void setMovieCollection(List<Movie> moviesReturn) {
        movieCollection = moviesReturn;
        notifyDataSetChanged();
    }

    public List<Movie> getMovieCollection() {
        return movieCollection;
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final ImageView moviePoster;
        final ImageView hiddenViewStar;

        private ViewHolder(View newView) {
            super(newView);
            moviePoster = newView.findViewById(R.id.movie_cardview_poster);
            hiddenViewStar = newView.findViewById(R.id.hidden_star);
            newView.setOnClickListener(this);
            newView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Movie currentMovie = movieCollection.get(getAdapterPosition());
            adapterClickHandler.onClick(v, currentMovie);
        }


        @Override
        public boolean onLongClick(View v) {
            Movie currentMovie = movieCollection.get(getAdapterPosition());
            adapterClickHandler.onLongClick(v, currentMovie, hiddenViewStar);
            return true;
        }
    }
}
