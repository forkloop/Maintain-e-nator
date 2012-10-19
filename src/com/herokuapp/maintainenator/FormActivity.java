package com.herokuapp.maintainenator;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.os.Bundle;

public class FormActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Tab tab = actionBar.newTab()
                       .setText(R.string.indoor)
                       .setTabListener(new FormTabListener<IndoorFormFragment>(this, "indoor", IndoorFormFragment.class));
        actionBar.addTab(tab);

        tab = actionBar.newTab()
                .setText(R.string.outdoor)
                .setTabListener(new FormTabListener<OutdoorFormFragment>(this, "outdoor", OutdoorFormFragment.class));
        actionBar.addTab(tab);
    }

}