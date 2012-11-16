package com.herokuapp.maintainenator;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapItemizedOverlay extends ItemizedOverlay<OverlayItem> {

    private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
    private MapActivity activity;
    private OverlayItem inDrag = null;
    private Drawable marker = null;
    private ImageView dragImage = null;
    private int xDragImageOffset = 0;
    private int yDragImageOffset = 0;
    private int xDragTouchOffset = 0;
    private int yDragTouchOffset = 0;

    private GeoPoint draggedPoint = null;

    public MapItemizedOverlay(Drawable marker) {
        super(boundCenterBottom(marker));
        this.marker = marker;
    }

    public MapItemizedOverlay(Drawable marker, MapActivity activity) {
        super(boundCenterBottom(marker));
        this.activity = activity;
        this.marker = marker;
        dragImage = (ImageView) activity.findViewById(R.id.drag);
        xDragImageOffset=dragImage.getDrawable().getIntrinsicWidth()/2;
        yDragImageOffset=dragImage.getDrawable().getIntrinsicHeight();
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
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
        boundCenterBottom(marker);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        final int action = event.getAction();
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        boolean result = false;

        if (action == MotionEvent.ACTION_DOWN) {
            for (OverlayItem overlay : overlays) {
                Point p = new Point(0, 0);
                mapView.getProjection().toPixels(overlay.getPoint(), p);
                if (hitTest(overlay, marker, x-p.x, y-p.y)) {
                    result = true;
                    inDrag = overlay;
                    overlays.remove(inDrag);
                    populate();

                    xDragTouchOffset = 0;
                    yDragTouchOffset = 0;
                    setDragImagePosition(p.x, p.y);
                    dragImage.setVisibility(View.VISIBLE);
                    xDragTouchOffset = x - p.x;
                    yDragTouchOffset = y - p.y;
                    break;
                }
            }
        } else if (action == MotionEvent.ACTION_UP && inDrag != null) {
            dragImage.setVisibility(View.GONE);
            GeoPoint point = mapView.getProjection().fromPixels(x - xDragTouchOffset, y - yDragTouchOffset);
            OverlayItem toDrop = new OverlayItem(point, null, null);
            overlays.add(toDrop);
            populate();
            inDrag = null;
            result = true;
            draggedPoint = point;
            Toast.makeText(activity, point.getLatitudeE6()/1E6 + ", " + point.getLongitudeE6()/1E6, Toast.LENGTH_SHORT).show();
        } else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
            setDragImagePosition(x, y);
            result = true;
        }
        return (result || super.onTouchEvent(event, mapView));
    }

    public GeoPoint getDraggedPoint() {
        return draggedPoint;
    }

    private void setDragImagePosition(int x, int y) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage.getLayoutParams();
        lp.setMargins((x - xDragImageOffset - xDragTouchOffset), (y - yDragImageOffset - yDragTouchOffset), 0, 0);
        dragImage.setLayoutParams(lp);
    }
}