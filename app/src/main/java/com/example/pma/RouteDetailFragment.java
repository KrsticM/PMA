package com.example.pma;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.pma.content.Content;
import com.example.pma.model.Route;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RouteDetailFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, LocationListener {

    private static final String TAG = "RouteDetailFragment";

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ROUTE_ID = "route_id";

    /**
     * The content this fragment is presenting.
     */
    private Route route;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RouteDetailFragment() {
    }

    /**
     *
     * Google maps
     */
    private GoogleMap mMap;
    private View mapView;
    private LocationManager locationManager;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ROUTE_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            route = Content.routesMap.get(getArguments().getString(ARG_ROUTE_ID));

            Activity activity = this.getActivity();
            Toolbar toolbarDetail = activity.findViewById(R.id.toolbar_detail);
            if (toolbarDetail != null) {
                Log.e(TAG, "route.content " + route.content);

                toolbarDetail.setTitle(route.content);
                toolbarDetail.setSubtitle("Liman 4 - Centar - Ž. Stanica"); // TODO: change this
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.route_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (route != null) {
            // ((TextView) rootView.findViewById(R.id.route_detail)).setText(route.details);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        Button timeTableButton = (Button) rootView.findViewById(R.id.time_table_button);
        timeTableButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_LONG).show();

                Intent activity2Intent = new Intent(getActivity(), TimeTableActivity.class);
                route = Content.routesMap.get(getArguments().getString(ARG_ROUTE_ID));
                Toast.makeText(getActivity(), route.content, Toast.LENGTH_LONG).show();
                activity2Intent.putExtra("route", route.content);
                activity2Intent.putExtra("route_subtitle", "Liman 4 - Centar - Ž. Stanica"); // TODO: change this
                startActivity(activity2Intent);
            }
        });


        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.e(TAG, "onMapReady");

        //UiSettings settings = mMap.getUiSettings();
        //settings.setZoomControlsEnabled(true);

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            // my location button set on right bottom position
            if (mapView != null &&
                    mapView.findViewById(Integer.parseInt("1")) != null) {
                Log.e(TAG, "USAO ");
                // Get the button view
                View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                // and next place it, on bottom right (as Google Maps app)
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        locationButton.getLayoutParams();

                // position on right bottom
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 0, 30, 30);
            }

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
           // mMap.moveCamera(CameraUpdateFactory.newLatLng());
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // mMap.setMyLocationEnabled(true);
        // mMap.setOnMyLocationButtonClickListener(this);
        // mMap.setOnMyLocationClickListener(this);

        // Add a marker in Sydney, Australia, and move the camera.
        // LatLng sydney = new LatLng(-34, 151);
        // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            String address = addresses.get(0).getAddressLine(0);
            Log.e(TAG, "address " + address);
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getActivity(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }


}

