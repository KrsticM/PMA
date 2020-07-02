package com.example.pma.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pma.R;
import com.example.pma.activity.RouteDetailActivity;
import com.example.pma.fragment.RouteDetailFragment;
import com.example.pma.model.Route;

import java.util.List;


public class SimpleRouteRecyclerViewAdapter extends RecyclerView.Adapter<SimpleRouteRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "SimpleRouteRecyclerViewAdapter";

    private final AppCompatActivity mParentActivity;
    private final List<Route> mValues;
    private final boolean mTwoPane;


    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Route route = (Route) view.getTag();

            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putInt(RouteDetailFragment.ARG_ROUTE_ID, route.getId());
                RouteDetailFragment fragment = new RouteDetailFragment();
                fragment.setArguments(arguments);
                mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.route_detail_container, fragment)
                        .commit();
            } else {
                Log.e(TAG, "Route ID: " + route.getId());
                Context context = view.getContext();
                Intent intent = new Intent(context, RouteDetailActivity.class);
                intent.putExtra(RouteDetailFragment.ARG_ROUTE_ID, route.getId());
                context.startActivity(intent);
            }
        }
    };

    public SimpleRouteRecyclerViewAdapter(AppCompatActivity parent, List<Route> routes, boolean twoPane) {
        mValues = routes;
        mParentActivity = parent;
        mTwoPane = twoPane;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mIdView.setText(mValues.get(position).getName());
        holder.mNameView.setText("Linija " + mValues.get(position).getName());
        holder.mDescriptionView.setText(mValues.get(position).getDescription());

        holder.itemView.setTag(mValues.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);

        SharedPreferences pref = mParentActivity.getSharedPreferences("Favorites", 0); // 0 - for private mode
        if(pref.contains(mValues.get(position).getId().toString())) {
            Log.e("Usao", "Its favorite");
            holder.mImageButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ADFFE700")));

        } else {
            Log.e("Usao", "It's not favorite");
            holder.mImageButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
        }

        holder.mImageButton.setOnClickListener(new View.OnClickListener()   {
            SharedPreferences pref = mParentActivity.getSharedPreferences("Favorites", 0); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            public void onClick(View v)  {
                if(pref.contains(mValues.get(position).getId().toString())) { // It's favorite, do unfav
                    Log.e("Kliknuto", "un fav");
                    editor.remove(mValues.get(position).getId().toString()); // Remove from fav
                    //holder.mImageButton.setImageResource(R.drawable.round_star_button);
                    holder.mImageButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
                } else {
                    Log.e("Kliknuto", "fav");
                    editor.putString(mValues.get(position).getId().toString(), mValues.get(position).getName().toString()); // Add to fav
                    //holder.mImageButton.setImageResource(R.drawable.silver_line);
                    holder.mImageButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ADFFE700")));
                }
                editor.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mIdView;
        final TextView mNameView;
        final TextView mDescriptionView;
        final ImageButton mImageButton;

        ViewHolder(View view) {
            super(view);
            mIdView = view.findViewById(R.id.id_text);
            mNameView = view.findViewById(R.id.name);
            mDescriptionView = view.findViewById(R.id.description);
            mImageButton = view.findViewById(R.id.image_button);
        }
    }
}

