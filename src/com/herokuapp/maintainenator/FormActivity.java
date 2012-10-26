package com.herokuapp.maintainenator;

import java.io.File;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class FormActivity extends Activity {

    private static final int CAMERA_REQUEST = 0;
    private static final int ALBUM_REQUEST = 1;
    private Activity currentActivity;
    private ActionBar actionBar;
    private IndoorFormFragment indoorFormFragment;
    private OutdoorFormFragment outdoorFormFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentActivity = this;
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Tab tab = actionBar.newTab()
                       .setText(R.string.indoor)
                       .setTag("indoor")
                       .setTabListener(new FormTabListener<IndoorFormFragment>(this, "indoor", IndoorFormFragment.class));
        actionBar.addTab(tab);

        tab = actionBar.newTab()
                .setText(R.string.outdoor)
                .setTag("outdoor")
                .setTabListener(new FormTabListener<OutdoorFormFragment>(this, "outdoor", OutdoorFormFragment.class));
        actionBar.addTab(tab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clip:
                return true;
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getSimpleName(), "requstCode " + requestCode + ", resultCode " + resultCode);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            String photoURI = PhotoActionDialogFragment.getNewPhotoName();
            galleryAddPicture(photoURI);
            displayPhoto(photoURI);
            Log.d(getClass().getSimpleName(), "Camera: display the picture.");
        } else if (requestCode == ALBUM_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri photoUri = data.getData();
            if (photoUri != null) {
                Log.d(getClass().getSimpleName(), "Picture: " + photoUri);
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(photoUri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                displayPhoto(filePath);
                Log.d(getClass().getSimpleName(), "Gallery: display the picture.");
            }
        }
    }

    public void setIndoorFormFragment(Fragment fragment) {
        indoorFormFragment = (IndoorFormFragment) fragment;
    }

    public void setOutdoorFormFragment(Fragment fragment) {
        outdoorFormFragment = (OutdoorFormFragment) fragment;
    }

    private void galleryAddPicture(String photoURI) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoURI);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    public void displayPhoto(String photoURI) {
        Tab tab = actionBar.getSelectedTab();
        if (tab.getTag().equals("indoor")) {
            Bitmap bmp = BitmapFactory.decodeFile(photoURI);
            Bitmap photo = Bitmap.createScaledBitmap(bmp, 200, 140, true);
            ImageView imageView = (ImageView) findViewById(IndoorFormFragment.getLongClickedId());
            imageView.setImageBitmap(photo);
            imageView.setOnClickListener(new PhotoClickListener(photoURI));
            enableNextPhoto(IndoorFormFragment.getLongClickedId());
        } else {
            //TODO outdoor
        }
    }

    public void deletePhoto() {
        if (actionBar.getSelectedTab().getTag().equals("indoor")) {
            ImageView imageView = (ImageView) findViewById(IndoorFormFragment.getLongClickedId());
            imageView.setOnClickListener(null);
            imageView.setImageBitmap(null);
            imageView.setImageResource(R.drawable.content_new_picture);
        }
    }

    private void enableNextPhoto(int currentId) {
        if (currentId == R.id.indoor_image_view1) {
            ImageView iv = (ImageView) findViewById(R.id.indoor_image_view2);
            if (!iv.isLongClickable()) {
                iv.setOnLongClickListener(indoorFormFragment);
                iv.setBackgroundColor(0xFF00BAFF);
                iv.setImageResource(R.drawable.content_new_picture);
                iv.setScaleType(ScaleType.CENTER);
            }
        } else if (currentId == R.id.indoor_image_view2) {
            ImageView iv = (ImageView) findViewById(R.id.indoor_image_view3);
            if (!iv.isLongClickable()) {
                iv.setOnLongClickListener(indoorFormFragment);
                iv.setImageResource(R.drawable.content_new_picture);
                iv.setScaleType(ScaleType.CENTER);
            }
        }
    }

    private class PhotoClickListener implements OnClickListener {

        private String photoURI;

        public PhotoClickListener(String photoURI) {
            this.photoURI = photoURI;
        }

        @Override
        public void onClick(View v) {
            FullPhotoDialogFragment dialog = new FullPhotoDialogFragment();
            dialog.setPhotoURI(photoURI);
            dialog.show(getFragmentManager(), "full_photo");
        }
    }
}