package com.example.pma.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pma.R;
import com.example.pma.database.DBContentProvider;
import com.example.pma.database.RouteSQLiteHelper;
import com.example.pma.fragment.RouteDetailFragment;
import com.example.pma.model.BusStop;
import com.example.pma.model.Timetable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimeTableActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ListView listView;
    ArrayAdapter<String> adapter;

    private static final String TAG = "TimeTableActivity";
    private List<Timetable> timetableArrayList = new ArrayList<>();

    private Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);
        // Show the Up button in the action bar.
        // back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Linija " + getIntent().getExtras().getString("route_name"));
            actionBar.setSubtitle(getIntent().getExtras().getString("route_description"));
        }

        // spiner
        Spinner spinner_days = findViewById(R.id.spinner_days);
        ArrayAdapter<CharSequence> spinerAdapter = ArrayAdapter.createFromResource(this, R.array.spinner_days, android.R.layout.simple_spinner_item);
        spinerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_days.setAdapter(spinerAdapter);
        spinner_days.setOnItemSelectedListener(this);

        // pronalazak dana u nedelju
        int day = getDay();
        spinner_days.setSelection(day); // selektuje dropdown u zavisnosti od dana u nedelji

        Integer route_id = getIntent().getExtras().getInt("route_id");
        uri = Uri.parse(DBContentProvider.CONTENT_URI_ROUTE + "/" + route_id.toString() + "/timetable");

        String[] allColumns = {RouteSQLiteHelper.COLUMN_ID, RouteSQLiteHelper.COLUMN_TYPE, RouteSQLiteHelper.COLUMN_CONTENT};

        Cursor cursor = getContentResolver().query(uri, allColumns, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            createTimetable(cursor);
            cursor.moveToNext();
        }
        cursor.close();

        // timetable
        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this, R.layout.mytextview);
        listView.setAdapter(adapter);
    }

    private int getDay() {
        int retVal = -1;
        Calendar calendar = Calendar.getInstance();
        String dayLongName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        Log.d(TAG, "DAN U NEDELJI " + dayLongName);

        if(dayLongName.equals("Monday") || dayLongName.equals("Tuesday") || dayLongName.equals("Wednesday")
                || dayLongName.equals("Thursday") || dayLongName.equals("Friday") ) {
            retVal = 0; // radni dan
        } else if (dayLongName.equals("Saturday")) {
            retVal = 1; // subota
        } else {
            retVal = 2; // nedelja
        }
        return  retVal;
    }

    private void createTimetable(Cursor cursor) {
        Timetable tt = new Timetable();
        BusStop bs = new BusStop();
        tt.setId(cursor.getInt(0));
        tt.setType(cursor.getString(1));
        tt.setContent(cursor.getString(2));
        timetableArrayList.add(tt);
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

        if(!adapter.isEmpty()) {
            adapter = new ArrayAdapter<String>(this, R.layout.mytextview);
        }
        for(Timetable tt: timetableArrayList){
            if(tt.getType().equals(text)) {
                String[] splited = tt.getContent().split(";");
                for(int j = 0; j < splited.length; j++) {
                    adapter.add(splited[j]);
                }
            }
        }
        listView.setAdapter(adapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
