package com.example.pma.activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import com.example.pma.R;
import com.example.pma.adapter.SimpleRouteRecyclerViewAdapter;
import com.example.pma.database.DBContentProvider;
import com.example.pma.database.RouteSQLiteHelper;
import com.example.pma.model.BusStop;
import com.example.pma.model.Route;
import com.example.pma.model.Timetable;
import com.example.pma.network.RetrofitClientInstance;
import com.example.pma.service.GetDataService;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final String AUTHORITY = "com.example.pma";
    private static final String ROUTE_PATH = "route";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * Reference to drawer.
     */
    private DrawerLayout drawer;

    /**
     * Reference to navigation view.
     */
    private NavigationView navigationView;

    private ProgressDialog progressDoalog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.e(TAG, "\t\t onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (findViewById(R.id.route_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        progressDoalog = new ProgressDialog(MainActivity.this);
        progressDoalog.setMessage("Loading....");
        progressDoalog.show();

        /*Create handle for the RetrofitInstance interface*/
        GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        Call<List<Route>> call = service.getAllRoutes();
        call.enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                progressDoalog.dismiss();
                generateDataList(response.body());
            }

            @Override
            public void onFailure(Call<List<Route>> call, Throwable t) {
                progressDoalog.dismiss();
                Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
       }

    /*Method to generate List of data using RecyclerView with custom adapter*/
    private void generateDataList(List<Route> routeList) {
        // Save data to the database
        Log.e(TAG, "generateDataList");

        RouteSQLiteHelper dbHelper = new RouteSQLiteHelper(MainActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onUpgrade(db,1,2);
        {
            for(Route r : routeList) {
                ContentValues entry = new ContentValues();
                entry.put(RouteSQLiteHelper.COLUMN_ID, r.getId());
                entry.put(RouteSQLiteHelper.COLUMN_NAME, r.getName());
                entry.put(RouteSQLiteHelper.COLUMN_DESCRIPTION, r.getDescription());
                MainActivity.this.getContentResolver().insert(DBContentProvider.CONTENT_URI_ROUTE, entry);

                for(BusStop bs : r.getBusStops()) {
                    Log.e(TAG, bs.getName());

                    ContentValues busStopEntry = new ContentValues();
                    busStopEntry.put(RouteSQLiteHelper.COLUMN_ID, bs.getId());
                    busStopEntry.put(RouteSQLiteHelper.COLUMN_NAME, bs.getName());
                    busStopEntry.put(RouteSQLiteHelper.COLUMN_LAT, bs.getLat());
                    busStopEntry.put(RouteSQLiteHelper.COLUMN_LNG, bs.getLng());
                    busStopEntry.put(RouteSQLiteHelper.COLUMN_ROUTE_ID, r.getId());

                    Uri uri = Uri.parse("content://" + AUTHORITY + "/" + ROUTE_PATH + "/" + r.getId().toString() + "/stop");
                    MainActivity.this.getContentResolver().insert(uri, busStopEntry);
                }
                for(Timetable tt: r.getTimetables()) {
                    ContentValues timetableEntry = new ContentValues();
                    timetableEntry.put(RouteSQLiteHelper.COLUMN_ID, tt.getId());
                    timetableEntry.put(RouteSQLiteHelper.COLUMN_TYPE, tt.getType());
                    timetableEntry.put(RouteSQLiteHelper.COLUMN_CONTENT, tt.getContent());
                    timetableEntry.put(RouteSQLiteHelper.COLUMN_ROUTE_ID, r.getId());

                    Uri uri = Uri.parse("content://" + AUTHORITY + "/" + ROUTE_PATH + "/" + r.getId().toString() + "/timetable");
                    MainActivity.this.getContentResolver().insert(uri, timetableEntry);
                }
            }
        }

        db.close();

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView, routeList);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, List<Route> routeList) {
        recyclerView.setAdapter(new SimpleRouteRecyclerViewAdapter(this, routeList, mTwoPane));
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //Toast.makeText(this, "on Options Item Selected", Toast.LENGTH_SHORT).show();
        int id = item.getItemId();

        if(id == android.R.id.home){ // use android.R.id
            drawer.openDrawer(Gravity.LEFT);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Toast.makeText(this, "Navigation item selected", Toast.LENGTH_SHORT).show();
        switch (menuItem.getItemId()) {
            case R.id.all_routs:
                // Toast.makeText(this, "Kliknuto sve rute", Toast.LENGTH_SHORT).show();
                Intent intent= new Intent(MainActivity.this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.settings:
                // Toast.makeText(this, "Kliknuto podesavanje", Toast.LENGTH_SHORT).show();
                Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intentSettings);
                break;
            case R.id.warnings:
                Intent intentNews = new Intent (MainActivity.this, NewsActivity.class);
                startActivity(intentNews);
                break;
            case R.id.favourite_routes:
                Intent intentFavouriteRoutes = new Intent(MainActivity.this, FavouriteRoutesActivity.class);
                startActivity(intentFavouriteRoutes);
                break;
        }
        return false;
    }
}
