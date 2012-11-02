package com.herokuapp.maintainenator;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapActivity;
import android.os.Bundle;

public class MapViewActivity extends MapActivity {

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        MapController mapController = mapView.getController();
        GeoPoint center = new GeoPoint(43000828, -78789839);
        mapController.setCenter(center);
        mapController.setZoom(14);
    }

}
