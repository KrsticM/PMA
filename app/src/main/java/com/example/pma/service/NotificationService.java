package com.example.pma.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.pma.R;
import com.example.pma.activity.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("DDD", "Job started");
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                doBackgroundWork();
            }
        }, 0, 10000);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void doBackgroundWork() {
        Log.d("DDD","Calculate distance");
        double distance = calculateDistance(45.238121, 19.820861,45.239579, 19.825404); // Default value distance is 391m
        Log.d("DDD", String.valueOf(distance));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        double settings_distance = Double.parseDouble(preferences.getString("distance_meters", "0 m").split(" ")[0]);
        if ( settings_distance > distance) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext(),"test")
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(R.drawable.ic_bus)
                            .setContentTitle("Stanica:")
                            .setContentText("Autobus je na udaljenosti od " + String.valueOf(distance));
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(101, mBuilder.build());
        }
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
