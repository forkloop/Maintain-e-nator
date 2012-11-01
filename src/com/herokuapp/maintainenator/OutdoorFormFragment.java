package com.herokuapp.maintainenator;

import android.app.Fragment;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class OutdoorFormFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (LinearLayout) inflater.inflate(R.layout.fragment_outdoor_form, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationManager locationManager = ((FormActivity) getActivity()).getLocationManager();
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, (LocationListener) getActivity(), null);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, (LocationListener) getActivity(), null);
    }
}