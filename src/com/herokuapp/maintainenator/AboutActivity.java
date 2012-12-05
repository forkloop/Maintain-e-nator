package com.herokuapp.maintainenator;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends Activity {

    private static final String INFORMATION = 
            "Copyright 2012 Maintain-e-nator group.\n" +
            "MIT Licensed.\n" +
            "Xin Liu (forkloop@gmail.com)\n" +
            "Juehui Zhang (juehuizh@gmail.com)\n" +
            "John Longanecker (johnlonganecker@gmail.com)\n";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView appInfo = (TextView) findViewById(R.id.app_info);
        appInfo.setText(INFORMATION);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                // Consume the menu processing here.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
