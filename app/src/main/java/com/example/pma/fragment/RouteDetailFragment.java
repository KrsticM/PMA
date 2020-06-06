package com.example.pma.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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

import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pma.R;
import com.example.pma.database.DBContentProvider;
import com.example.pma.database.RouteSQLiteHelper;
import com.example.pma.directionHelper.FetchURL;
import com.example.pma.model.BusStop;
import com.example.pma.model.Position;
import com.example.pma.network.RetrofitClientInstance;
import com.example.pma.service.GetDataService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteDetailFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, LocationListener, GoogleMap.OnMarkerClickListener {

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
     * Google maps
     */
    public GoogleMap mMap;
    private View mapView;
    private LocationManager locationManager;
    private MySupportMapFragment mSupportMapFragment;

    private List<MarkerOptions> markerOptionsList = new ArrayList<>();
    public Polyline currentPolyline;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private List<BusStop> busStops = new ArrayList<>();

    MarkerOptions stop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate() RouteDetailFragment");
        timerHandler.postDelayed(timerRunnable, 0); // TIMER


        if (getArguments().containsKey(ARG_ROUTE_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            Log.e(TAG, "ARG_ROUTE_ID: " + getArguments().getInt(ARG_ROUTE_ID));


            Activity activity = this.getActivity();
            Toolbar toolbarDetail = activity.findViewById(R.id.toolbar_detail);
            if (toolbarDetail != null) {
                Log.e(TAG, "route.content " + getArguments().getString("route_name"));

                toolbarDetail.setTitle("Linija " + getArguments().getString("route_name"));
                toolbarDetail.setSubtitle(getArguments().getString("route_description"));
            }

            Uri uri = Uri.parse(DBContentProvider.CONTENT_URI_ROUTE + "/" + getArguments().getInt(ARG_ROUTE_ID) + "/stop");

            String[] allColumns = {RouteSQLiteHelper.COLUMN_ID, RouteSQLiteHelper.COLUMN_NAME, RouteSQLiteHelper.COLUMN_LAT, RouteSQLiteHelper.COLUMN_LNG};

            Cursor cursor = getActivity().getContentResolver().query(uri, allColumns, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                createBusStop(cursor);
                cursor.moveToNext();
            }
            cursor.close();

        }

    }

    private void createBusStop(Cursor cursor) {
        BusStop bs = new BusStop();
        bs.setId(cursor.getInt(0));
        bs.setName(cursor.getString(1));
        bs.setLat(cursor.getDouble(2));
        bs.setLng(cursor.getDouble(3));
        Log.e(TAG, "DEBUG: \t\t\t DODATO STAJALISTE" + bs.getName());
        busStops.add(bs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.route_detail, container, false);

        // Postavljanje polyline-ova
        for (int i = 0; i < busStops.size() - 1; i++) {
            MarkerOptions startStation = new MarkerOptions().position(new LatLng(busStops.get(i).getLat(), busStops.get(i).getLng())).title(busStops.get(i).getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop));
            MarkerOptions stopStation = new MarkerOptions().position(new LatLng(busStops.get(i + 1).getLat(), busStops.get(i + 1).getLng())).title(busStops.get(i + 1).getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop));
            new FetchURL(getActivity()).execute(getUrl(startStation.getPosition(), stopStation.getPosition(), "driving"), "driving");
            markerOptionsList.add(startStation);

        }

        Activity activity = this.getActivity();
        final NestedScrollView nestedScrollView = activity.findViewById(R.id.route_detail_container);

        mSupportMapFragment = (MySupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mSupportMapFragment != null)
            mSupportMapFragment.setListener(new MySupportMapFragment.OnTouchListener() {
                @Override
                public void onTouch() {
                    nestedScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });

        mSupportMapFragment.getMapAsync(this);
        mapView = mSupportMapFragment.getView();

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.e(TAG, "onMapReady");

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);

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

            // preuzimanje lokacije uredjaja
            Location myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // NETWORK PROVIDER AKO JE NA TELEFONU
            Log.e(TAG, "myLocation: " + myLocation);
            getAddressName(myLocation); // izvlaci adresu iz lokacije
            Log.w(TAG, "DEBUG: \t\t\t\t " + myLocation.getLatitude() + ", " + myLocation.getLongitude());
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            MarkerOptions start = new MarkerOptions().position(latLng); // oznacavanje lokacije uredjaja

            // pronalazak najblize stanice
            float smallestDistance = -1;
            List<Location> locations = new ArrayList<>();
            for (int i = 0; i < busStops.size(); i++) { // prolazak kroz sve stanice i preuzimanje njihovih lokacija
                Location temp = new Location(LocationManager.NETWORK_PROVIDER);
                temp.setLongitude(busStops.get(i).getLng());
                temp.setLatitude(busStops.get(i).getLat());
                locations.add(temp);
            }
            Location closestLocation = null;
            for (Location location : locations) { // prolazak kroz sve lokacije i trazenje najblize u odnosu na lokaciju uredjaja
                Log.w(TAG, "lokacija: " + location.getLatitude() + ", " + location.getLongitude());
                float distance = myLocation.distanceTo(location);
                if (smallestDistance == -1 || distance < smallestDistance) {
                    closestLocation = location;
                    smallestDistance = distance;
                }
            }

            Log.w(TAG, "najbliza: " + closestLocation.getLatitude() + ", " + closestLocation.getLongitude());
            // oznacavanje najblize stanice
            MarkerOptions stop = new MarkerOptions().position(new LatLng(closestLocation.getLatitude(), closestLocation.getLongitude()));

            // preuzimanje najblize stanice kako bi preuzeli naziv
            BusStop closestBusStop = null;
            for (BusStop bs : busStops) {
                if (bs.getLat().equals(closestLocation.getLatitude()) && bs.getLng().equals(closestLocation.getLongitude())) {
                    closestBusStop = bs;
                    break;
                }
            }

            // postavljanje naziva najblize stanice na ui
            TextView busStationName = getActivity().findViewById(R.id.bus_station_name);
            if (closestBusStop != null) {
                busStationName.setText(closestBusStop.getName());
            }

            // crtanje polyline-a od lokacije uredjaja do najblize stanice
            new FetchURL(getActivity()).execute(getUrl(start.getPosition(), stop.getPosition(), "walking"), "walking");
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        for (BusStop bs : busStops) {
            MarkerOptions startStation = new MarkerOptions().position(new LatLng(bs.getLat(), bs.getLng())).title(bs.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop)).infoWindowAnchor(0.5f, 0.5f);


            Marker mark = mMap.addMarker(startStation);
            mark.setTag(bs.getId());
        }

        showAllMarkers();
        mMap.setOnMarkerClickListener(this);

    }

    private void showAllMarkers() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (MarkerOptions m : markerOptionsList) {
            builder.include(m.getPosition());
        }

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.20);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cu);
    }

    private String getUrl(LatLng origin, LatLng destination, String directionMode) {
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
        Log.d(TAG, "usao u onLocationChanged() ");
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);

        // preuzimanje naziva adrese iz lokacije
        getAddressName(location);

        // crtanje polyline-a od lokacije uredjaja to markera
        if (stop != null) {
            MarkerOptions start = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()));
            new FetchURL(getActivity()).execute(getUrl(start.getPosition(), stop.getPosition(), "walking"), "walking");
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
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        // Toast.makeText(getActivity(), "naziv: " + marker.getTitle(), Toast.LENGTH_SHORT).show();

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            // preuzimanje lokacije uredjaja i oznacavanje lokacije
            Location myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // NETWORK PROVIDER AKO JE NA TELEFONU
            MarkerOptions start = new MarkerOptions().position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));

            // oznacavanje lokacije markera
            stop = new MarkerOptions().position(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));

            // brisanje starog polyline-a
            if (currentPolyline != null) {
                currentPolyline.remove();
            }

            // crtanje polyline-a od lokacije uredjaja to markera
            new FetchURL(getActivity()).execute(getUrl(start.getPosition(), stop.getPosition(), "walking"), "walking");

            // preuzianje i postavljanje naziva oznacene stanice
            TextView busStationName = getActivity().findViewById(R.id.bus_station_name);
            busStationName.setText(marker.getTitle());

            // preuzimanje naziva adrese iz lokacije
            getAddressName(myLocation);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }


        return false;
    }

    private void getAddressName(Location myLocation) {
        if (getActivity() != null) {
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1);

                String address = addresses.get(0).getAddressLine(0); // vrati Ulica, Grad, Drzava
                // preuzimanje samo ulice
                String[] splited = address.split(",");
                String addressName = splited[0];
                // postavljanje naziva ulice (ui)
                TextView yourLocation = getActivity().findViewById(R.id.your_location);
                yourLocation.setText(addressName);
                Log.d(TAG, "address " + address);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void setDurationDistance(String toString) {
        if (getActivity() != null) {
            TextView duration_distance = getActivity().findViewById(R.id.duration_distance);
            String[] list = toString.split(";");
            duration_distance.setText("Šetaj " + list[1] + " (" + list[0] + ")");
        }

    }


    /**
     * Demonstrates customizing the info window and/or its contents.
     */
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;


        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mMap.setOnInfoWindowClickListener(this);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }


        private void render(Marker marker, View view) {
            ImageButton imageButton = view.findViewById(R.id.badge);
            Log.e("OMG", "aaa " + imageButton);

            Log.d("OMG", "id -> " + marker.getId() + "tag " + marker.getTag());
            SharedPreferences pref = getActivity().getSharedPreferences("Alarms", 0); // 0 - for private mode
            if (pref.contains(marker.getTag().toString())) {
                Log.e("OMG", "podesen alarm za ovu stanicu");
                imageButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#008577")));
            } else {
                Log.e("OMG", "nije podesen alarm za stanicu");
                imageButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
            }

            String title = marker.getTitle();
            TextView titleUi = view.findViewById(R.id.title);
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

        }

        @Override
        public void onInfoWindowClick(Marker marker) {
            SharedPreferences pref = getActivity().getSharedPreferences("Alarms", 0); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            ImageButton imageButton = mWindow.findViewById(R.id.badge);

            if (pref.contains(marker.getTag().toString())) { // It's alarm, do unalarm
                Log.e("OMG", "alarm je, nek nije vise ");
                editor.remove(marker.getTag().toString()); // Remove from fav
                imageButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
                Toast.makeText(getActivity(), "Alarm je isključen.", Toast.LENGTH_SHORT).show();

            } else {
                Log.e("OMG", "add to alarm");
                editor.putString(marker.getTag().toString(), marker.getTitle()); // Add to alarm
                imageButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#008577")));
                Toast.makeText(getActivity(), "Alarm je uključen.", Toast.LENGTH_SHORT).show();

            }
            editor.commit();
            marker.hideInfoWindow();
            marker.showInfoWindow();

        }
    }

    // TIMER
    long startTime = 0;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
    Marker busPositionMarker;
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            Log.e("TIMER", "time");
            Call<Position> call = service.getPosition4();

            call.enqueue(new Callback<Position>() {
                @Override
                public void onResponse(Call<Position> call, Response<Position> response) {
                    MarkerOptions busPositionMarkerOptions = new MarkerOptions().position(new LatLng(response.body().getX(), response.body().getY()))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));

                    if (busPositionMarker == null) {
                        busPositionMarker = mMap.addMarker(busPositionMarkerOptions);
                        Log.e("MARKER", "Dodat");
                    } else {
                        busPositionMarker.remove();
                        busPositionMarker = mMap.addMarker(busPositionMarkerOptions);

                        Log.e("MARKER", "promenjena pozicija");
                    }

                }

                @Override
                public void onFailure(Call<Position> call, Throwable t) {
                    Log.e("ERROR:", "Something went wrong...Please try later!");
                    // Toast.makeText(getActivity(), "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
                }
            });


            timerHandler.postDelayed(this, 900);
        }
    };
}



