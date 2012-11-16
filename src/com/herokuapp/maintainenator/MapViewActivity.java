package com.herokuapp.maintainenator;

import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

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
            // default to Davis Hall
            latitude = intent.getIntExtra("latitude", 43002854);
            longitude = intent.getIntExtra("longitude", -78789839);
            Log.d(TAG, latitude + ", " + longitude);
        }
        mapView.setBuiltInZoomControls(true);
        MapController mapController = mapView.getController();
        GeoPoint center = new GeoPoint(latitude, longitude);
        mapController.setCenter(center);
        mapController.setZoom(18);
        List<Overlay> mapOverlays = mapView.getOverlays();
        Log.d(TAG, "# of overlays: " + mapOverlays);
        Drawable drawable = this.getResources().getDrawable(R.drawable.location_marker);
        MapItemizedOverlay itemizedOverlay = new MapItemizedOverlay(drawable, this);
        OverlayItem overlayItem = new OverlayItem(center, null, null);
        itemizedOverlay.addOverlay(overlayItem);
        mapOverlays.add(itemizedOverlay);
    }

}