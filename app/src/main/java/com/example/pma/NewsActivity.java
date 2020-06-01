package com.example.pma;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NewsActivity extends AppCompatActivity {
    ListView listView;
    String mTitle[] = {"Promena linije 1", "Exit red voznje", "Zatvoren most Slobode"};
    String mDescription[] = {"Linija 1 menja vreme polazaka od datuma 12.06.2020", "Za vreme trajanja exita Linija 9 menja raspored polazaka", "Zbog trke na Mišeluku Most Slobode biće zatvoren za gradski saobraćaj"};

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        listView = findViewById(R.id.news_list);
        // now create an adapter class
        MyAdapter adapter = new MyAdapter(this, mTitle, mDescription);
        listView.setAdapter(adapter);
    }

        class MyAdapter extends ArrayAdapter<String> {

            Context context;
            String rTitle[];
            String rDescription[];

            MyAdapter(Context c, String title[], String description[]) {
                super(c, R.layout.news_row, R.id.title, title);
                this.context = c;
                this.rTitle = title;
                this.rDescription = description;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View row = layoutInflater.inflate(R.layout.news_row, parent, false);

                TextView myTitle = row.findViewById(R.id.title);
                TextView myDescription = row.findViewById(R.id.description);

                // now set our resources on views
                myTitle.setText(rTitle[position]);
                myDescription.setText(rDescription[position]);
                return row;
            }
        }
}






















