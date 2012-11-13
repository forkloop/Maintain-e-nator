package com.herokuapp.maintainenator;

import java.util.List;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class ReportActivity extends ListActivity implements OnItemClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);

      DatabaseHandler db = new DatabaseHandler(getApplicationContext());
      List<History> values = db.getAllReports();

      // show the elements in a ListView
      ArrayAdapter<History> adapter = new ArrayAdapter<History>(this,
          android.R.layout.simple_list_item_1, values);
      setListAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        //TODO info dialog
    }
}