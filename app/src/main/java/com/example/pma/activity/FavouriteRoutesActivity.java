package com.example.pma.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.pma.R;
import com.example.pma.adapter.SimpleRouteRecyclerViewAdapter;
import com.example.pma.database.DBContentProvider;
import com.example.pma.database.RouteSQLiteHelper;
import com.example.pma.model.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavouriteRoutesActivity extends AppCompatActivity {

    private boolean mTwoPane;
    private static final String AUTHORITY = "com.example.pma";
    private static final String ROUTE_PATH = "route";

    private List<Route> routeList = new ArrayList<>();
    private List<Route> newRouteList = new ArrayList<>();

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

        // Load all routes from database and take only ones from pref.getAll map
        Uri uri = Uri.parse(DBContentProvider.CONTENT_URI_ROUTE + "/");

        String[] allColumns = {RouteSQLiteHelper.COLUMN_ID, RouteSQLiteHelper.COLUMN_NAME, RouteSQLiteHelper.COLUMN_DESCRIPTION};

        Cursor cursor = FavouriteRoutesActivity.this.getContentResolver().query(uri, allColumns, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            createRoutes(cursor);
            cursor.moveToNext();
        }
        cursor.close();

        Log.d("FAVORITES", "lista.size + " + routeList.size());
        Map<String, ?> prefs = pref.getAll();
        for(Route r: routeList) {
            if(prefs.containsKey(r.getId().toString())) {
                newRouteList.add(r);
            }
        }
        Log.d("FAVORITES", "newRouteList.size + " + newRouteList.size());
        recyclerView.setAdapter(new SimpleRouteRecyclerViewAdapter(this, newRouteList, mTwoPane));
    }


    private void createRoutes(Cursor cursor) {
        Route route = new Route();
        route.setId(cursor.getInt(0));
        route.setName(cursor.getString(1));
        route.setDescription(cursor.getString(2));
        routeList.add(route);
    }
}
