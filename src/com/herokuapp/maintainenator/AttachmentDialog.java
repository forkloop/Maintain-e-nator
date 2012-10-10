package com.herokuapp.maintainenator;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AttachmentDialog extends DialogFragment implements OnItemClickListener {

    private static final String TITLE = "Attachment";
    private String[] attachmentList = {"Photo", "Video"};

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
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((DisplayCreateFormActivity) getActivity()).toast("Clicked " + attachmentList[position]);
        this.dismiss();
    }
}
