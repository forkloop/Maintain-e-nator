package com.herokuapp.maintainenator.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.herokuapp.maintainenator.R;

public class ImageTextAdapter extends ArrayAdapter<String> {

    private static final String[] buildings = {"Davis", "Furnas", "Jarvis", "Ketter"};
    private static final Integer[] images = {R.drawable.davis, R.drawable.furnas, R.drawable.jarvis, R.drawable.ketter};
    private Context context;

    public ImageTextAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId, buildings);
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
     return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
     return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
           LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
           View row = inflater.inflate(R.layout.building_row, parent, false);
           TextView label = (TextView) row.findViewById(R.id.spinner_building_text);
           label.setText(buildings[position]);
           ImageView icon = (ImageView) row.findViewById(R.id.spinner_building_image);
           icon.setImageResource(images[position]);
           return row;
          }
}
