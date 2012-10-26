package com.herokuapp.maintainenator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class PhotoActionDialogFragment extends DialogFragment {

    private static final String STORAGEDIR = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_PICTURES + File.separator;
    private static String newPhotoName;
    private static final int CAMERA_REQUEST = 0;
    private static final int ALBUM_REQUEST = 1;
    private static final int DELETE_REQUEST = 2;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Log.d(getClass().getSimpleName(), "Associcated activity: " + getActivity());
        builder.setTitle("Actions for photo")
                   .setItems(R.array.photo_action, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                       Log.d(getClass().getSimpleName(), "Choose " + which);
                       switch (which) {
                           case CAMERA_REQUEST:
                               setNewPhotoName();
                               takePicture();
                               break;
                           case ALBUM_REQUEST:
                               choosePicture();
                               break;
                           case DELETE_REQUEST:
                               ((FormActivity) getActivity()).deletePhoto();
                               break;
                           default:
                               return;
                       }
               }
        });
        return builder.create();
    }

    private void setNewPhotoName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        String photoName = format.format(date);
        newPhotoName = STORAGEDIR + photoName + ".jpg";
    }

    static String getNewPhotoName() {
        return newPhotoName;
    }

    private void choosePicture() {
        Intent albumIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Log.d(getClass().getSimpleName(), "Choose picture from album.");
        getActivity().startActivityForResult(albumIntent, ALBUM_REQUEST);
    }

    private void takePicture() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File out = new File(newPhotoName);
        Uri uri = Uri.fromFile(out);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        Log.d(getClass().getSimpleName(), "Save image file to " + uri.toString());
        getActivity().startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getSimpleName(), "requstCode " + requestCode + ", resultCode " + resultCode);
    }
}