package com.herokuapp.maintainenator;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapItemizedOverlay extends ItemizedOverlay<OverlayItem> {

    private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
    private Context context;

    public MapItemizedOverlay(Drawable marker) {
        super(boundCenterBottom(marker));
    }

    public MapItemizedOverlay(Drawable marker, Context context) {
        super(boundCenterBottom(marker));
        this.context = context;
    }

    @Override
    protected OverlayItem createItem(int index) {
        return overlays.get(index);
    }

    @Override
    public int size() {
        return overlays.size();
    }

    public void addOverlay(OverlayItem overlay) {
        overlays.add(overlay);
        populate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            GeoPoint point = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
            Toast.makeText(context, point.getLatitudeE6() + ", " + point.getLongitudeE6(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}