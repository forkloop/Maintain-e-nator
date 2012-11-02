package com.herokuapp.maintainenator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayCreateFormActivity extends Activity implements LocationListener, OnClickListener {

    private static final String END = "\r\n";
    private static final long LOCATION_UPDATE_TIME = 5000L;
    private static final String BOUNDARY = "1q2w3e4r5t";
    private static final String TWO_HYPHENS = "--";

    private Button submitButton;
    private EditText descriptionView;
    private EditText addressView;

    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    private LocationManager locationManager;

    private String cachedAddress;
    private Location cachedGPSLocation;
    private Location cachedNetworkLocation;

    SharedPreferences sharedPreferences;
    private boolean localDev;
    private String username;
    private String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_outdoor_form);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        submitButton = (Button) findViewById(R.id.outdoor_submit);
        submitButton.setOnClickListener(this);

        descriptionView = (EditText) findViewById(R.id.outdoor_description);
        addressView = (EditText) findViewById(R.id.outdoor_address);

        broadcastReceiver = new WifiBroadcastReceiver();
        intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private boolean checkSubmitInfo() {
        if (!(descriptionView.getText().toString().isEmpty() || addressView.getText().toString().isEmpty())) {
            Log.d(getClass().getSimpleName(), "qqqq" + descriptionView.getText().toString());
            Log.d(getClass().getSimpleName(), "wwww" + addressView.getText().toString());
            return true;
        }
        Toast.makeText(this, "Missing information!", Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.indoor_submit) {
            if (checkSubmitInfo()) {
                if (true) {
                    new UploadJSONTask().execute();
                } else {
                    new UploadMultipartTask().execute();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clip:
                FragmentManager fm = getFragmentManager();
                AttachmentDialog dialog = new AttachmentDialog();
                dialog.show(fm, "fragment");
                return true;
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                    return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        localDev = sharedPreferences.getBoolean("dev_mode", true);
        Log.d(getClass().getSimpleName(), "local_dev?: " + localDev);
        username = sharedPreferences.getString("username", "");
        Log.d(getClass().getSimpleName(), "username: " + username);
        password = sharedPreferences.getString("password", "");

        registerReceiver(broadcastReceiver, intentFilter);
        Log.d(getClass().getSimpleName(), "Requesting location...");
        Log.d(getClass().getSimpleName(), "Network enabled: " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        Log.d(getClass().getSimpleName(), "GPS enabled: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_TIME, 0, this);
        cachedNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.d(getClass().getSimpleName(), "cachedNetworkLocation: " + cachedNetworkLocation);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_TIME, 0, this);
        cachedGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Log.d(getClass().getSimpleName(), "cachedGPSLocation: " + cachedGPSLocation);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        locationManager.removeUpdates(this);
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(getClass().getSimpleName(), "Location changed to: " + location);
        if (location.getProvider().equals("gps")) {
            if (cachedGPSLocation == null || Math.abs(location.getLatitude() - cachedGPSLocation.getLatitude()) >1e-5 ||
                    Math.abs(location.getLongitude() - cachedGPSLocation.getLongitude()) > 1e-5) {
                cachedGPSLocation = location;
                new ReverseGeoTask().execute(new Location[] {location});
            }
        } else if (location.getProvider().equals("network")) {
            if (cachedNetworkLocation == null || Math.abs(location.getLatitude() - cachedNetworkLocation.getLatitude()) > 1e-5 ||
                    Math.abs(location.getLongitude() - cachedNetworkLocation.getLongitude()) > 1e-5) {
                cachedNetworkLocation = location;
                new ReverseGeoTask().execute(new Location[] {location});
            }
        } else {
            Log.d(getClass().getSimpleName(), "Update from unknown location provider: " + location.getProvider());
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(getClass().getSimpleName(), provider + ": DISABLED!");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(getClass().getSimpleName(), provider + ": ENABLED!");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extra) {
        Log.d(getClass().getSimpleName(), provider + ": " + status);
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Listen for WiFi status and other intents.
     */
    private class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(getClass().getSimpleName(), "WiFi: " + intent.getAction());
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION) || intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (networkInfo.isConnected()) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    Log.d(getClass().getSimpleName(), wifiInfo.getBSSID() + ": " + wifiInfo.getRssi());
                }
            }
        }
    }

    private String generateMultipartForm() {
        StringBuilder sb = new StringBuilder();
        sb.append(TWO_HYPHENS + BOUNDARY + END);
        sb.append("Content-Disposition: form-data; name=\"description\"" + END + END + descriptionView.getText().toString() + END);
        sb.append(TWO_HYPHENS + BOUNDARY + END);
        sb.append("Content-Disposition: form-data; name=\"location\"" + END + END + addressView.getText().toString() + END);
        String latString = String.format("%.5f", cachedGPSLocation.getLatitude());
        if (!latString.equals(getString(R.string.default_latitude))) {
            sb.append(TWO_HYPHENS + BOUNDARY + END);
            sb.append("Content-Disposition: form-data; name=\"latitude\"" + END + END + latString + END);
            sb.append(TWO_HYPHENS + BOUNDARY + END);
            sb.append("Content-Disposition: form-data; name=\"longitude\"" + END + END + String.format("%.5f", cachedGPSLocation.getLongitude()) + END);
        }
        sb.append(TWO_HYPHENS + BOUNDARY + END);
        File photo = new File("");
        Log.d(getClass().getSimpleName(), "photo name: " + photo.getName());
        sb.append("Content-Disposition: form-data; name=\"photo\"; filename=\"" + photo.getName() + "\"" + END);
        sb.append("Content-type: image/jpeg" + END + END);
        return sb.toString();
    }

    private class UploadMultipartTask extends AsyncTask<Void, Integer, String> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(DisplayCreateFormActivity.this, "", "");
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... params) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            try {
                if (localDev) {
                    url = new URL(getString(R.string.local_url));
                } else {
                    url = new URL(getString(R.string.post_url));
                }
            } catch(MalformedURLException mue) {
                mue.printStackTrace();
                return mue.getMessage();
            }
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
                String credential = username + ":" + password;
                String encodedCredential = Base64.encodeToString(credential.getBytes(), Base64.DEFAULT);
                Log.d(getClass().getSimpleName(), "encodedCredential: " + encodedCredential);
                urlConnection.setRequestProperty("Authorization", "Basic " + encodedCredential);
                String requestBodyFirstPart = generateMultipartForm();
                urlConnection.connect();
                DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
                out.writeBytes(requestBodyFirstPart);
                FileInputStream fileInputStream = new FileInputStream("");
                byte[] fileBuffer = new byte[1024];
                int length = 0;
                while ((length = fileInputStream.read(fileBuffer)) != -1) {
                    out.write(fileBuffer, 0, length);
                }
                out.writeBytes(END);
                out.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS);
                fileInputStream.close();
                out.flush();
                out.close();
                return urlConnection.getResponseMessage();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return ioe.getMessage();
            } finally {
                urlConnection.disconnect();
            }
        }
    }

    private String generateJsonData() {
        JSONObject json = new JSONObject();
        try {
            json.put("description", descriptionView.getText().toString());
            json.put("location", addressView.getText().toString());
            String latString = String.format("%.5f", cachedGPSLocation.getLatitude());
            if (!latString.equals(getString(R.string.default_latitude))) {
                json.put("latitude", latString.toString());
                json.put("longitude", String.format("%.5f", cachedGPSLocation.getLongitude()));
            }
        } catch (JSONException je) {
            Log.e(getClass().getSimpleName(), je.toString());
            return null;
        }
        Log.d(getClass().getSimpleName(), "JSON: " + json.toString());
        return json.toString();
    }

    private class UploadJSONTask extends AsyncTask<Void, Integer, String> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(DisplayCreateFormActivity.this, "", "");
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... params) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            OutputStream out = null;
            String data = generateJsonData();
            try {
                if (localDev) {
                    url = new URL(getString(R.string.local_url));
                } else {
                    url = new URL(getString(R.string.post_url));
                }
            } catch (MalformedURLException mue) {
                Log.e(getClass().getSimpleName(), mue.getMessage());
                return mue.getMessage();
            }
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                String credential = username + ":" + password;
                String encodedCredential = Base64.encodeToString(credential.getBytes(), Base64.DEFAULT);
                Log.d(getClass().getSimpleName(), "encodedCredential: " + encodedCredential);
                urlConnection.setRequestProperty("Authorization", "Basic " + encodedCredential);
                urlConnection.connect();
                out = urlConnection.getOutputStream();
                out.write(data.getBytes("UTF-8"));
                out.flush();
                out.close();
                Log.d(getClass().getSimpleName(), "Response: " + urlConnection.getResponseMessage());
                return urlConnection.getResponseMessage();
            } catch (IOException ioe) {
                Log.e(getClass().getSimpleName(), "Error response: " + ioe.getMessage());
                ioe.printStackTrace();
                return ioe.getMessage();
            } finally {
                urlConnection.disconnect();
            }
        }
    }

    private class ReverseGeoTask extends AsyncTask<Location, Integer, String> {

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.equals(cachedAddress)) {
                cachedAddress = result;
                addressView.setText(result);
            }
        }

        private String getJsonResponse(InputStream in) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                bufferedReader.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return null;
            }
            return sb.toString();
        }

        @Override
        protected String doInBackground(Location... params) {
            if (params.length > 0) {
                Location location = params[0];
                HttpURLConnection urlConnection = null;
                URL url = null;
                try {
                    url = new URL(getString(R.string.google_map_url) + location.getLatitude() + "," + location.getLongitude() + "&sensor=true");
                    Log.d(getClass().getSimpleName(), "Requesting Google Maps API: " + url.toString());
                } catch (MalformedURLException mue) {
                    Log.e(getClass().getSimpleName(), mue.getMessage());
                    return mue.getMessage();
                }
                try {
                    urlConnection = (HttpURLConnection) url.openConnection();
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        String jsonResponseString = getJsonResponse(urlConnection.getInputStream());
                        Log.d(getClass().getSimpleName(), jsonResponseString);
                        JSONObject jsonResponse = new JSONObject(jsonResponseString);
                        String address = jsonResponse.getJSONArray(getString(R.string.json_array_tag)).getJSONObject(0).getString(getString(R.string.json_address_tag));
                        Log.d(getClass().getSimpleName(), "Address: " + address);
                        return address;
                    }
                    Log.d(getClass().getSimpleName(), "Response: " + urlConnection.getResponseMessage());
                    return urlConnection.getResponseMessage();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    Log.e(getClass().getSimpleName(), ioe.getMessage());
                    return ioe.getMessage();
                } catch (JSONException je) {
                    Log.e(getClass().getSimpleName(), je.getMessage());
                    return je.getMessage();
                } finally {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }

}