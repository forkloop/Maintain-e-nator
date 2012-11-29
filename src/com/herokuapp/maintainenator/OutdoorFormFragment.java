package com.herokuapp.maintainenator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.herokuapp.maintainenator.FormActivity.AudioPlayListener;

public class OutdoorFormFragment extends Fragment implements OnLongClickListener, OnClickListener {

    private static final String AUDIO_DIR = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_MUSIC + File.separator;
    private static final String TAG = "OutdoorFormFragment";
    private static final int MAP_REQUEST_CODE = 123;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final String END = "\r\n";
    private static final String BOUNDARY = "1q2w3e4r5t";
    private static final String TWO_HYPHENS = "--";
    private static final int MAX_PHOTO_NUM = 3;
    private static final String PHOTO_PATH_SEPARATOR = "##";

    private String[] photoAudioArray;
    private EditText descriptionText;
    private EditText locationText;
    private ImageView imageView;
    private Button mapButton;
    private int longClickedId;

    // Audio.
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private Button recordButton;
    private ImageView audioView;
    private String audioFilePath;
    private boolean sendAudioFile = false;
    private AlertDialog audioRemovalDialog = null;

    private FormActivity parentActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parentActivity = (FormActivity) getActivity();
        photoAudioArray = new String[MAX_PHOTO_NUM + 1];

        mediaRecorder = new MediaRecorder();
    }

    @Override
    public void onDestroy() {
        mediaRecorder.release();
        mediaRecorder = null;
        super.onDestroy();
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

        //Audio.
        recordButton = (Button) layout.findViewById(R.id.outdoor_audio);
        recordButton.setOnTouchListener(new ButtonTouchListener());
        audioView = (ImageView) layout.findViewById(R.id.outdoor_audio_record);
        audioView.setOnLongClickListener(this);
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
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.outdoor_audio_record) {
            if (sendAudioFile) {
                generateAudioRemovalDialog().show();
            }
        } else {
            longClickedId = v.getId();
            PhotoActionDialogFragment photoActionDialog = new PhotoActionDialogFragment();
            photoActionDialog.show(getActivity().getFragmentManager(), "photo_action");
        }
        return true;
    }

    public int getLongClickedId() {
        return longClickedId;
    }

    private AlertDialog generateAudioRemovalDialog() {
        if (audioRemovalDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete this audio message?")
                       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendAudioFile = false;
                            audioView.setImageBitmap(null);
                            audioView.setOnClickListener(null);
                            audioView.setClickable(false);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            audioRemovalDialog = builder.create();
        }
        return audioRemovalDialog;
}
    // Invoked by FormActivity when add new photo
    void setPhotoPath(int viewId, String path) {
        if (viewId == R.id.outdoor_image_view1) {
            photoAudioArray[0] = path;
        } else if (viewId == R.id.outdoor_image_view2) {
            photoAudioArray[1] = path;
        } else if (viewId == R.id.outdoor_image_view3) {
            photoAudioArray[2] = path;
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
            if (photoAudioArray[index] != null && !photoAudioArray[index].isEmpty()) {
                if (first) {
                    first = false;
                    sb.append(photoAudioArray[index]);
                } else {
                    sb.append(PHOTO_PATH_SEPARATOR);
                    sb.append(photoAudioArray[index]);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult" + requestCode + ", " + resultCode);
        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            int latitude = data.getIntExtra("lat", 0);
            int longitude = data.getIntExtra("long", 0);
            Toast.makeText(getActivity(), latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
        }
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
                startActivityForResult(intent, MAP_REQUEST_CODE);
            } else {
                Toast.makeText(getActivity(), "Can't acquire current location.", Toast.LENGTH_LONG).show();
            }
        } else if (vid == R.id.outdoor_submit) {
            if (checkData()) {
                photoAudioArray[3] = audioFilePath;
                ((FormActivity) getActivity()).new UploadMultipartTask().execute(photoAudioArray);
                DatabaseHandler db = new DatabaseHandler(((FormActivity) getActivity()).getApplicationContext());
                History outdoorReport = new History (descriptionText.getText().toString(), locationText.getText().toString());
                outdoorReport.setPhotosPath(joinPhotoPath());
                outdoorReport.setAudioPath(audioFilePath);
                db.addReport(outdoorReport);
                db.close();
                Log.d(getClass().getSimpleName(), "Add outdoor report to database " + outdoorReport);
            } else {
                Toast.makeText(getActivity(), "Please fill in.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ButtonTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "Start recording...");
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                Date date = new Date();
                audioFilePath = AUDIO_DIR + "audio-" + DATE_FORMAT.format(date) + ".mp4";
                mediaRecorder.setOutputFile(audioFilePath);
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException ioe) {
                    Log.e(TAG, ioe.getMessage());
                }
            } else if (action == MotionEvent.ACTION_UP) {
                Log.d(TAG, "Stop recording...");
                mediaRecorder.stop();
                sendAudioFile = true;
                if (!audioView.isClickable()) {
                    audioView.setOnClickListener(new AudioPlayListener(mediaPlayer, audioFilePath));
                }
                audioView.setImageResource(R.drawable.audio_record);
            }
            // No more actions for following receivers.
            return true;
        }
    }

}