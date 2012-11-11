package com.herokuapp.maintainenator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

    private Button settingsButton;
    private Button createButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsButton = (Button) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(this);

        createButton = (Button) findViewById(R.id.create_button);
        createButton.setOnClickListener(this);

        ((Button) findViewById(R.id.view_button)).setOnClickListener(this);

        TextView welcomeView = (TextView) findViewById(R.id.welcome);
        Intent intent = getIntent();
        if (intent != null) {
            String name = (String) intent.getCharSequenceExtra("name");
            if (name != null) {
                welcomeView.setText(getString(R.string.welcome) + ", " + name);
            }
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.settings_button) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (viewId == R.id.create_button) {
            startActivity(new Intent(this, FormActivity.class));
        } else if (viewId == R.id.view_button) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}