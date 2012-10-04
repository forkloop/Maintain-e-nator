package com.herokuapp.maintainenator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DisplayCreateFormActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        
        setContentView(R.layout.activity_display_create_form);
        
    }
}
