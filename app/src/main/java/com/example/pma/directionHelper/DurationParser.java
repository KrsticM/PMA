package com.example.pma.directionHelper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DurationParser extends AsyncTask<String, Integer, String> {
    TaskLoadedCallback taskCallback;
    TaskLoadedCallbackBus taskLoadedCallbackBus;
    String directionMode = "driving";
    Boolean mode;
    String stationName;

    public DurationParser(Context mContext, String directionMode, Boolean mode) {
        this.taskCallback = (TaskLoadedCallback) mContext;
        this.taskLoadedCallbackBus = (TaskLoadedCallbackBus)mContext;
        this.directionMode = directionMode;
        this.mode = mode;
    }

    public DurationParser(Context mContext, String directionMode, Boolean mode, String stationName) {
        this.taskCallback = (TaskLoadedCallback) mContext;
        this.taskLoadedCallbackBus = (TaskLoadedCallbackBus)mContext;
        this.directionMode = directionMode;
        this.mode = mode;
        this.stationName = stationName;
    }

    // Parsing the data in non-ui thread
    @Override
    protected String doInBackground(String... jsonData) {

        JSONObject jObject = null;
        try {
            jObject = new JSONObject(jsonData[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jRoutes;
        JSONArray jLegs;
        String distance = "";
        String duration = "";
        try {
            jRoutes = jObject.getJSONArray("routes");
            jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("legs");
            JSONObject distanceJSON = ((JSONObject)jLegs.get(0)).getJSONObject("distance");
            Log.d("DEBUG", distanceJSON.getString("text"));
            distance = distanceJSON.getString("text");
            JSONObject durationJSON = ((JSONObject)jLegs.get(0)).getJSONObject("duration");
            Log.d("DEBUG", durationJSON.getString("text"));
            duration = durationJSON.getString("text");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return distance + ";" + duration;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(String result) {
        Log.e("DEBUG", "onPostExecute");
        if(mode) {
            taskCallback.onTaskDone(result);
        } else {
            taskLoadedCallbackBus.onTaskDoneBus(result, stationName);
        }


    }
}
