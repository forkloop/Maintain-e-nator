package com.herokuapp.maintainenator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class MapViewActivity extends MapActivity {

    private static final String TAG = "MapViewActivity";
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        MapView mapView = (MapView) findViewById(R.id.mapview);
        Intent intent = getIntent();
        int latitude = 0;
        int longitude = 0;
        if (intent != null) {
            latitude = intent.getIntExtra("latitude", 43000828);
            longitude = intent.getIntExtra("longitude", -78789839);
            Log.d(TAG, latitude + ", " + longitude);
        }
        mapView.setBuiltInZoomControls(true);
        MapController mapController = mapView.getController();
        GeoPoint center = new GeoPoint(latitude, longitude);
        mapController.setCenter(center);
        mapController.setZoom(14);
    }

}
