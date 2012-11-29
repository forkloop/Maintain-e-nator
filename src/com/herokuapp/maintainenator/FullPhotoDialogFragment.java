package com.herokuapp.maintainenator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class FullPhotoDialogFragment extends DialogFragment {

    private static final String TAG = "FullPhotoDialogFragment";
    private final int maxHeight = 560;
    private String photoURI;

    public void setPhotoURI(String photoURI) {
        this.photoURI = photoURI;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.fragment_full_photo, null);
        ImageView iv = (ImageView) layout.findViewById(R.id.full_photo);
        Bitmap bmp = BitmapFactory.decodeFile(photoURI);
        int height = bmp.getHeight();
        int width = bmp.getWidth();
        float ratio = (float) width / height;
        Log.d(TAG, "width/height => ratio: " + ratio);
        Bitmap scaledBmp = null;
        if (width > height) {
            Matrix matrix = new Matrix();
            float scale = (float) maxHeight / height;
            matrix.postScale(scale, scale);
            matrix.postRotate(90);
            scaledBmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, false);
        } else {
            scaledBmp = Bitmap.createScaledBitmap(bmp, (int) (ratio * maxHeight), maxHeight, false);
        }
        iv.setImageBitmap(scaledBmp);
        builder.setView(layout);
        return builder.create();
    }
}