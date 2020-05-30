package com.example.pma;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class TimeTableActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ListView listView;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);
        // Show the Up button in the action bar.
        // back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getIntent().getExtras().getString("route"));
            actionBar.setSubtitle(getIntent().getExtras().getString("route_subtitle"));

        }

        // spiner
        Spinner spinner_days = findViewById(R.id.spinner_days);
        ArrayAdapter<CharSequence> spinerAdapter = ArrayAdapter.createFromResource(this, R.array.spinner_days, android.R.layout.simple_spinner_item);
        spinerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_days.setAdapter(spinerAdapter);
        spinner_days.setOnItemSelectedListener(this);


        // timetable
        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this, R.layout.mytextview);
        adapter.add("04:30");
        adapter.add("05:00, 05:27, 05:45");
        adapter.add("06:00, 05:27, 05:45");
        adapter.add("07:00, 05:27, 05:45");
        adapter.add("08:00, 05:27, 05:45");

        listView.setAdapter(adapter);
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
            navigateUpTo(new Intent(this, RouteDetailFragment.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public  void onItemSelected(AdapterView<?> parent, View view, int position, long i) {
        String text = parent.getItemAtPosition(position).toString();
        // Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
