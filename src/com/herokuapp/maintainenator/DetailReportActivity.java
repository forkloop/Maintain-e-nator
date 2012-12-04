package com.herokuapp.maintainenator;

import java.io.IOException;
import java.util.Arrays;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailReportActivity extends Activity {

    private static final String PHOTO_PATH_SEPARATOR = "##";
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_report_view);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        int rowid = intent.getIntExtra("rowid", 0);
        if (rowid > 0) {
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            History history = db.getReportById(rowid);
            setTitle(history.getDescription());
            ((TextView) findViewById(R.id.detail_report_description)).setText(history.getDescription());
            ((TextView) findViewById(R.id.detail_report_location)).setText(history.getLocation());
            String photosPath = history.getPhotosPath();
            if (photosPath != null && !photosPath.isEmpty()) {
                String[] photos = photosPath.split(PHOTO_PATH_SEPARATOR);
                Log.d(getClass().getSimpleName(), Arrays.toString(photos));
                if (photos.length > 0) {
                    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.detail_report_photo_scroll);
                    for (int index=0; index<photos.length; index++) {
                        ImageView iv = new ImageView(this);
                        Bitmap bmp = BitmapFactory.decodeFile(photos[index]);
                        Bitmap photo = Bitmap.createScaledBitmap(bmp, 450, 300, true);
                        iv.setImageBitmap(photo);
                        linearLayout.addView(iv);
                    }
                }
            }
            String audioFile = history.getAudioPath();
            ImageView audioView = (ImageView) findViewById(R.id.detail_report_audio);
            if (audioFile != null && !audioFile.isEmpty()) {
                audioView.setOnClickListener(new AudioPlayClickListener(audioFile));
            } else {
                audioView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onStop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = new Intent(this, ReportActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private class AudioPlayClickListener implements OnClickListener {

        private static final String TAG = "DetailReportActivity$AudioPlayClickListener";
        private String audioFile;

        public AudioPlayClickListener(String audioFile) {
            this.audioFile = audioFile;
        }

        @Override
        public void onClick(View v) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.reset();
                        }
                    });
                }
                try {
                    mediaPlayer.setDataSource(audioFile);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    Log.d(TAG, ioe.getMessage());
                }
            }
        }

}