package com.herokuapp.maintainenator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

public class ReportActivity extends Activity implements OnItemClickListener {
    private static final String PHOTO_PATH_SEPARATOR = "##";

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.simple_adapter);
      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);

      DatabaseHandler db = new DatabaseHandler(getApplicationContext());
      List<History> values = db.getAllReports();
      List<HashMap<String, Object>> data= this.getData(values);
      Log.d(getClass().getSimpleName(), String.valueOf(data.size()));

      // show the elements in a ListView
      SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.item,
              new String[] { "photoIcon", "description", "location" }, new int[] {
                      R.id.photoIcon, R.id.description, R.id.location });
      ListView listView = (ListView) findViewById(R.id.list_item);;
      listView.setAdapter(adapter);

      adapter.setViewBinder(new ViewBinder(){
          public boolean setViewValue(View view,Object data,String textRepresentation){
              if(view instanceof ImageView && data instanceof Bitmap){
                  setViewImage((ImageView) view, (Bitmap)data);
                  return true;
              }
              else return false;
          }
      });

      listView.setOnItemClickListener(this);
    }

    public void setViewImage(ImageView v, Bitmap bitmap) {
        v.setImageBitmap(bitmap);
    }

    public List<HashMap<String, Object>> getData(List<History> values) {
        List<HashMap<String, Object>> listData = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = null;

        for(History h : values) {
            map = new HashMap<String, Object>();
            String photosPath = h.getPhotosPath();
            String path = null;
            if (photosPath != null && !photosPath.isEmpty()) {
                String[] photos = photosPath.split(PHOTO_PATH_SEPARATOR);
                path = photos[0];
                Log.d(getClass().getSimpleName(), path);
            }
            Bitmap bmp = BitmapFactory.decodeFile(path);
            Bitmap photo = Bitmap.createScaledBitmap(bmp, 120, 80, true);
            map.put("photoIcon", photo);
            map.put("description", h.getDescription());
            map.put("location", h.getLocation());
            listData.add(map);
        }
        return listData;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        super.onBackPressed();
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
        Log.d(getClass().getSimpleName(), "position: " + position + ", id: " + id);
        Intent intent = new Intent(this, DetailReportActivity.class);
        // position start from 0, while rowid start from 1.
        intent.putExtra("rowid", position + 1);
        startActivity(intent);
    }
}