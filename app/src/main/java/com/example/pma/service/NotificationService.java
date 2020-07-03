package com.example.pma.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.pma.R;
import com.example.pma.activity.MainActivity;
import com.example.pma.database.DBContentProvider;
import com.example.pma.database.RouteSQLiteHelper;
import com.example.pma.model.BusStop;
import com.example.pma.model.Position;
import com.example.pma.model.Positions;
import com.example.pma.network.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationService extends JobService {

    private boolean jobDone = false;
    @Override
    public boolean onStartJob(JobParameters params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "test",
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
        doBackgroundWork(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobDone = true;
        return true;
    }

    private void doBackgroundWork(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Koje su stanice oznacene
                final ArrayList<String> already_notified = new ArrayList();
                while(!jobDone){

                    GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
                    Call<Positions> call = service.getPosition4();

                    call.enqueue(new Callback<Positions>() {
                        @Override
                        public void onResponse(Call<Positions> call, Response<Positions> response) {
                            Positions positions = response.body();

                            ArrayList<Position> bus1 = new ArrayList<>(positions.getPositions());
                            SharedPreferences pref = getBaseContext().getSharedPreferences("Alarms", 0);
                            Map<String, String> alarms = (Map<String, String>) pref.getAll();

                            for (String id : alarms.keySet()) {
                                // uzeti podatke o stanici
                                Uri uri = Uri.parse(DBContentProvider.CONTENT_URI_ROUTE + "/4/stop");

                                String[] allColumns = {RouteSQLiteHelper.COLUMN_ID, RouteSQLiteHelper.COLUMN_NAME, RouteSQLiteHelper.COLUMN_LAT, RouteSQLiteHelper.COLUMN_LNG};

                                Cursor cursor = getBaseContext().getContentResolver().query(uri, allColumns, null, null, null);

                                cursor.moveToFirst();
                                while (!cursor.isAfterLast()) {
                                    if (Integer.valueOf(id) == cursor.getInt(0)) {
                                        String name = cursor.getString(1);
                                        Double positionX = cursor.getDouble(2);
                                        Double positionY = cursor.getDouble(3);

                                        for (int i = 0; i < bus1.size(); i++) {
                                            double posX = bus1.get(i).getX();
                                            double posY = bus1.get(i).getY();

                                            //Racunaj udaljenost izmedju stanice i trenutne pozicije autobusa
                                            double distance = calculateDistance(posX, posY, positionX, positionY); // Racuna udaljenost u tackama
                                            double time_distance = (distance / 8.33333) / 60; // Distanca u sekundama racunamo da se krece prosecno 30km/h
                                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                                            double settings_distance = Double.parseDouble(preferences.getString("distance_meters", "0 m").split(" ")[0]);
                                            double setting_time_distance = Double.parseDouble(preferences.getString("time_distance", "0 min").split(" ")[0]);
                                            if (settings_distance > distance || setting_time_distance > time_distance) {

                                                //Ako je vec za trenutni bus i trenutnu stanicu poslata notifikacija preskoci
                                                if (already_notified.contains(cursor.getInt(0) + "-" + i)) {
                                                    break;
                                                } else {
                                                    already_notified.add(cursor.getInt(0) + "-" + i);

                                                    Intent notificationIntent = new Intent(getBaseContext(), MainActivity.class);
                                                    PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),
                                                            0, notificationIntent, 0);
                                                    NotificationCompat.Builder mBuilder =
                                                            new NotificationCompat.Builder(getApplicationContext(), "test")
                                                                    .setContentIntent(pendingIntent)
                                                                    .setOnlyAlertOnce(true)
                                                                    .setSmallIcon(R.drawable.ic_bus)
                                                                    .setContentTitle("Stanica: " + name)
                                                                    .setContentText("Autobus je na udaljenosti od " + String.format("%.1f", distance) + " metara odnosno " + String.format("%.1f", time_distance) + " min");
                                                    NotificationManager mNotificationManager =
                                                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                                    mNotificationManager.notify(Integer.valueOf(id), mBuilder.build());

                                                //Ako su sva tri busa prosla kroz sve stanice isprazni niz
                                                if (alarms.size()*3 == already_notified.size()) {
                                                    already_notified.clear();
                                                    //Zbog testiranja necemo prekidati rad niti
                                                    //jobDone = true;
                                                }
                                                }
                                            }
                                        }
                                    }
                                    cursor.moveToNext();
                                }
                                cursor.close();
                            }
                        }

                        @Override
                        public void onFailure(Call<Positions> call, Throwable t) {
                            Log.e("ERROR:", "Something went wrong...Please try later!");
                        }
                    });

                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                jobFinished(params, false);
            }
        }).start();
    }

    public double calculateDistance(double x1, double y1, double x2, double y2) {
        Location locationA = new Location("point A");
        locationA.setLatitude(x1);
        locationA.setLongitude(y1);

        Location locationB = new Location("point B");
        locationB.setLatitude(x2);
        locationB.setLongitude(y2);

        double distance = locationA.distanceTo(locationB);
        return distance;
    }
}