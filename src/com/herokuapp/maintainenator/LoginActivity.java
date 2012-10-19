package com.herokuapp.maintainenator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener{

    private static final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/userinfo.email";
    private AccountManager accountManager;
    private Account account;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ((Button) findViewById(R.id.login_button)).setOnClickListener(this);

        accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Log.d(getClass().getSimpleName(), "# of account: " + accounts.length);
        if (accounts.length > 0) {
            account = accounts[0];
        }
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
                //TODO start main activity
                Log.d(getClass().getSimpleName(), "authToken: " + authToken);
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
        }
    }

}