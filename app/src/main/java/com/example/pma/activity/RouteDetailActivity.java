package com.example.pma.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pma.R;
import com.example.pma.directionHelper.TaskLoadedCallback;
import com.example.pma.fragment.RouteDetailFragment;
import com.example.pma.database.DBContentProvider;
import com.example.pma.database.RouteSQLiteHelper;
import com.example.pma.model.Route;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainActivity}.
 */
public class RouteDetailActivity extends AppCompatActivity implements TaskLoadedCallback {


    private static final String TAG = "RouteDetailActivity";

    private BottomSheetBehavior mBottomSheetBehavior;

    private Route route;

    private Uri uri;

    private RouteDetailFragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("ROTATE: ", "onCreate RouteDetailActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        if (toolbar != null) {
            toolbar.setTitle("DSA");

        }
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        // back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        uri = Uri.parse(DBContentProvider.CONTENT_URI_ROUTE + "/" + getIntent().getIntExtra(RouteDetailFragment.ARG_ROUTE_ID, 0));

        String[] allColumns = {RouteSQLiteHelper.COLUMN_ID, RouteSQLiteHelper.COLUMN_NAME, RouteSQLiteHelper.COLUMN_DESCRIPTION};

        Cursor cursor = getContentResolver().query(uri, allColumns, null, null, null);
        cursor.moveToFirst();
        route = createRoute(cursor);
        cursor.close();

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            Log.e("ROTATE: ", "savedInstanceStateIsNull");
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(RouteDetailFragment.ARG_ROUTE_ID, route.getId());
            arguments.putString("route_name", route.getName());
            arguments.putString("route_description", route.getDescription());

            fragment = new RouteDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().add(R.id.route_detail_container, fragment).commit();
        } else {
            fragment = (RouteDetailFragment)getSupportFragmentManager().findFragmentById(R.id.route_detail_container);
            Log.e(TAG, "PRONASLI FRAGMENT");
        }

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setHideable(false);


        Button timeTableButton = findViewById(R.id.time_table_button);
        timeTableButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Toast.makeText(RouteDetailActivity.this, "Clicked", Toast.LENGTH_LONG).show();
                Intent activity2Intent = new Intent(RouteDetailActivity.this, TimeTableActivity.class);
                // Toast.makeText(RouteDetailActivity.this, route.content, Toast.LENGTH_LONG).show();
                activity2Intent.putExtra("route_id", route.getId());
                activity2Intent.putExtra("route_name", route.getName());
                activity2Intent.putExtra("route_description", route.getDescription());
                startActivity(activity2Intent);
            }
        });

        final FloatingActionButton fab = findViewById(R.id.fab);
        SharedPreferences pref = getSharedPreferences("Favorites", 0); // 0 - for private mode
        if(pref.contains(route.getId().toString())) {
            Log.e("zvezdica", "Ruta je omiljena");
            //holder.mImageButton.setImageAlpha(1);
            fab.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ADFFE700")));

        } else {
            Log.e("zvezdica", "Ruta nije omiljena");
            //holder.mImageButton.setImageAlpha(0);
            fab.setImageTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
        }

        fab.setOnClickListener(new View.OnClickListener()   {
            SharedPreferences pref = getSharedPreferences("Favorites", 0); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            public void onClick(View v)  {
                if(pref.contains(route.getId().toString())) { // It's favorite, do unfav
                    Log.e("zvezdica", "kliknuto unfave ");
                    editor.remove(route.getId().toString()); // Remove from fav
                    //holder.mImageButton.setImageResource(R.drawable.round_star_button);
                    fab.setImageTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
                } else {
                    Log.e("Kliknuto", "fav");
                    editor.putString(route.getId().toString(), route.getName().toString()); // Add to fav
                    //holder.mImageButton.setImageResource(R.drawable.silver_line);
                    fab.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ADFFE700")));
                }
                editor.commit();
            }
        });


    }


    private Route createRoute(Cursor cursor) {
        Route route = new Route();
        route.setId(cursor.getInt(0));
        route.setName(cursor.getString(1));
        route.setDescription(cursor.getString(2));
        return route;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskDone(Object... values) {

        if(values[0] instanceof String) {
            Log.d("DDD: ", values[0].toString());
            fragment.setDurationDistance(values[0].toString());
        }
        else {
            fragment.currentPolyline = fragment.mMap.addPolyline((PolylineOptions) values[0]);
        }
    }

}
