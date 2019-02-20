package com.thebaileybrew.ultimateflix;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.Transition;
import com.flaviofaria.kenburnsview.TransitionGenerator;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.thebaileybrew.ultimateflix.adapters.CreditsAdapter;
import com.thebaileybrew.ultimateflix.database.MovieSnapshotViewModel;
import com.thebaileybrew.ultimateflix.database.async.AsyncCreditsLoader;
import com.thebaileybrew.ultimateflix.models.Credit;
import com.thebaileybrew.ultimateflix.models.Movie;
import com.thebaileybrew.ultimateflix.ui.UltimateFlix;
import com.thebaileybrew.ultimateflix.ui.fragments.DashDetailsFragment;
import com.thebaileybrew.ultimateflix.ui.fragments.DashReviewsFragment;
import com.thebaileybrew.ultimateflix.ui.fragments.DashTrailersFragment;
import com.thebaileybrew.ultimateflix.utils.UrlUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.thebaileybrew.ultimateflix.database.ConstantUtils.DASH_DETAILS;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.DASH_REVIEWS;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.DASH_TRAILERS;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_KEY;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_RELEASE_DATE;
import static com.thebaileybrew.ultimateflix.database.ConstantUtils.MOVIE_SYNOPSIS;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = DetailsActivity.class.getSimpleName();

    private TextView mTextMessage;
    private int movieID;
    private Fragment loadedFragment = null;
    private Movie currentMovie = new Movie();
    private List<Credit> allCredits = new ArrayList<>();
    private String movieBackdrop;
    private KenBurnsView backdropImage;
    private Toolbar mToolbar;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMovieDatabaseReference;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
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

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMovieDatabaseReference = mFirebaseDatabase.getReference();

        initDiagView();
        initNavigationPanel();
        initCreditsRecycler();
    }

    private void setupToolbarView() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorAccentFade), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setTitle(currentMovie.getMovieTitle());
        getSupportActionBar().setSubtitle(currentMovie.getMovieLanguage());
        getSupportActionBar().getThemedContext();

        AppBarLayout appBarLayout = findViewById(R.id.app_toolbar);
        appBarLayout.setExpanded(true);
        appBarLayout.setOutlineProvider(null);
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
        backdropImage = findViewById(R.id.movie_backdrop);
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
        Bundle bundle = new Bundle();
        bundle.putInt(MOVIE_KEY, movieID);
        bundle.putString(MOVIE_SYNOPSIS, currentMovie.getMovieOverview());
        bundle.putString(MOVIE_RELEASE_DATE, currentMovie.getMovieReleaseDate());

        switch (fragmentName) {
            case DASH_DETAILS:
                if(loadedFragment == null) {
                    loadedFragment = new DashDetailsFragment();
                    loadedFragment.setArguments(bundle);
                    transaction.add(R.id.content_for_fragment, loadedFragment).commit();
                } else {
                    if (!(fm.findFragmentById(R.id.content_for_fragment) instanceof DashDetailsFragment)) {
                        loadedFragment = new DashDetailsFragment();
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
                    loadedFragment.setArguments(bundle);
                    transaction.add(R.id.content_for_fragment, loadedFragment).commit();
                } else {
                    if (!(fm.findFragmentById(R.id.content_for_fragment) instanceof DashReviewsFragment)) {
                        loadedFragment = new DashReviewsFragment();
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
                    loadedFragment.setArguments(bundle);
                    transaction.add(R.id.content_for_fragment, loadedFragment).commit();
                } else {
                    if (!(fm.findFragmentById(R.id.content_for_fragment) instanceof DashTrailersFragment)) {
                        loadedFragment = new DashTrailersFragment();
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
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(0);
        loadFragment(DASH_DETAILS);
    }

    private void initCreditRecycler() {}

    public int getMovieID() {
        return movieID;
    }

}
