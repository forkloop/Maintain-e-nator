package com.herokuapp.maintainenator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FormActivity extends Activity implements LocationListener {

    private static final int CAMERA_REQUEST = 0;
    private static final int ALBUM_REQUEST = 1;
    private static final float MIN_ACCURACY = 100;
    private static final long LOCATION_UPDATE_TIME = 5000L;
    private Activity currentActivity;
    private ActionBar actionBar;
    private IndoorFormFragment indoorFormFragment;
    private OutdoorFormFragment outdoorFormFragment;

    private String cachedAddress;
    private Location lastLocation;
    private Location cachedGPSLocation;
    private Location cachedNetworkLocation;
    private LocationManager locationManager;
    private SharedPreferences sharedPreferences;
    private boolean localDev;
    private String username;
    private String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentActivity = this;
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Tab tab = actionBar.newTab()
                       .setText(R.string.indoor)
                       .setTag("indoor")
                       .setTabListener(new FormTabListener<IndoorFormFragment>(this, "indoor", IndoorFormFragment.class));
        actionBar.addTab(tab);

        tab = actionBar.newTab()
                .setText(R.string.outdoor)
                .setTag("outdoor")
                .setTabListener(new FormTabListener<OutdoorFormFragment>(this, "outdoor", OutdoorFormFragment.class));
        actionBar.addTab(tab);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkNetworkStatus()) {
            localDev = sharedPreferences.getBoolean("dev_mode", true);
            Log.d(getClass().getSimpleName(), "local_dev?: " + localDev);
            username = sharedPreferences.getString("username", "");
            Log.d(getClass().getSimpleName(), "username: " + username);
            password = sharedPreferences.getString("password", "");
            // Location setup, stop using periodically location update
            Log.d(getClass().getSimpleName(), "Requesting location...");
            Log.d(getClass().getSimpleName(), "Network enabled: " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
            Log.d(getClass().getSimpleName(), "GPS enabled: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_TIME, 0, this);
            cachedNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Log.d(getClass().getSimpleName(), "cachedNetworkLocation: " + cachedNetworkLocation);
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_TIME, 0, this);
            cachedGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d(getClass().getSimpleName(), "cachedGPSLocation: " + cachedGPSLocation);
        } else {
            Toast.makeText(this, "Please enable network connection.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        locationManager.removeUpdates(this);
        super.onStop();
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
                //TODO
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getSimpleName(), "requstCode " + requestCode + ", resultCode " + resultCode);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            String photoURI = PhotoActionDialogFragment.getNewPhotoName();
            galleryAddPicture(photoURI);
            displayPhoto(photoURI);
            Log.d(getClass().getSimpleName(), "Camera: display the picture.");
        } else if (requestCode == ALBUM_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri photoUri = data.getData();
            if (photoUri != null) {
                Log.d(getClass().getSimpleName(), "Picture: " + photoUri);
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(photoUri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                displayPhoto(filePath);
                Log.d(getClass().getSimpleName(), "Gallery: display the picture.");
            }
        }
    }

    // Called by FormTabListener
    public void setIndoorFormFragment(Fragment fragment) {
        indoorFormFragment = (IndoorFormFragment) fragment;
    }

    public void setOutdoorFormFragment(Fragment fragment) {
        outdoorFormFragment = (OutdoorFormFragment) fragment;
    }

    private void galleryAddPicture(String photoURI) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoURI);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    public void displayPhoto(String photoURI) {
        Tab tab = actionBar.getSelectedTab();
        Bitmap bmp = BitmapFactory.decodeFile(photoURI);
        Bitmap photo = Bitmap.createScaledBitmap(bmp, 200, 140, true);
        if (tab.getTag().equals("indoor")) {
            ImageView imageView = (ImageView) findViewById(IndoorFormFragment.getLongClickedId());
            imageView.setImageBitmap(photo);
            imageView.setOnClickListener(new PhotoClickListener(photoURI));
            enableNextPhoto(true, IndoorFormFragment.getLongClickedId());
        } else {
            OutdoorFormFragment outdoorFragment = (OutdoorFormFragment) getFragmentManager().findFragmentByTag("outdoor");
            int longClickedId = outdoorFragment.getLongClickedId();
            ImageView imageView = (ImageView) findViewById(longClickedId);
            imageView.setImageBitmap(photo);
            imageView.setOnClickListener(new PhotoClickListener(photoURI));
            enableNextPhoto(false, longClickedId);
        }
    }

    public void deletePhoto() {
        if (actionBar.getSelectedTab().getTag().equals("indoor")) {
            ImageView imageView = (ImageView) findViewById(IndoorFormFragment.getLongClickedId());
            imageView.setOnClickListener(null);
            imageView.setImageBitmap(null);
            imageView.setImageResource(R.drawable.content_new_picture);
        }
    }

    private void enableNextPhoto(boolean isIndoor, int currentId) {
        if (isIndoor) {
            if (currentId == R.id.indoor_image_view1) {
                ImageView iv = (ImageView) findViewById(R.id.indoor_image_view2);
                if (!iv.isLongClickable()) {
                    iv.setOnLongClickListener(indoorFormFragment);
                    iv.setBackgroundColor(0xFF00BAFF);
                    iv.setImageResource(R.drawable.content_new_picture);
                    iv.setScaleType(ScaleType.CENTER);
                }
            } else if (currentId == R.id.indoor_image_view2) {
                ImageView iv = (ImageView) findViewById(R.id.indoor_image_view3);
                if (!iv.isLongClickable()) {
                    iv.setOnLongClickListener(indoorFormFragment);
                    iv.setImageResource(R.drawable.content_new_picture);
                    iv.setScaleType(ScaleType.CENTER);
                }
            }
        } else {
            if (currentId == R.id.outdoor_image_view1) {
                ImageView iv = (ImageView) findViewById(R.id.outdoor_image_view2);
                if (!iv.isLongClickable()) {
                    iv.setOnLongClickListener(outdoorFormFragment);
                    iv.setBackgroundColor(0xFF00BAFF);
                    iv.setImageResource(R.drawable.content_new_picture);
                    iv.setScaleType(ScaleType.CENTER);
                }
            } else if (currentId == R.id.outdoor_image_view2) {
                ImageView iv = (ImageView) findViewById(R.id.outdoor_image_view3);
                if (!iv.isLongClickable()) {
                    iv.setOnLongClickListener(outdoorFormFragment);
                    iv.setImageResource(R.drawable.content_new_picture);
                    iv.setScaleType(ScaleType.CENTER);
                }
            }
        }
    }

    private class PhotoClickListener implements OnClickListener {

        private String photoURI;

        public PhotoClickListener(String photoURI) {
            this.photoURI = photoURI;
        }

        @Override
        public void onClick(View v) {
            FullPhotoDialogFragment dialog = new FullPhotoDialogFragment();
            dialog.setPhotoURI(photoURI);
            dialog.show(getFragmentManager(), "full_photo");
        }
    }

    /**
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(getClass().getSimpleName(), "Location changed to: " + location);
        if (lastLocation == null || location.getAccuracy() < lastLocation.getAccuracy()) {
            lastLocation = location;
            //new ReverseGeoTask().execute(location);
        }
        /*
        if (location.getAccuracy() < MIN_ACCURACY) {
            if (location.getProvider().equals("gps")) {
                if (cachedGPSLocation == null || Math.abs(location.getLatitude() - cachedGPSLocation.getLatitude()) >1e-5 ||
                        Math.abs(location.getLongitude() - cachedGPSLocation.getLongitude()) > 1e-5) {
                    cachedGPSLocation = location;
                    new ReverseGeoTask().execute(location);
                }
            } else if (location.getProvider().equals("network")) {
                if (cachedNetworkLocation == null || Math.abs(location.getLatitude() - cachedNetworkLocation.getLatitude()) > 1e-5 ||
                        Math.abs(location.getLongitude() - cachedNetworkLocation.getLongitude()) > 1e-5) {
                    cachedNetworkLocation = location;
                    new ReverseGeoTask().execute(location);
                }
            } else {
                Log.d(getClass().getSimpleName(), "Update from unknown location provider: " + location.getProvider());
            }
        }
        */
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

    /**
     *  Reverse the geo data into plain location.
     *  Return array of length 2, first one is location for indoor, second is for outdoor.
     */
    class ReverseGeoTask extends AsyncTask<Location, Integer, String[]> {

        private String findBuilding(Location location) {
            //TODO use geo data
            return "Bell Hall";
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
        protected void onPostExecute(String[] result) {
            Tab tab = actionBar.getSelectedTab();
            if (tab.getTag().equals("indoor")) {
                Spinner buildingSpinner = (Spinner) findViewById(R.id.building_spinner);
                String[] buildingArray = getResources().getStringArray(R.array.buildings);
                for (int index=0; index<buildingArray.length; index++) {
                    if (buildingArray[index].equals(result[0])) {
                        buildingSpinner.setSelection(index);
                        break;
                    }
                }
            } else {
                TextView addressView = (TextView) findViewById(R.id.outdoor_address);
                if (!addressView.isFocused()) {
                    addressView.setText(result[1]);
                }
                if (result[1].contains("ECONNREFUSED")) {
                    Toast.makeText(getApplicationContext(), "Please enable network connection!", Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected String[] doInBackground(Location... params) {
            if (params.length > 0) {
                Location location = params[0];
                // Locate which building might be
                String building = findBuilding(location);
                HttpURLConnection urlConnection = null;
                URL url = null;
                try {
                    url = new URL(getString(R.string.google_map_url) + location.getLatitude() + "," + location.getLongitude() + "&sensor=true");
                    Log.d(getClass().getSimpleName(), "Requesting Google Maps API: " + url.toString());
                } catch (MalformedURLException mue) {
                    Log.e(getClass().getSimpleName(), mue.getMessage());
                    return (new String[] {building, mue.getMessage()});
                }
                try {
                    urlConnection = (HttpURLConnection) url.openConnection();
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        String jsonResponseString = getJsonResponse(urlConnection.getInputStream());
                        Log.d(getClass().getSimpleName(), jsonResponseString);
                        JSONObject jsonResponse = new JSONObject(jsonResponseString);
                        String address = jsonResponse.getJSONArray(getString(R.string.json_array_tag)).getJSONObject(0).getString(getString(R.string.json_address_tag));
                        Log.d(getClass().getSimpleName(), "Address: " + address);
                        return (new String[] {building, address});
                    }
                    Log.d(getClass().getSimpleName(), "Response: " + urlConnection.getResponseMessage());
                    return new String[] {building, urlConnection.getResponseMessage()};
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    Log.e(getClass().getSimpleName(), ioe.getMessage());
                    return new String[] {building, ioe.getMessage()};
                } catch (JSONException je) {
                    je.printStackTrace();
                    Log.e(getClass().getSimpleName(), je.getMessage());
                    return new String[] {building, je.getMessage()};
                } finally {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }

    private boolean checkNetworkStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        Log.d(getClass().getSimpleName(), networkInfo.toString());
        return networkInfo != null && networkInfo.isConnected();
    }

    public LocationManager getLocationManager() {
        return this.locationManager;
    }
}