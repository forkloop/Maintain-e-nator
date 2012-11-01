package com.herokuapp.maintainenator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Fragment;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class IndoorFormFragment extends Fragment implements OnItemSelectedListener, OnLongClickListener {

    private static final int HIGEST_FLOOR = 5;
    private static final int[] FLOOR = {4, 5, 3};
    private List<String> floorArray;
    private Spinner buildingSpinner;
    private Spinner floorSpinner;
    private Spinner roomSpinner;
    private ImageView imageView;

    private static int longClickedId;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        floorArray = new ArrayList<String>(HIGEST_FLOOR);
        floorArray.add("Base");
        for(int i=1; i<=HIGEST_FLOOR; i++) {
            floorArray.add(i + "F");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationManager locationManager = ((FormActivity) getActivity()).getLocationManager();
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, (LocationListener) getActivity(), null);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, (LocationListener) getActivity(), null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = (LinearLayout) inflater.inflate(R.layout.fragment_indoor_form, container, false);

        imageView = (ImageView) layout.findViewById(R.id.indoor_image_view1);
        imageView.setOnLongClickListener(this);
        buildingSpinner = (Spinner) layout.findViewById(R.id.building_spinner);
        buildingSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> buildingAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.buildings, android.R.layout.simple_spinner_item);
        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buildingSpinner.setAdapter(buildingAdapter);

        floorSpinner = (Spinner) layout.findViewById(R.id.floor_spinner);
        floorSpinner.setOnItemSelectedListener(this);
        roomSpinner = (Spinner) layout.findViewById(R.id.room_spinner);
        roomSpinner.setOnItemSelectedListener(this);
        return layout;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getId() == R.id.building_spinner) {
            Log.d(getClass().getSimpleName(), "Building spinner selected.");
            ArrayAdapter<String> floorAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, floorArray.subList(0, FLOOR[pos]+1));
            floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floorSpinner.setAdapter(floorAdapter);
        } else if (parent.getId() == R.id.floor_spinner) {
            Log.d(getClass().getSimpleName(), "Floor Spinner selected.");
            ArrayAdapter<String> roomAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
                    getRoomList(buildingSpinner.getSelectedItemPosition(), pos));
            roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roomSpinner.setAdapter(roomAdapter);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private List<String> getRoomList(int buildingNo, int floorNo) {
        //XXX
        return Arrays.asList((new String[]{"101", "202", "Men's", "Women's"}));
    }

    @Override
    public boolean onLongClick(View v) {
        longClickedId = v.getId();
        PhotoActionDialogFragment photoActionDialog = new PhotoActionDialogFragment();
        photoActionDialog.show(getActivity().getFragmentManager(), "photo_action");
        return true;
    }

    static int getLongClickedId() {
        return longClickedId;
    }
}