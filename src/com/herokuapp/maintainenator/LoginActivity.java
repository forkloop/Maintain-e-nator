package com.herokuapp.maintainenator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener{

    private static final String PREFS_FILE = "maintainenator";
    private static final String TAG = "LoginActivity";

    private static final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
    private AccountManager accountManager;
    private Account account;

    private AnimatorSet animatorSet;

    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editor = getSharedPreferences(PREFS_FILE, MODE_PRIVATE).edit();

        final Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);
        final Button nothankButton = (Button) findViewById(R.id.nothank_button);
        nothankButton.setOnClickListener(this);

        animatorSet = new AnimatorSet();
        AnimatorSet fadeinAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.fadein);
        fadeinAnimator.setTarget(loginButton);
        ValueAnimator animator = ObjectAnimator.ofFloat(nothankButton, "alpha", 0f, 1f);
        animator.setDuration(500);
        animatorSet.play(fadeinAnimator).with(animator);

        accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Log.d(getClass().getSimpleName(), "# of account: " + accounts.length);
        if (accounts.length > 0) {
            account = accounts[0];
            Log.d(getClass().getSimpleName(), account.name);
        }
        //accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, null, this, new OnTokenAcquired(), null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        animatorSet.start();
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            try {
                String authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                Log.d(getClass().getSimpleName(), "authToken: " + authToken);
                if (authToken != null) {
                    LoginAsyncTask loginTask = new LoginAsyncTask();
                    loginTask.execute(new String[] {authToken});
                    String name = loginTask.get();
                    if (name != null) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        /* Write the name and email into shared preference. */
                        Log.d(TAG, name);
                        editor.putString("submitter", name);
                        editor.putString("sub_email", account.name);
                        editor.commit();

                        startActivity(intent);
                        finish();
                    }
                }
            } catch (OperationCanceledException oce) {
                Toast.makeText(getApplicationContext(), "Authorization canceled: " + oce.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.d(getClass().getSimpleName(), "Authorization exception: " + e.getMessage());
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_button) {
            Bundle options = new Bundle();
            accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, options, this, new OnTokenAcquired(), null);
        } else if (v.getId() == R.id.nothank_button) {
            startActivity(new Intent(this, MainActivity.class));
            /* Write the name and email into shared preference. */
            editor.putString("submitter", "");
            editor.commit();
            //Make MainActivity as the bottom activity of activity stack.
            finish();
        }
    }

    private class LoginAsyncTask extends AsyncTask<String, Integer, String> {

        private String getJSONResponse(InputStream in) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                bufferedReader.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return null;
            }
            return sb.toString();
        }

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            HttpURLConnection conn = null;
            try {
                Log.d(getClass().getSimpleName(), "https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + params[0]);
                url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + params[0]);
            } catch (MalformedURLException mue) {
                mue.printStackTrace();
                Log.e(getClass().getSimpleName(), mue.getMessage());
                return null;
            }
            try {
                conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String jsonString = getJSONResponse(conn.getInputStream());
                    Log.d(getClass().getSimpleName(), jsonString);
                    JSONObject json = new JSONObject(jsonString);
                    return json.getString("given_name");
                } else {
                    Log.d(getClass().getSimpleName(), conn.getResponseMessage());
                    return null;
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Log.d(getClass().getSimpleName(), ioe.getMessage());
                return null;
            } catch (JSONException je) {
                je.printStackTrace();
                Log.e(getClass().getSimpleName(), je.getMessage());
                return null;
            } finally {
                conn.disconnect();
            }
        }
    }
}