package com.herokuapp.maintainenator;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class ReportActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      DatabaseHandler db = new DatabaseHandler(getApplicationContext());
      List<History> values = db.getAllReports();

      // show the elements in a ListView
      ArrayAdapter<History> adapter = new ArrayAdapter<History>(this,
          android.R.layout.simple_list_item_1, values);
      setListAdapter(adapter);
    }
    
    @Override
    protected void onResume() {
      super.onResume();
    }

    @Override
    protected void onPause() {
      super.onPause();
    }
}
