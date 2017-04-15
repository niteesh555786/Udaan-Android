package com.example.geek.udaanv20;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final Map<String, List<String>> PLACES_BY_BEACONS;

    static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("49478:42872", new ArrayList<String>() {{
            add("Heavenly Sandwiches");
            // read as: "Heavenly Sandwiches" is closest
            //7 to the beacon with major 22504 and minor 48827
            add("Green & Green Salads");
            // "Green & Green Salads" is the next closest
            add("Mini Panini");
            // "Mini Panini" is the furthest away
        }});
        placesByBeacons.put("38545:30919", new ArrayList<String>() {{
            add("Mini Panini");
            add("Green & Green Salads");
            add("Heavenly Sandwiches");
        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    private List<String> placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }

    private BeaconManager beaconManager;
    private Region region;
    private static final String THINGSPEAK_READ_URL ="http://api.thingspeak.com/channels/246649/feeds/last.json?api_key=2LOZ2RSRBZSXLZPV";
    private static final String THINGSPEAK_READ_TO_GATE = "http://api.thingspeak.com/channels/251937/feeds/last.json?api_key=CPNZNEFBKLR2EU9T";
    private static final String THINGSPEAK_READ_FROM_GATE = "http://api.thingspeak.com/channels/252021/feeds/last.json?api_key=0YPO42RYHGBCC65I";
    private static final String THINGSPEAK_READ_GATE_CHANGE = "http://api.thingspeak.com/channels/251938/feeds/last.json?api_key=JODK8CMIJEYXRDZP";
    public static final String EXTRA_MESSAGE = "com.example.udaanv20.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //final MyApplication var = (MyApplication)getApplicationContext();
        //final boolean flag=var.getFlag();
        //final Global globalVariable = (Global) getApplicationContext();
        //final boolean newflag = globalVariable.getFlag();

        //TextView tv=(TextView)findViewById(R.id.avgWaitingTime);
        //tv.setText(Global.flag+" minutes");

        /*if(!newflag){
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            startActivity(intent);
            TextView tv=(TextView)findViewById(R.id.avgWaitingTime);
            tv.setText(newflag+" minutes");
        }///sir problem is here*/



        new MyAsyncTask_waitTime().execute();
        new MyAsyncTask_from_gate().execute();
        new MyAsyncTask_to_gate().execute();
        new MyAsyncTask_flightNo().execute();

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    List<String> places = placesNearBeacon(nearestBeacon);
                    // TODO: update the UI here
                    Log.d("Airport", "Nearest places: " + places);
                }
            }
        });
        region = new Region("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);


    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class MyAsyncTask_waitTime extends AsyncTask<String , String, Void>
    {

        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        InputStream inputStream = null;
        String result = "";

        @Override
        protected void onPreExecute() {

           // progressDialog.setMessage("Downloading your data...");
            //progressDialog.show();

        }

        int val = 0;
        @Override
        protected Void doInBackground(String... params) {

            try {
                URL url = new URL(THINGSPEAK_READ_URL);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);

                StringBuilder builder = new StringBuilder();
                String line = bufferedReader.readLine();

                JSONObject jsonObject = new JSONObject(line);

                val = jsonObject.getInt("field1");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            TextView tv=(TextView)findViewById(R.id.avgWaitingTime);
            tv.setText(val+" minutes");

        }

    }

    class MyAsyncTask_from_gate extends AsyncTask<String , String, Void>
    {

        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        InputStream inputStream = null;
        String result = "";

        @Override
        protected void onPreExecute() {

            // progressDialog.setMessage("Downloading your data...");
            //progressDialog.show();

        }

        int fromGate = 0;
        @Override
        protected Void doInBackground(String... params) {

            try {
                URL url = new URL(THINGSPEAK_READ_FROM_GATE);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);

                StringBuilder builder = new StringBuilder();
                String line = bufferedReader.readLine();

                JSONObject jsonObject = new JSONObject(line);

                int fromGate = jsonObject.getInt("field1");
                final MyApplication var = (MyApplication)getApplicationContext();
                var.setGateFrom(fromGate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //TextView tv=(TextView)findViewById(R.id.avgWaitingTime);
            //tv.setText(val+" minutes");

        }

    }

    class MyAsyncTask_to_gate extends AsyncTask<String , String, Void>
    {

        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        InputStream inputStream = null;
        String result = "";

        @Override
        protected void onPreExecute() {

            // progressDialog.setMessage("Downloading your data...");
            //progressDialog.show();

        }

        int toGate = 0;
        @Override
        protected Void doInBackground(String... params) {

            try {
                URL url = new URL(THINGSPEAK_READ_TO_GATE);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);

                StringBuilder builder = new StringBuilder();
                String line = bufferedReader.readLine();

                JSONObject jsonObject = new JSONObject(line);

                toGate = jsonObject.getInt("field1");
                final MyApplication var = (MyApplication)getApplicationContext();
                var.setGateTo(toGate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //TextView tv=(TextView)findViewById(R.id.avgWaitingTime);
            //tv.setText(val+" minutes");

        }

    }

    class MyAsyncTask_flightNo extends AsyncTask<String , String, Void>
    {

        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        InputStream inputStream = null;
        String result = "";

        @Override
        protected void onPreExecute() {

            // progressDialog.setMessage("Downloading your data...");
            //progressDialog.show();

        }

        int flightNo = 0;
        @Override
        protected Void doInBackground(String... params) {

            try {
                URL url = new URL(THINGSPEAK_READ_GATE_CHANGE);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);

                StringBuilder builder = new StringBuilder();
                String line = bufferedReader.readLine();

                JSONObject jsonObject = new JSONObject(line);

                flightNo = jsonObject.getInt("field1");
                final MyApplication var = (MyApplication)getApplicationContext();
                var.setFlightNo(flightNo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //TextView tv=(TextView)findViewById(R.id.avgWaitingTime);
            //tv.setText(val+" minutes");

        }

    }

}
