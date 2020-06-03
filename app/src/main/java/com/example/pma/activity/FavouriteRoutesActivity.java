package com.example.pma.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.pma.R;
import com.example.pma.adapter.SimpleRouteRecyclerViewAdapter;

public class FavouriteRoutesActivity extends AppCompatActivity {

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_routes);

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        // Get all routes and only show favorite routes
        SharedPreferences pref = getSharedPreferences("Favorites", 0); // 0 - for private mode
        Log.e("FAVORITES: ", pref.getAll().toString());

        // Load all routes from database and tako only ones from pref.getAll map

        //recyclerView.setAdapter(new SimpleRouteRecyclerViewAdapter(this, // HERE SEND NEW LIST OF ROUTES//, mTwoPane));
    }
}
