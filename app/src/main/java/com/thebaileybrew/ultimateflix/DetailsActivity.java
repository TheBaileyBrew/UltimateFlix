package com.thebaileybrew.ultimateflix;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.thebaileybrew.ultimateflix.adapters.CreditsAdapter;
import com.thebaileybrew.ultimateflix.database.async.AsyncCreditsLoader;
import com.thebaileybrew.ultimateflix.models.Credit;
import com.thebaileybrew.ultimateflix.models.Movie;
import com.thebaileybrew.ultimateflix.ui.UltimateFlix;
import com.thebaileybrew.ultimateflix.ui.fragments.DashDetailsFragment;
import com.thebaileybrew.ultimateflix.ui.fragments.DashReviewsFragment;
import com.thebaileybrew.ultimateflix.ui.fragments.DashTrailersFragment;
import com.thebaileybrew.ultimateflix.utils.UrlUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.thebaileybrew.ultimateflix.database.ConstantUtils.*;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = DetailsActivity.class.getSimpleName();

    private TextView mTextMessage;
    private int movieID;
    private Fragment loadedFragment = null;
    private Movie currentMovie = new Movie();
    private List<Credit> allCredits = new ArrayList<>();
    private String movieBackdrop;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_details:
                    loadFragment(DASH_DETAILS);
                    break;
                case R.id.navigation_trailers:
                    loadFragment(DASH_TRAILERS);
                    break;
                case R.id.navigation_reviews:
                    loadFragment(DASH_REVIEWS);
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_toolbar_view);
        Intent getMovieIntent = getIntent();
        if (getMovieIntent != null) {
            currentMovie = getMovieIntent.getParcelableExtra(MOVIE_KEY);
            movieID = currentMovie.getMovieID();
            Log.e(TAG, "onCreate: movieID: " + movieID);
            getMovieDetails(movieID);
        }
        setupToolbarView();

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mMovieDatabaseReference = mFirebaseDatabase.getReference();

        initDiagView();
        initNavigationPanel();
        initCreditsRecycler();
    }

    private void setupToolbarView() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.getNavigationIcon().setColorFilter(ContextCompat.getColor(UltimateFlix.getContext(), R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setSubtitle(null);
        getSupportActionBar().getThemedContext();

        AppBarLayout appBarLayout = findViewById(R.id.app_toolbar);
        appBarLayout.setExpanded(true);
        appBarLayout.setOutlineProvider(null);

        TextView movieTitle = findViewById(R.id.movie_title_text);
        movieTitle.setText(currentMovie.getMovieTitle());

        TextView releaseDate = findViewById(R.id.release_detail);
        releaseDate.setText(formatDate(currentMovie.getMovieReleaseDate()));
    }


    private void getMovieDetails(int movieID) {
        AsyncCreditsLoader asyncCreditsLoader = new AsyncCreditsLoader();
        try {
            allCredits = asyncCreditsLoader.execute(String.valueOf(movieID)).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initDiagView() {
        KenBurnsView backdropImage = findViewById(R.id.movie_backdrop);
        String backdropImageResource = UrlUtils.buildBackdropUrl(currentMovie.getMovieBackdrop(), currentMovie.getMoviePosterPath());
        Picasso.get()
                .load(backdropImageResource)
                .placeholder(R.drawable.flix_logo)
                .into(backdropImage);
    }

    private void initCreditsRecycler() {
        RecyclerView creditRecycler = findViewById(R.id.credit_recycler);
        creditRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        CreditsAdapter adapter = new CreditsAdapter(UltimateFlix.getContext(), allCredits, creditRecycler);
        creditRecycler.setAdapter(adapter);


    }

    private void loadFragment(String fragmentName) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        //transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        Bundle bundle = new Bundle();
        bundle.putInt(MOVIE_KEY, movieID);
        bundle.putString(MOVIE_SYNOPSIS, currentMovie.getMovieOverview());
        bundle.putString(MOVIE_RELEASE_DATE, currentMovie.getMovieReleaseDate());
        bundle.putDouble(MOVIE_AVERAGE, currentMovie.getMovieVoteAverage());
        bundle.putInt(MOVIE_VOTE_COUNT, currentMovie.getMovieVoteCount());

        switch (fragmentName) {
            case DASH_DETAILS:
                if(loadedFragment == null) {
                    loadedFragment = new DashDetailsFragment();
                    loadedFragment.setEnterTransition(new Slide(Gravity.END));
                    loadedFragment.setArguments(bundle);
                    transaction.add(R.id.content_for_fragment, loadedFragment).commit();
                } else {
                    if (!(fm.findFragmentById(R.id.content_for_fragment) instanceof DashDetailsFragment)) {
                        loadedFragment = new DashDetailsFragment();

                        loadedFragment.setEnterTransition(new Slide(Gravity.END));
                        loadedFragment.setArguments(bundle);
                        transaction.replace(R.id.content_for_fragment, loadedFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
                break;
            case DASH_REVIEWS:
                if(loadedFragment == null) {
                    loadedFragment = new DashReviewsFragment();
                    loadedFragment.setEnterTransition(new Slide(Gravity.END));
                    loadedFragment.setArguments(bundle);
                    transaction.add(R.id.content_for_fragment, loadedFragment).commit();
                } else {
                    if (!(fm.findFragmentById(R.id.content_for_fragment) instanceof DashReviewsFragment)) {
                        loadedFragment = new DashReviewsFragment();
                        loadedFragment.setEnterTransition(new Slide(Gravity.END));
                        loadedFragment.setArguments(bundle);
                        transaction.replace(R.id.content_for_fragment, loadedFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
                break;
            case DASH_TRAILERS:
                if(loadedFragment == null) {
                    loadedFragment = new DashTrailersFragment();
                    loadedFragment.setEnterTransition(new Slide(Gravity.END));
                    loadedFragment.setArguments(bundle);
                    transaction.add(R.id.content_for_fragment, loadedFragment).commit();
                } else {
                    if (!(fm.findFragmentById(R.id.content_for_fragment) instanceof DashTrailersFragment)) {
                        loadedFragment = new DashTrailersFragment();
                        loadedFragment.setEnterTransition(new Slide(Gravity.END));
                        loadedFragment.setArguments(bundle);
                        transaction.replace(R.id.content_for_fragment, loadedFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
                break;
        }
    }

    private void initNavigationPanel() {
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(0);
        loadFragment(DASH_DETAILS);
    }

    private String formatDate(String movieReleaseDate) {
        String[] datestamps = movieReleaseDate.split("-");
        String dateYear = datestamps[0];
        String dateMonth = datestamps[1];
        String dateDay = datestamps[2];
        return dateMonth + getString(R.string.linebreak) + dateDay + getString(R.string.linebreak) + dateYear;
    }

    public int getMovieID() {
        return movieID;
    }

}
