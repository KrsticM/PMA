package com.example.pma.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pma.R;
import com.example.pma.model.News;
import com.example.pma.model.Route;
import com.example.pma.network.RetrofitClientInstance;
import com.example.pma.service.GetDataService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {
    ListView listView;
    List<News> news_list;
    String mTitle[] = {};
    String mDescription[] = {};

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        listView = findViewById(R.id.news_list);

        GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        Call<List<News>> call = service.getAllNews();
        call.enqueue(new Callback<List<News>>() {
            @Override
            public void onResponse(Call<List<News>> call, Response<List<News>> response) {
                Toast.makeText(NewsActivity.this, "Successful!!", Toast.LENGTH_SHORT).show();
                news_list = response.body();
            }

            @Override
            public void onFailure(Call<List<News>> call, Throwable t) {
                Toast.makeText(NewsActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
        // now create an adapter class
        if (news_list != null) {
            for (News n : news_list) {
                mTitle[n.getId()] = n.getTitle();
                mDescription[n.getId()] = n.getContent();
            }
            MyAdapter adapter = new MyAdapter(this, mTitle, mDescription);
            listView.setAdapter(adapter);
        }
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






















