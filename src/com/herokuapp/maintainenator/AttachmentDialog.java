package com.herokuapp.maintainenator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class AttachmentDialog extends DialogFragment implements OnItemClickListener {

    private static final String TITLE = "Attachment";
    private String[] attachmentList = {"Camera", "Photo Albums", "Cancel"};
    private static final String STORAGEDIR = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_PICTURES + File.separator;
    private String mCurrentPhotoPath;
    private static final int CAMERA_REQUEST = 1;
    private static final int ABLUM_REQUEST = 2;
    private ImageView imageView;

    public AttachmentDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.attachment_dialog, container);
        getDialog().setTitle(TITLE);

        ListView listView = (ListView) view.findViewById(R.id.attachment_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                android.R.layout.simple_expandable_list_item_1, android.R.id.text1, attachmentList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);
        imageView = (ImageView) ((DisplayCreateFormActivity) getActivity()).findViewById(R.id.imageView);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0) {
            this.createImageFormat();
            this.takePicture();
        }
        else if(position == 1) {
            this.choosePicture();
        }
        else if(position == 2) {
            this.dismiss();
        }
    }

    private void createImageFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        String imageName = format.format(date);
        mCurrentPhotoPath = STORAGEDIR + imageName + ".jpg";
    }

    // Choose a photo from gallery
    private void choosePicture() {
        Intent ablumIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Log.d(getClass().getSimpleName(), "Choose picture from ablum");
        startActivityForResult(ablumIntent, ABLUM_REQUEST);
    }

    // Take a Photo with the Camera App and save it in /Pictures
    private void takePicture() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File out = new File(mCurrentPhotoPath);
        Uri uri = Uri.fromFile(out);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        Log.d(getClass().getSimpleName(), "Save image file to " + uri.toString());
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    // Receiving camera intent result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getSimpleName(), "requst code " + requestCode + " resultCode " + resultCode);
        if (requestCode == CAMERA_REQUEST && resultCode == DisplayCreateFormActivity.RESULT_OK) {
            Log.d(getClass().getSimpleName(), "picture from " + mCurrentPhotoPath);
            // Display the picture
            this.displayPicture(mCurrentPhotoPath);
            this.galleryAddPic();
            Log.d(getClass().getSimpleName(), "Camera - Display the picture in the imageview");
            this.dismiss();
        } else if(requestCode == ABLUM_REQUEST && resultCode == DisplayCreateFormActivity.RESULT_OK) {
            // Display the picture from gallery
            Uri photoUri = data.getData();
            if (photoUri != null) {
                Log.d(getClass().getSimpleName(), "picture from " + photoUri);
                try {
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    Cursor cursor = ((DisplayCreateFormActivity) getActivity()).getContentResolver().query(photoUri, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();
                    this.displayPicture(filePath);
                    Log.d(getClass().getSimpleName(), "Gallery - Display the picture in the imageview");
                    this.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.d(getClass().getSimpleName(), "Result code was " + resultCode);
        }
    }

    private void displayPicture(String fileName) {
        Bitmap bmp = BitmapFactory.decodeFile(fileName);
        Bitmap photo = Bitmap.createScaledBitmap(bmp, 360, 270, true);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
        imageView.setImageBitmap(rotatedBitmap);
    }

    // Invoke the system's media scanner to add the photo to the Media Provider's database
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        ((DisplayCreateFormActivity) getActivity()).sendBroadcast(mediaScanIntent);
    }
}
