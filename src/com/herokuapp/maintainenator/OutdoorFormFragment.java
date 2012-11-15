package com.herokuapp.maintainenator;

import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
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

    private static final String TAG = "OutdoorFormFragment";

    private static final String END = "\r\n";
    private static final String BOUNDARY = "1q2w3e4r5t";
    private static final String TWO_HYPHENS = "--";
    private static final int MAX_PHOTO_NUM = 3;
    private static final String PHOTO_PATH_SEPARATOR = "##";

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
        if (locationText.getText().toString().isEmpty()) {
            String cachedLocation = ((FormActivity) getActivity()).getCachedLocation();
            if (cachedLocation != null) {
                locationText.setText(cachedLocation);
            }
        }
//        LocationManager locationManager = ((FormActivity) getActivity()).getLocationManager();
//        // Seems network location will suppress gps location update...
//        //locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, (LocationListener) getActivity(), null);
//        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, (LocationListener) getActivity(), null);
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
        Location latestLocation = ((FormActivity) getActivity()).getLatestLocation();
        if (latestLocation != null) {
            sb.append(TWO_HYPHENS + BOUNDARY + END);
            sb.append("Content-Disposition: form-data; name=\"latitude\"" + END + END + latestLocation.getLatitude() + END);
            sb.append(TWO_HYPHENS + BOUNDARY + END);
            sb.append("Content-Disposition: form-data; name=\"longitude\"" + END + END + latestLocation.getLongitude() + END);
        }
        return sb.toString();
    }

    private boolean checkData() {
        if (descriptionText.getText().toString().isEmpty() || locationText.getText().toString().isEmpty()) {
            return false;
        }
        return true;
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

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.map_button) {
            Location latestLocation = ((FormActivity) getActivity()).getLatestLocation();
            if (latestLocation != null) {
                Intent intent = new Intent(getActivity(), MapViewActivity.class);
                intent.putExtra("latitude", (int) (latestLocation.getLatitude() * 1000000));
                intent.putExtra("longitude", (int) (latestLocation.getLongitude() * 1000000));
                Log.d(TAG, latestLocation.getLatitude() + ", " + latestLocation.getLongitude());
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "Can't acquire current location.", Toast.LENGTH_LONG).show();
            }
        } else if (vid == R.id.outdoor_submit) {
            if (checkData()) {
                ((FormActivity) getActivity()).new UploadMultipartTask().execute(photoArray);
                DatabaseHandler db = new DatabaseHandler(((FormActivity) getActivity()).getApplicationContext());
                History outdoorReport = new History (descriptionText.getText().toString(), locationText.getText().toString());
                outdoorReport.setPhotosPath(joinPhotoPath());
                db.addReport(outdoorReport);
                db.close();
                Log.d(getClass().getSimpleName(), "Add outdoor report to database " + outdoorReport);
            } else {
                Toast.makeText(getActivity(), "Please fill in.", Toast.LENGTH_LONG).show();
            }
        }
    }

}