package com.example.pma.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.widget.RelativeLayout;

import com.example.pma.R;
import com.example.pma.directionHelper.FetchURL;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
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
    //private Route route;

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
    public GoogleMap mMap;
    private View mapView;
    private LocationManager locationManager;
    private MySupportMapFragment mSupportMapFragment;

    private MarkerOptions place1, place2;
    private List<MarkerOptions> markerOptionsList = new ArrayList<>();
    public Polyline currentPolyline;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "on create RouteDetailFragment ");


        if (getArguments().containsKey(ARG_ROUTE_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            Log.e(TAG, "ARG_ROUTE_ID: " +  getArguments().getInt(ARG_ROUTE_ID));



            Activity activity = this.getActivity();
            Toolbar toolbarDetail = activity.findViewById(R.id.toolbar_detail);
            if (toolbarDetail != null) {
                Log.e(TAG, "route.content " + getArguments().getString("route_name"));

                toolbarDetail.setTitle("Linija " + getArguments().getString("route_name"));
                toolbarDetail.setSubtitle(getArguments().getString("route_description"));
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.route_detail, container, false);

        // TODO: uzeti sve stanice za markere, a za polyline prvu i poslednju
        place1 = new MarkerOptions().position(new LatLng(45.237077, 19.826358)).title("NARODNOG FRONTA - OKRETNICA").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
        place2 = new MarkerOptions().position(new LatLng( 45.248134, 19.849265)).title("STRAŽILOVSKA - URBIS").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
        new FetchURL(getActivity()).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");

        markerOptionsList.add(place1);
        markerOptionsList.add(place2);

        Activity activity = this.getActivity();
        final NestedScrollView nestedScrollView = activity.findViewById(R.id.route_detail_container);

        mSupportMapFragment = (MySupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if(mSupportMapFragment != null)
            mSupportMapFragment.setListener(new MySupportMapFragment.OnTouchListener() {
                @Override
                public void onTouch() {
                    nestedScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });

        //SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
        //        .findFragmentById(R.id.map);

        mSupportMapFragment.getMapAsync(this);
        mapView = mSupportMapFragment.getView();

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
            mMap.getUiSettings().setZoomControlsEnabled(true);

            // my location button set on right bottom position
            if (mapView != null &&
                    mapView.findViewById(Integer.parseInt("1")) != null) {
                // Get the button view
                View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                // and next place it, on bottom right (as Google Maps app)
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        locationButton.getLayoutParams();

                // position on right bottom
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 0, 30, 260);
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

        // TODO: za sada zakucano za jednu stanicu

//        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
//        options.add(new LatLng(45.237077, 19.826358));
//        options.add(new LatLng( 45.248134, 19.849265));
//        mMap.addPolyline(options);

//        LatLng busStop = new LatLng(45.238842, 19.833227);
//        mMap.addMarker(new MarkerOptions().position(busStop).title("NARODNOG FRONTA - BALZAKOVA")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
//        mMap.getUiSettings().setMapToolbarEnabled(false);

        // mMap.setMyLocationEnabled(true);
        // mMap.setOnMyLocationButtonClickListener(this);
        // mMap.setOnMyLocationClickListener(this);

        // Add a marker in Sydney, Australia, and move the camera.
        // LatLng sydney = new LatLng(-34, 151);
        // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.addMarker(place1);
        mMap.addMarker(place2);

        showAllMarkers();

    }

    private void showAllMarkers() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(MarkerOptions m: markerOptionsList) {
            builder.include(m.getPosition());
        }

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.30);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cu);
    }

    private String getUrl(LatLng origin, LatLng destination, String directionMode){
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
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
        //Toast.makeText(getActivity(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
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

