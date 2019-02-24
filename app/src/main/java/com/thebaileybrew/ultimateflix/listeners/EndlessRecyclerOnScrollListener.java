package com.thebaileybrew.ultimateflix.listeners;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public final static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    private int previousTotal = 0; //The total number of items in the data set after the last load
    private boolean loading = true; // True if we are still waiting for the las set od data to load

    private int visibleThreshold = 5; // The minimum number of items to have below the current scroll position
    private int firstVisibleItem, visibleItemCount, totalItemCount;

    private int currentPage = 1;

    private GridLayoutManager mGridLayoutManager;

    public EndlessRecyclerOnScrollListener(GridLayoutManager gridLayoutManager) {
        this.mGridLayoutManager = gridLayoutManager;
    }

    @Override
    public void onScrolled (RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount= mGridLayoutManager.getItemCount();
        firstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            //The End is Nigh

            currentPage++;
            onLoadMore(currentPage);
            loading = true;
        }

    }


    public abstract void onLoadMore(int currentPage);
}
