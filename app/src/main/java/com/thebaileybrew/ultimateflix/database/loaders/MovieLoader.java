package com.thebaileybrew.ultimateflix.database.loaders;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thebaileybrew.ultimateflix.BuildConfig;
import com.thebaileybrew.ultimateflix.database.async.AsyncMovieLoader;
import com.thebaileybrew.ultimateflix.models.Movie;
import com.thebaileybrew.ultimateflix.ui.UltimateFlix;
import com.thebaileybrew.ultimateflix.utils.UrlUtils;
import com.thebaileybrew.ultimateflix.utils.jsonUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MovieLoader {

    public MovieLoader(String sorting, String language, String filterYear, String queryResult) {
        UltimateFlix.updateAsyncParameters(sorting, language, filterYear, queryResult);
    }
}
