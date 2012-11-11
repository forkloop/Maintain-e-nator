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
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener{

    private static final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
    private AccountManager accountManager;
    private Account account;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ((Button) findViewById(R.id.login_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.nothank_button)).setOnClickListener(this);

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
        // TODO implement the abstractaccountauthenticator
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
                        intent.putExtra("name", name);
                        intent.putExtra("email", account.name);
                        startActivity(intent);
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