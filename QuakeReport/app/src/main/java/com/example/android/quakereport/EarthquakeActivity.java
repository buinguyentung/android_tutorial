/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class EarthquakeActivity extends AppCompatActivity {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();

    Context context;

    /** URL to query the USGS dataset for earthquake information */
    private static final String USGS_REQUEST_URL =
            //"https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2018-01-01&endtime=2018-12-01&minmagnitude=7";
			"https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=6&limit=10";
			
    ArrayList<Earthquake> earthquakes;
    EarthquakeAdapter adapter;
    ListView earthquakeListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        context = this;

        // THE VERY BEGIN
        // Create a fake list of earthquake locations.
        //ArrayList<Earthquake> earthquakes = new ArrayList<>();
//        earthquakes.add(new Earthquake("7.2","San Francisco", "Feb 2, 2016"));
//        earthquakes.add(new Earthquake("6.1","London", "Feb 2, 2016"));
//        earthquakes.add(new Earthquake("3.9","Tokyo", "Feb 2, 2016"));
//        earthquakes.add(new Earthquake("5.4","Mexico City", "Feb 2, 2016"));
//        earthquakes.add(new Earthquake("2.8","Moscow", "Feb 2, 2016"));
//        earthquakes.add(new Earthquake("4.9","Rio de Janeiro", "Feb 2, 2016"));
//        earthquakes.add(new Earthquake("1.6","Paris", "Feb 2, 2016"));

        earthquakes = new ArrayList<Earthquake>();

        // Find a reference to the {@link ListView} in the layout
        earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new {@link ArrayAdapter} of earthquakes
        adapter = new EarthquakeAdapter(context, earthquakes);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(adapter);

        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Earthquake currentEarthquake = adapter.getItem(position);
                Uri earthquakeUri = Uri.parse(currentEarthquake.getmUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);
                startActivity(websiteIntent);
            }
        });

        // Kick off an {@link AsyncTask} to perform the network request
        EarthquakeAsyncTask task = new EarthquakeAsyncTask();
        task.execute();
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private class EarthquakeAsyncTask extends AsyncTask<URL, Void, Void> {

        @Override
        protected Void doInBackground(URL... urls) {
            earthquakes = QueryUtils.fetchEarthquakeData(USGS_REQUEST_URL);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.refreshEarthquakeList(earthquakes);
        }

    }
}
