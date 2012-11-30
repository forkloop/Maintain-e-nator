package com.herokuapp.maintainenator;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

    private Button settingsButton;
    private Button createButton;
    private Button reportButton;

    private Point windowSize;

    private AnimatorSet animatorSet;
    private ValueAnimator slideRightAnimator;
    private ValueAnimator slideTopAnimator;
    private ValueAnimator slideBottomAnimator;
    private ValueAnimator slideRight2Animator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        Button devButton = (Button) findViewById(R.id.view_button);
        devButton.setOnClickListener(this);
        slideRight2Animator = ObjectAnimator.ofFloat(devButton, "x", windowSize.x);

        animatorSet.play(slideBottomAnimator).with(slideRightAnimator);
        animatorSet.play(slideBottomAnimator).with(slideTopAnimator).with(slideRight2Animator);

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
        if (viewId != R.id.view_button) {
            animatorSet.addListener(new ClickAnimatorListener(viewId, this));
            animatorSet.setDuration(1000);
            animatorSet.start();
        }
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