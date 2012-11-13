package com.herokuapp.maintainenator;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailReportActivity extends Activity {

    private static final String PHOTO_PATH_SEPARATOR = "##";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_report_view);
        Intent intent = getIntent();
        int rowid = intent.getIntExtra("rowid", 0);
        if (rowid > 0) {
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            History history = db.getReportById(rowid);
            ((TextView) findViewById(R.id.detail_report_description)).setText(history.getDescription());
            ((TextView) findViewById(R.id.detail_report_location)).setText(history.getLocation());
            String photosPath = history.getPhotosPath();
            if (!photosPath.isEmpty()) {
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
        }
    }
}
