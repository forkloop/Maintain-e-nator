package com.herokuapp.maintainenator;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class IndoorFormFragment extends Fragment implements OnItemSelectedListener, OnLongClickListener {

    private static final String TAG = "IndoorFormFragment";
    private static final String PHOTO_PATH_SEPARATOR = "##";
    private static final String END = "\r\n";
    private static final String BOUNDARY = "1q2w3e4r5t";
    private static final String TWO_HYPHENS = "--";

    private static final int HIGEST_FLOOR = 5;
    private static final int MAX_PHOTO_NUM = 3;
    private static final int[] FLOOR = {4, 5, 3};
    private List<String> floorArray;
    private Spinner buildingSpinner;
    private Spinner floorSpinner;
    private ImageView imageView;
    private EditText descriptionText;
    private EditText roomText;
    private EditText extraLocation;
    private static int longClickedId;
    private int buildingSelectedTime;
    private String[] photoArray;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildingSelectedTime = 0;
        photoArray = new String[MAX_PHOTO_NUM];
        floorArray = new ArrayList<String>(HIGEST_FLOOR);
        floorArray.add("Base");
        for(int i=1; i<=HIGEST_FLOOR; i++) {
            floorArray.add(i + "F");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = (LinearLayout) inflater.inflate(R.layout.fragment_indoor_form, container, false);

        layout.findViewById(R.id.indoor_submit).setOnClickListener(new IndoorSubmitClickListener());
        descriptionText = (EditText) layout.findViewById(R.id.indoor_description);
        roomText = (EditText) layout.findViewById(R.id.room_text);
        extraLocation = (EditText) layout.findViewById(R.id.indoor_location_optional);
        imageView = (ImageView) layout.findViewById(R.id.indoor_image_view1);
        imageView.setOnLongClickListener(this);
        buildingSpinner = (Spinner) layout.findViewById(R.id.building_spinner);
        buildingSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> buildingAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.buildings, android.R.layout.simple_spinner_item);
        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buildingSpinner.setAdapter(buildingAdapter);

        floorSpinner = (Spinner) layout.findViewById(R.id.floor_spinner);
        floorSpinner.setOnItemSelectedListener(this);
        return layout;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getId() == R.id.building_spinner) {
            synchronized(this) {
                buildingSelectedTime++;
            }
            Log.d(getClass().getSimpleName(), "Building spinner selected.");
            ArrayAdapter<String> floorAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, floorArray.subList(0, FLOOR[pos]+1));
            floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floorSpinner.setAdapter(floorAdapter);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public boolean onLongClick(View v) {
        longClickedId = v.getId();
        PhotoActionDialogFragment photoActionDialog = new PhotoActionDialogFragment();
        photoActionDialog.show(getActivity().getFragmentManager(), "photo_action");
        return true;
    }

    int getBuildingSelectedTime() {
        synchronized(this) {
            return buildingSelectedTime;
        }
    }

    static int getLongClickedId() {
        return longClickedId;
    }

    void setPhotoPath(int viewId, String path) {
        if (viewId == R.id.indoor_image_view1) {
            photoArray[0] = path;
        } else if (viewId == R.id.indoor_image_view2) {
            photoArray[1] = path;
        } else if (viewId == R.id.indoor_image_view3) {
            photoArray[2] = path;
        }
    }

    String generateMultipartForm() {
        // Just text info
        StringBuilder sb = new StringBuilder();
        sb.append(TWO_HYPHENS + BOUNDARY + END);
        sb.append("Content-Disposition: form-data; name=\"description\"" + END + END + descriptionText.getText().toString() + END);
        sb.append(TWO_HYPHENS + BOUNDARY + END);
        sb.append("Content-Disposition: form-data; name=\"building\"" + END + END + buildingSpinner.getSelectedItem().toString() + END);
        sb.append(TWO_HYPHENS + BOUNDARY + END);
        sb.append("Content-Disposition: form-data; name=\"floor\"" + END + END + floorSpinner.getSelectedItem().toString() + END);
        sb.append(TWO_HYPHENS + BOUNDARY + END);
        sb.append("Content-Disposition: form-data; name=\"room\"" + END + END + roomText.getText().toString() + END);
        // Add indoor status
        sb.append(TWO_HYPHENS + BOUNDARY + END);
        sb.append("Content-Disposition: form-data; name=\"indoor\"" + END + END + "checked" + END);
        String extra = extraLocation.getText().toString();
        if (!extra.isEmpty()) {
            sb.append(TWO_HYPHENS + BOUNDARY + END);
            sb.append("Content-Disposition: form-data; name=\"location\"" + END + END + extra + END);
        }
        Location latestLocation = ((FormActivity) getActivity()).getLatestLocation();
        if (latestLocation != null) {
            sb.append(TWO_HYPHENS + BOUNDARY + END);
            sb.append("Content-Disposition: form-data; name=\"latitude\"" + END + END + latestLocation.getLatitude() + END);
            sb.append(TWO_HYPHENS + BOUNDARY + END);
            sb.append("Content-Disposition: form-data; name=\"longitude\"" + END + END + latestLocation.getLongitude() + END);
        }
        return sb.toString();
    }

    private String joinPhotoPath() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int index=0; index<MAX_PHOTO_NUM; index++) {
            if (photoArray[index] != null && !photoArray[index].isEmpty()) {
                if (first) {
                    first = false;
                    sb.append(photoArray[index]);
                } else {
                    sb.append(PHOTO_PATH_SEPARATOR);
                    sb.append(photoArray[index]);
                }
            }
        }
        return sb.toString();
    }

    private class IndoorSubmitClickListener implements OnClickListener {

        boolean checkData() {
            if (descriptionText.getText().toString().isEmpty() || roomText.getText().toString().isEmpty()) {
                return false;
            }
            if (photoArray[0] != null || photoArray[1] != null || photoArray[2] != null) {
                return true;
            }
            //TODO
//            ((FormActivity) getActivity()).buildAlertDialog().show();
            Log.d(getClass().getSimpleName(), "returning...");
            return true;
        }

        @Override
        public void onClick(View v) {
            if (checkData()) {
                ((FormActivity) getActivity()).new UploadMultipartTask().execute(photoArray);
                DatabaseHandler db = new DatabaseHandler(((FormActivity) getActivity()).getApplicationContext());
                String location = buildingSpinner.getSelectedItem().toString() + ", " + floorSpinner.getSelectedItem().toString() + ", " + roomText.getText().toString();
                String description = descriptionText.getText().toString() + extraLocation.getText().toString();;
                History indoorReport = new History(description, location);
                indoorReport.setPhotosPath(joinPhotoPath());
                db.addReport(indoorReport);
                db.close();
                Log.d(TAG, "Add indoor report to database " + indoorReport);
            } else {
                Toast.makeText(getActivity(), "Missing info.", Toast.LENGTH_LONG).show();
            }
        }

    }
}