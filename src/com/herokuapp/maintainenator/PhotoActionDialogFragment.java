package com.herokuapp.maintainenator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class PhotoActionDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Log.d(getClass().getSimpleName(), "Associcated activity: " + getActivity());
        builder.setTitle("Actions for photo")
                   .setItems(R.array.photo_action, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                   //XXX The 'which' argument contains the index position of the selected item
               }
        });
        return builder.create();
    }
}