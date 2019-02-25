package com.thebaileybrew.ultimateflix;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.thebaileybrew.ultimateflix.adapters.MovieAdapter;
import com.thebaileybrew.ultimateflix.database.MovieSnapshotViewModel;
import com.thebaileybrew.ultimateflix.listeners.EndlessRecyclerOnScrollListener;
import com.thebaileybrew.ultimateflix.models.Movie;
import com.thebaileybrew.ultimateflix.ui.UltimateFlix;
import com.thebaileybrew.ultimateflix.utils.displayMetricsUtils;
import com.thebaileybrew.ultimateflix.utils.networkUtils;

import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.view.View.VISIBLE;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.CURRENT_FILM_ID;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_KEY;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_ID;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_POSTER;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_RELEASE;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.WIDGET_MOVIE_TITLE;

public class MovieSelectActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterClickHandler {
    private static final String TAG = MovieSelectActivity.class.getSimpleName();

    private FirebaseAnalytics mFirebaseAnalytics;

    private MovieAdapter mAdapter;
    private SharedPreferences sharedPrefs;
    private int currentPage = 1;

    private RecyclerView mRecyclerView;
    private ConstraintLayout noNetworkLayout;
    private GridLayoutManager gridLayoutManager;
    private SwipeRefreshLayout swipeRefresh;

    private LinearLayout searchLayout;
    private TextInputEditText searchEntry;
    private boolean searchVisible = false;

    private Animation animFadeIn, animFadeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(currentPage));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        initViews();
        //defineAnimation();

        setupRecyclerView();
        setupListeners();
    }

    //Set up the Recycler View and check for Network Connection
    private void setupRecyclerView() {
        mRecyclerView = findViewById(R.id.movie_recycler);


        //Define Grid size/scale factor
        int columnIndex = displayMetricsUtils.calculateGridColumn(this);
        gridLayoutManager = new GridLayoutManager(this, columnIndex);

        //Check for network
        if (networkUtils.checkNetwork(this)) {
            //Load Movies
            noNetworkLayout.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(VISIBLE);
            populateUI();
        } else {
            //Show no connection layout
            mRecyclerView.setVisibility(View.INVISIBLE);
            noNetworkLayout.setVisibility(VISIBLE);
            getRandomNoNetworkView();
            swipeRefresh.setRefreshing(false);
        }

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new MovieAdapter(UltimateFlix.getContext(), this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(gridLayoutManager) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                Log.e(TAG, "onLoadMore: called " + currentPage);
                currentPage++;
                int currentSize = mAdapter.getItemCount();
                UltimateFlix.getContext().updateFirebase(String.valueOf(currentPage));
                Log.e(TAG, "onLoadMore: called " + currentPage + " - " + currentSize );
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    //Starts listeners for (ViewModel, Swipe to Refresh, Search Entry)
    private void setupListeners() {
        MovieSnapshotViewModel viewModel = ViewModelProviders.of(this).get(MovieSnapshotViewModel.class);
        LiveData<List<Movie>> liveData = viewModel.getDataSnapshotLiveData();
        liveData.observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                if (movies != null) {
                    mAdapter.setMovieCollection(movies);
                }
            }
        });

        //Set the Swipe To Refresh Listener
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (networkUtils.checkNetwork(UltimateFlix.getContext())) {
                    //Load Movies
                    noNetworkLayout.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(VISIBLE);
                    Log.e(TAG, "onRefresh: Swiped");
                    populateUI();
                } else {
                    //Show no connection layout
                    noNetworkLayout.setVisibility(VISIBLE);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    getRandomNoNetworkView();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateUI();
    }

    //Initialize the views in the activity
    private void initViews() {
        noNetworkLayout = findViewById(R.id.no_connection_constraint_layout);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        searchLayout = findViewById(R.id.search_layout);
        TextInputLayout searchEntryLayout = findViewById(R.id.search_layout_entry);
        searchEntry = findViewById(R.id.search_entry);
    }

    //Collect SharedPrefs from Activity
    private void getSharedPreferences() {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(UltimateFlix.getContext());
    }

    //Method to show the hidden layout for searching the movie database
    private void showSearchMenu() {
        searchLayout.setVisibility(VISIBLE);
        searchVisible = true;
    }
    private void hideSearchMenu() {
        searchLayout.setVisibility(View.INVISIBLE);
        searchVisible = false;
    }

    private void getRandomNoNetworkView() {
        getSharedPreferences();
        if (!networkUtils.checkNetwork(UltimateFlix.getContext())) {
            TextView noNetworkTextMessageOne = findViewById(R.id.internet_out_message);
            ImageView noNetworkImage = findViewById(R.id.internet_out_image);
            Random randomNetworkGen = new Random();
            int i = randomNetworkGen.nextInt((5 - 1) + 1);
            switch (i) {
                case 1:
                    noNetworkTextMessageOne.setText(getString(R.string.internet_message_one));
                    noNetworkImage.setImageResource(R.drawable.voldemort);
                    break;
                case 2:
                    noNetworkTextMessageOne.setText(getString(R.string.internet_message_two));
                    noNetworkImage.setImageResource(R.drawable.wonka);
                    break;
                case 3:
                    noNetworkTextMessageOne.setText(getString(R.string.internet_message_three));
                    noNetworkImage.setImageResource(R.drawable.lotr);
                    break;
                case 4:
                    noNetworkTextMessageOne.setText(getString(R.string.internet_message_four));
                    noNetworkImage.setImageResource(R.drawable.taken);
                    break;
                default:
                    noNetworkTextMessageOne.setText(getString(R.string.internet_message_default));
                    noNetworkImage.setImageResource(R.drawable.thanos);
                    break;

            }
            swipeRefresh.setRefreshing(false);
        } else {
            populateUI();
        }
    }

    //Update the UI views based on preferences
    private void populateUI() {
        getSharedPreferences();
        swipeRefresh.setRefreshing(false);
    }

    //Create Menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return true;
    }

    //Determines function for menu option selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_refresh:
                mAdapter.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //OnClick Interface for Movie Selection
    @Override
    public void onClick(View view, Movie movie) {
        Log.e(TAG, "onClick: view clicked: " + view.getId() );
        Intent openDisplayDetails = new Intent(MovieSelectActivity.this, DetailsActivity.class);
        //Put Parcel Extra
        openDisplayDetails.putExtra(MOVIE_KEY, movie);
        Log.e(TAG, "onClick: clicked movie is: " + movie.getMovieTitle() + " - " + movie.getMovieID() );
        startActivity(openDisplayDetails);
    }

    @Override
    public void onLongClick(View view, int movieID, String movieTitle, String movieRelease, String moviePath) {
        sharedPrefs.edit().putInt(WIDGET_MOVIE_ID, movieID).apply();
        sharedPrefs.edit().putString(WIDGET_MOVIE_TITLE, movieTitle).apply();
        sharedPrefs.edit().putString(WIDGET_MOVIE_RELEASE, movieRelease).apply();
        sharedPrefs.edit().putString(WIDGET_MOVIE_POSTER, moviePath).apply();
        Toast.makeText(this, "Movie: " + movieTitle + " added to Widget Data", Toast.LENGTH_SHORT).show();
    }

}
