package com.kainosdub.rpbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivity {
    EditText loginNickname, loginPIN;
    TextView loginErrorMessage;
    Button loginButton;
    ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        initialize();
        setListeners();
    }

    private void initialize() {
        loginNickname = findViewById(R.id.loginNickname);
        loginPIN = findViewById(R.id.loginPIN);
        loginButton = findViewById(R.id.loginButton);
        loginErrorMessage = findViewById(R.id.loginErrorMessage);
        executorService = Executors.newFixedThreadPool(1);
    }

    private void setListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clear existing error message
                loginErrorMessage.setText("");

                String providedNickname = loginNickname.getText().toString().trim();
                String providedPIN = loginPIN.getText().toString().trim();
                JSONObject jsonPayload = new JSONObject();
                try {
                    jsonPayload.put("action", "details");
                    jsonPayload.put("nick_name", providedNickname);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Future<String> result = executorService.submit(new RPBankAPITask(jsonPayload));
                String response = null;
                try {
                    while(!result.isDone() && !result.isCancelled()) {
                        Thread.sleep(100);
                    }
                    if(result.isDone()) {
                        response = result.get();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                }
                if(response != null) {
                    if(response.compareTo("None") == 0) {
                        loginErrorMessage.setText("User does not exist");
                    } else {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String actualPin = jsonResponse.getString("pin_number");
                            if(actualPin != null && actualPin.trim().compareTo(providedPIN) == 0) {
                                // Login success and send to user home
                                Intent userHomeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                                userHomeIntent.putExtra("jsonData", response);
                                startActivity(userHomeIntent);
                            } else {
                                loginErrorMessage.setText("Incorrect password");
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: "+response, Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}