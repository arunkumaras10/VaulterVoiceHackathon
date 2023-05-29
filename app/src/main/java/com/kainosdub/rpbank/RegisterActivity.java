package com.kainosdub.rpbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextNickname, editTextUsername, editTextPhone, editTextPIN;
    private Button registerButton, playPauseButton;
    private ImageButton recordButton;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private TextView registerErrorMessage;
    ExecutorService executorService;
    boolean recording = false, enrollmentRequired = false;
    private String filePath;
    private final String requiredFieldErrorString = "This field is required";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        initialize();
        setListeners();
    }

    private void initialize() {
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextNickname = findViewById(R.id.editTextNickname);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPIN = findViewById(R.id.editTextPIN);
        recordButton = findViewById(R.id.registerRecordButton);
        playPauseButton = findViewById(R.id.playPauseButton);
        registerButton = findViewById(R.id.registerButton);
        registerErrorMessage = findViewById(R.id.registerErrorMessage);
        mediaRecorder = new MediaRecorder();
        executorService = Executors.newFixedThreadPool(1);
        filePath = getApplicationContext().getFilesDir().getAbsolutePath() + "/enrollment.mp4";
    }

    private boolean validateAllFields() {
        if(editTextFullName.length() == 0) {
            editTextFullName.setError(requiredFieldErrorString);
            return false;
        }
        if(editTextNickname.length() == 0) {
            editTextNickname.setError(requiredFieldErrorString);
            return false;
        }
        if(editTextUsername.length() == 0) {
            editTextUsername.setError(requiredFieldErrorString);
            return false;
        }
        if(editTextPhone.length() == 0) {
            editTextPhone.setError(requiredFieldErrorString);
            return false;
        }
        if(editTextPIN.length() == 0) {
            editTextPIN.setError(requiredFieldErrorString);
            return false;
        }
        return true;
    }
    private boolean validateMobileNumber() {
        String phone = editTextPhone.getText().toString().trim();
        if(phone.length() != 10) {
            editTextPhone.setError("Mobile number must be of 10 digits");
            return false;
        }
        return true;
    }

    private boolean validatePin() {
        String pin = editTextPIN.getText().toString().trim();
        if(pin.length() != 6) {
            editTextPIN.setError("PIN number must be of 6 digits");
            return false;
        }
        return true;
    }

    private void setListeners() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateAllFields() || !validateMobileNumber() || !validatePin()) {
                    return;
                }
                String fullName = editTextFullName.getText().toString().trim();
                String nickname = editTextNickname.getText().toString().trim();
                String username = editTextUsername.getText().toString().trim();
                String phone = editTextPhone.getText().toString().trim();
                String pin = editTextPIN.getText().toString().trim();

                JSONObject jsonPayload = new JSONObject();
                try {
                    jsonPayload.put("action", "register");
                    jsonPayload.put("nick_name", nickname);
                    jsonPayload.put("full_name", fullName);
                    jsonPayload.put("user_name", username);
                    jsonPayload.put("pin_number", pin);
                    jsonPayload.put("mob_number", phone);
                    jsonPayload.put("upi_id", nickname + "@rpbank");
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
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        if(status != null && status.compareTo("success") == 0) {
                            // Registration success
                            registerErrorMessage.setText("User registration successful. You can login now.");
                            // enroll speaker if required
                            if(enrollmentRequired) {
                                enrollSpeech(nickname);
                            }
                        } else if(status != null && status.compareTo("failed") == 0){
                            String reason = jsonResponse.has("reason")? ": " + jsonResponse.getString("reason") : "";
                            registerErrorMessage.setText("Registration failed" + reason);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error: " + response, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                }
            }
        });

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                } else {
                    mediaPlayer.pause();
                }
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording) {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    recordButton.setBackground(getResources().getDrawable(R.drawable.gray_rounded_bg, getTheme()));
                    recording = false;
                    try {
                        mediaPlayer.setDataSource(filePath);
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    playPauseButton.setVisibility(View.VISIBLE);
                    enrollmentRequired = true;
                } else {
                    prepareRecorder();
                    recordButton.setBackground(getResources().getDrawable(R.drawable.green_rounded_bg, getTheme()));
                    recording = true;
                    mediaRecorder.start();
                }
            }
        });
    }

    private void enrollSpeech(String nickname) {
        Future<Boolean> result = executorService.submit(new SpeakerVerificationTask(filePath, nickname, true));
        try {
            while (!result.isDone() && !result.isCancelled()) {
                Thread.sleep(100);
            }
            if (result.isDone()) {
                result.get();
            }
        } catch (InterruptedException e) {
            // ignore error
            Log.d("error", e.toString());
        } catch (ExecutionException e) {
            // ignore error
            Log.d("error", e.toString());
        }
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

    public void prepareRecorder() {
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(filePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setAudioEncodingBitRate(16);
            mediaRecorder.setAudioSamplingRate(16000);
            mediaRecorder.prepare();
            Log.d("debug", "media recorder set for prepare");
        } catch (Exception e) {
            // ignore exception
            Log.d("debug", e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}