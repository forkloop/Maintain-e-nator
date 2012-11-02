package com.herokuapp.maintainenator;

import android.app.Fragment;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class OutdoorFormFragment extends Fragment implements OnLongClickListener, OnClickListener {

    private String[] photoPath;
    private ImageView imageView;
    private Button mapButton;
    private int longClickedId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = (LinearLayout) inflater.inflate(R.layout.fragment_outdoor_form, container, false);

        imageView = (ImageView) layout.findViewById(R.id.outdoor_image_view1);
        imageView.setOnLongClickListener(this);
        mapButton = (Button) layout.findViewById(R.id.map_button);
        mapButton.setOnClickListener(this);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationManager locationManager = ((FormActivity) getActivity()).getLocationManager();
        Log.d(getClass().getSimpleName(), "GPS: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        Log.d(getClass().getSimpleName(), "Network: " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, (LocationListener) getActivity(), null);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, (LocationListener) getActivity(), null);
    }

    @Override
    public boolean onLongClick(View v) {
        longClickedId = v.getId();
        PhotoActionDialogFragment photoActionDialog = new PhotoActionDialogFragment();
        photoActionDialog.show(getActivity().getFragmentManager(), "photo_action");
        return true;
    }

    public int getLongClickedId() {
        return longClickedId;
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.map_button) {
            startActivity(new Intent(getActivity(), MapViewActivity.class));
        }
    }
}