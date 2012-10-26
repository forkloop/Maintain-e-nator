package com.herokuapp.maintainenator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class FullPhotoDialogFragment extends DialogFragment {

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
        Bitmap photo = Bitmap.createScaledBitmap(bmp, 420, 600, true);
        iv.setImageBitmap(photo);
        builder.setView(layout);
        return builder.create();
    }
}
