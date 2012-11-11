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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class OutdoorFormFragment extends Fragment implements OnLongClickListener, OnClickListener {

    private static final String END = "\r\n";
    private static final String BOUNDARY = "1q2w3e4r5t";
    private static final String TWO_HYPHENS = "--";
    private static final int MAX_PHOTO_NUM = 3;

    private String[] photoArray;
    private EditText descriptionText;
    private EditText locationText;
    private ImageView imageView;
    private Button mapButton;
    private int longClickedId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoArray = new String[MAX_PHOTO_NUM];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = (LinearLayout) inflater.inflate(R.layout.fragment_outdoor_form, container, false);

        layout.findViewById(R.id.outdoor_submit).setOnClickListener(this);
        descriptionText = (EditText) layout.findViewById(R.id.outdoor_description);
        locationText = (EditText) layout.findViewById(R.id.outdoor_address);
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

    // Invoked by FormActivity when add new photo
    void setPhotoPath(int viewId, String path) {
        if (viewId == R.id.outdoor_image_view1) {
            photoArray[0] = path;
        } else if (viewId == R.id.outdoor_image_view2) {
            photoArray[1] = path;
        } else if (viewId == R.id.outdoor_image_view3) {
            photoArray[2] = path;
        }
    }

    //Invoked by UploadMultipartTask
    String generateMultipartForm() {
        StringBuilder sb = new StringBuilder();
        sb.append(TWO_HYPHENS + BOUNDARY + END);
        sb.append("Content-Disposition: form-data; name=\"description\"" + END + END + descriptionText.getText().toString() + END);
        sb.append(TWO_HYPHENS + BOUNDARY + END);
        sb.append("Content-Disposition: form-data; name=\"location\"" + END + END + locationText.getText().toString() + END);
        return sb.toString();
    }

    private boolean checkData() {
        if (descriptionText.getText().toString().isEmpty() || locationText.getText().toString().isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.map_button) {
            startActivity(new Intent(getActivity(), MapViewActivity.class));
        } else if (vid == R.id.outdoor_submit) {
            if (checkData()) {
                ((FormActivity) getActivity()).new UploadMultipartTask().execute(photoArray);
            } else {
                Toast.makeText(getActivity(), "Please fill in.", Toast.LENGTH_LONG).show();
            }
        }
    }
}