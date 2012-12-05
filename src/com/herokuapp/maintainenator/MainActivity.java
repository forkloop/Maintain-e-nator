package com.herokuapp.maintainenator;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

    private static final String PREFS_FILE = "maintainenator";
    private static final String TAG = "MainActivity";

    private static final long SLIDE_DURATION = 600;
    private Button settingsButton;
    private Button createButton;
    private Button reportButton;

    private Point windowSize;

    private AnimatorSet animatorSet;
    private ValueAnimator slideRightAnimator;
    private ValueAnimator slideTopAnimator;
    private ValueAnimator slideBottomAnimator;
    private ValueAnimator slideRight2Animator;

    private SharedPreferences sharedPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreference = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);

        /* Retrieve the window size. */
        windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);

        animatorSet = new AnimatorSet();

        settingsButton = (Button) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(this);
        slideBottomAnimator = ObjectAnimator.ofFloat(settingsButton, "y", windowSize.y);

        createButton = (Button) findViewById(R.id.create_button);
        createButton.setOnClickListener(this);
        slideTopAnimator = ObjectAnimator.ofFloat(createButton, "y", -windowSize.y);

        reportButton = (Button) findViewById(R.id.my_reports_button);
        reportButton.setOnClickListener(this);
        slideRightAnimator = ObjectAnimator.ofFloat(reportButton, "x", windowSize.x);

        Button aboutButton = (Button) findViewById(R.id.about_button);
        aboutButton.setOnClickListener(this);
        slideRight2Animator = ObjectAnimator.ofFloat(aboutButton, "x", windowSize.x);

        animatorSet.play(slideBottomAnimator).with(slideRightAnimator);
        animatorSet.play(slideBottomAnimator).with(slideTopAnimator).with(slideRight2Animator);

        TextView welcomeView = (TextView) findViewById(R.id.welcome);

        /* Check whether user logged in or not. */
        String submitter = sharedPreference.getString("submitter", "");
        Log.d(TAG, "submitter: " + submitter);
        if (!submitter.isEmpty()) {
            welcomeView.setText(getString(R.string.welcome) + ", " + submitter);
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        //if (viewId != R.id.about_button) {
            animatorSet.addListener(new ClickAnimatorListener(viewId, this));
            animatorSet.setDuration(SLIDE_DURATION);
            animatorSet.start();
        //}
        /*
        if (viewId == R.id.settings_button) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (viewId == R.id.create_button) {
            startActivity(new Intent(this, FormActivity.class));
        } else if (viewId == R.id.view_button) {
            //startActivity(new Intent(this, LoginActivity.class));
        } else if (viewId == R.id.my_reports_button) {
            startActivity(new Intent(this, ReportActivity.class));
        }
        */
    }

    private class ClickAnimatorListener implements AnimatorListener {

        private int vid;
        private Activity activity;

        public ClickAnimatorListener(int id, Activity parentActivity) {
            vid = id;
            activity = parentActivity;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (vid == R.id.settings_button) {
                Intent intent = new Intent(activity, SettingsActivity.class);
                startActivity(intent);
            } else if (vid == R.id.create_button) {
                startActivity(new Intent(activity, FormActivity.class));
            } else if (vid == R.id.my_reports_button) {
                startActivity(new Intent(activity, ReportActivity.class));
            } else if (vid == R.id.about_button) {
                startActivity(new Intent(activity, AboutActivity.class));
            }
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }
    }

}