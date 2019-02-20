package com.thebaileybrew.ultimateflix;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.thebaileybrew.ultimateflix.adapters.MovieAdapter;
import com.thebaileybrew.ultimateflix.database.MovieSnapshotViewModel;
import com.thebaileybrew.ultimateflix.database.loaders.MovieLoader;
import com.thebaileybrew.ultimateflix.models.Movie;
import com.thebaileybrew.ultimateflix.ui.MoviePreferences;
import com.thebaileybrew.ultimateflix.ui.UltimateFlix;
import com.thebaileybrew.ultimateflix.utils.displayMetricsUtils;
import com.thebaileybrew.ultimateflix.utils.networkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

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
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_KEY;

public class MovieSelectActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterClickHandler {
    private static final String TAG = MovieSelectActivity.class.getSimpleName();

    private MovieAdapter mAdapter;
    private SharedPreferences sharedPrefs;


    private String queryResult = "";
    private String sorting, language, filterYear;

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

        //Set the Search Text Listener
        searchEntry.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event != null
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        queryResult = v.getText().toString();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(searchEntry.getWindowToken(), 0);
                        }
                        getSharedPreferences();
                        hideSearchMenu();
                        swipeRefresh.setRefreshing(false);
                        return true;
                    }
                }
                return false;
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
        if (!sorting.equals(getString(R.string.preference_sort_favorite))
                && !sorting.equals(getString(R.string.preference_sort_watchlist))) {
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
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        return true;
    }

    //Determines function for menu option selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_prefs:
                Intent openSettings = new Intent(this, MoviePreferences.class);
                startActivity(openSettings);
                return true;
            case R.id.app_bar_search:
                if (searchVisible) {
                    hideSearchMenu();
                } else {
                    showSearchMenu();
                }
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

    //OnLongClick Interface for Dialog Creation
    @Override
    public void onLongClick(View view, Movie movie, ImageView hiddenStar) {
        Log.e(TAG, "onLongClick: view clicked: " + view.getId());
        int currentList = movie.getMovieFavorite();
        ImageView hidden = view.findViewById(R.id.hidden_star);
        showSelectionDialog(currentList, movie, hidden);
    }

    //Creates and customizes the long-click dialog to add/remove/change favorites and watchlist items
    //TODO: Still needs some work... See internal TODOs
    private void showSelectionDialog(int value, final Movie movie, final ImageView hidden) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Which List Should This Be Added To?");
        dialogBuilder.setNegativeButton("REMOVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: Update the View Model with REMOVE FROM FAVORITE

                hidden.setImageResource(R.drawable.ic_star);
                hidden.setVisibility(VISIBLE);
                hidden.startAnimation(animFadeIn);

                hidden.startAnimation(animFadeOut);
                hidden.setVisibility(View.INVISIBLE);
            }
        });
        dialogBuilder.setNeutralButton("FAVORITES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: Update the View Model with MARKED AS FAVORITE

                hidden.setImageResource(R.drawable.ic_star_border);
                hidden.setVisibility(VISIBLE);
                hidden.startAnimation(animFadeIn);
                hidden.startAnimation(animFadeOut);
                hidden.setVisibility(View.INVISIBLE);
            }
        });
        dialogBuilder.setPositiveButton("INTERESTED", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: Update the View Model with MARKED AS INTERESTED

                hidden.setImageResource(R.drawable.ic_star_interested);
                hidden.setVisibility(VISIBLE);
                hidden.startAnimation(animFadeIn);
                hidden.startAnimation(animFadeOut);
                hidden.setVisibility(View.INVISIBLE);
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorInterested));
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorFavorite));
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        switch(value) {
            case 0: //NOT ON A LIST SO CANNOT REMOVE WHAT DOESNT EXIST
            default:
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setClickable(false);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText("Not On A List Yet");
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorWhiteWash));
                break;
            case 1: //IS A CURRENT FAVORITE
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setClickable(false);
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText("Already A Favorite");
                alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorWhiteWash));
                break;
            case 2: //IS A CURRENT INTEREST
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Already On Watchlist");
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorWhiteWash));
                break;
        }
    }
}
