package com.kainosdub.rpbank;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private Spinner languageSpinner;
    private ImageButton recordButton;
    private Button goToLoginButton, goToRegisterButton;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private final String [] languages = {"english", "tamil"};
    private boolean recording;
    private MediaRecorder mediaRecorder;
    ExecutorService executorService;
    String filePath, language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        initialize();
        addListeners();
    }

    private void initialize() {
        languageSpinner = findViewById(R.id.languageSpinner);
        recordButton = findViewById(R.id.recordButton);
        goToLoginButton = findViewById(R.id.goToLoginButton);
        goToRegisterButton = findViewById(R.id.goToRegisterButton);
        recording = false;
        mediaRecorder = new MediaRecorder();
        executorService = Executors.newFixedThreadPool(2);
        filePath = getApplicationContext().getFilesDir().getAbsolutePath();
        filePath += "/record.mp4";
        language = languages[languageSpinner.getSelectedItemPosition()];
    }

    private void addListeners() {
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                language = languages[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recording) {
                    mediaRecorder.stop();
                    recordButton.setBackground(getResources().getDrawable(R.drawable.gray_rounded_bg, getTheme()));
                    mediaRecorder.reset();
                    recording = false;

                    Log.d("debug", String.format("Filepath=%s, language=%s", filePath, language));
                    Future<String> result = executorService.submit(new ASRTask(filePath, language));
                    String trans = "";
                    try {
                        while(!result.isDone() && !result.isCancelled()) {
                            Thread.sleep(100);
                        }
                        if(result.isDone()) {
                            trans = result.get();
                        }
                    } catch (InterruptedException e) {
                        // ignore error
                        Log.d("error", e.toString());
                    } catch (ExecutionException e) {
                        // ignore error
                        Log.d("error", e.toString());
                    }
                    Log.d("ASRTaskOutput", trans);
                    Toast.makeText(getApplicationContext(), "--" + trans + "--", Toast.LENGTH_LONG).show();

                    // Identify the command and execute it
                    Future<Command> commandResult = executorService.submit(new SpeechToCommandTask(trans, language));
                    Command command = null;
                    try {
                        while(!commandResult.isDone() && !commandResult.isCancelled()) {
                            Thread.sleep(100);
                        }
                        if(commandResult.isDone()) {
                            command = commandResult.get();
                        }
                    } catch (InterruptedException e) {
                        // ignore error
                        Log.d("error", e.toString());
                    } catch (ExecutionException e) {
                        // ignore error
                        Log.d("error", e.toString());
                    }
                    Toast.makeText(getApplicationContext(), "Command: " + command , Toast.LENGTH_LONG).show();
                    switch (command) {
                        case LOGIN:
                            startLoginActivity();
                            break;
                        case REGISTER:
                            startRegisterActivity();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "Unrecognized command: " + trans, Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(isOnline()) {
                        prepareRecorder();
                        recordButton.setBackground(getResources().getDrawable(R.drawable.green_rounded_bg, getTheme()));
                        recording = true;
                        mediaRecorder.start();
                    } else {
                        createNetworkWarningMessage();
                    }
                }
            }
        });

        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginActivity();
            }
        });

        goToRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegisterActivity();
            }
        });
    }

    private void startRegisterActivity() {
        Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void startLoginActivity() {
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginIntent);
    }

    private void createNetworkWarningMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("You must be connected to Internet for this functionality to work");
        builder.setTitle("Network Error !");
        builder.setCancelable(true);

        // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setNegativeButton("Ok", (DialogInterface.OnClickListener) (dialog, which) -> {
            // If user click no then dialog box is canceled.
            dialog.cancel();
        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();
        // Show the Alert Dialog box
        alertDialog.show();
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
        } catch (Exception e) {
            // ignore exception
            Log.d("debug", e.toString());
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}