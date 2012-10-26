package com.herokuapp.maintainenator;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

public class FormTabListener<T extends Fragment> implements ActionBar.TabListener {

    private Fragment fragment;
    private final Activity activity;
    private final String tabName;
    private final Class<T> cls;

    public FormTabListener(Activity activity, String tabName, Class<T> cls) {
        this.activity = activity;
        this.tabName = tabName;
        this.cls = cls;
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        if (fragment != null) {
            ft.attach(fragment);
        } else {
            fragment = Fragment.instantiate(activity, cls.getName());
            ft.add(android.R.id.content, fragment, tabName);
            if (tabName.equals("indoor")) {
                ((FormActivity) activity).setIndoorFormFragment(fragment);
            } else if (tabName.equals("outdoor")) {
                ((FormActivity) activity).setOutdoorFormFragment(fragment);
            }
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        if (fragment != null) {
            ft.detach(fragment);
        }
    }

}