package com.kainosdub.rpbank;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class BaseActivity extends AppCompatActivity {
    private Spinner languageSpinner;
    private ImageButton recordButton;
    private final String [] languages = {"english", "tamil"};
    private boolean recording;
    private MediaRecorder mediaRecorder;
    ExecutorService executorService;
    String filePath, language;
    protected JSONObject userData;
    Gson gson = new Gson();
    SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        Intent intent = getIntent();
        if(intent.hasExtra("jsonData")) {
            String jsonString = intent.getStringExtra("jsonData");
            try {
                userData = new JSONObject(jsonString);
            } catch (JSONException e) {
                // ignore error
            }
        }
        if(intent.hasExtra("language")) {
            language = intent.getStringExtra("language");
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

    protected void initialize() {
        languageSpinner = findViewById(R.id.baseLanguageSpinner);
        recordButton = findViewById(R.id.baseRecordButton);
        recording = false;
        mediaRecorder = new MediaRecorder();
        executorService = Executors.newFixedThreadPool(1);
        filePath = getApplicationContext().getFilesDir().getAbsolutePath();
        filePath += "/record.mp4";
        if(language != null && language.equalsIgnoreCase("tamil")) {
            // set language in spinner
            languageSpinner.setSelection(1);
        }
    }

    protected void addListeners() {
        Log.d("debug", "listeners adding");
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
                if (recording) {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    recordButton.setBackground(getResources().getDrawable(R.drawable.gray_rounded_bg, getTheme()));
                    recording = false;

                    if(! verifySpeaker()) {
                        createVoiceVerificationFailureMessage();
                        return;
                    }

                    Log.d("debug", String.format("Filepath=%s, language=%s", filePath, language));
                    String trans = getTranscription();
                    Log.d("ASRTaskOutput", trans);
                    Toast.makeText(getApplicationContext(), "--" + trans + "--", Toast.LENGTH_LONG).show();

                    // Identify the command and execute it
                    Command command = getCommand(trans);
                    Toast.makeText(getApplicationContext(), "Command: " + command, Toast.LENGTH_LONG).show();
                    switch (command) {
                        case CHECK_BALANCE:
                            double balance = getBalance();
                            Intent checkBalanceIntent = new Intent(getApplicationContext(), CheckBalanceActivity.class);
                            checkBalanceIntent.putExtra("balance", balance);
                            checkBalanceIntent.putExtra("jsonData", userData.toString());
                            checkBalanceIntent.putExtra("language", language);
                            finishIfRequired();
                            startActivity(checkBalanceIntent);
                            break;
                        case LAST_TEN_TRANSACTION_HISTORY:
                            ArrayList<TransactionHistory> history = getTransactionHistory();
                            Intent historyIntent = new Intent(getApplicationContext(), TransactionHistoryActivity.class);
                            historyIntent.putExtra("history", gson.toJson(history));
                            historyIntent.putExtra("jsonData", userData.toString());
                            historyIntent.putExtra("language", language);
                            finishIfRequired();
                            startActivity(historyIntent);
                            break;
                        case UPDATE_USER_DETAILS:
                            Intent updateIntent = new Intent(getApplicationContext(), UpdateDetailsActivity.class);
                            updateIntent.putExtra("jsonData", userData.toString());
                            updateIntent.putExtra("language", language);
                            finishIfRequired();
                            startActivity(updateIntent);
                            break;
                        case SEND_MONEY:
                            Intent sendMoneyIntent = new Intent(getApplicationContext(), SendMoneyActivity.class);
                            sendMoneyIntent.putExtra("jsonData", userData.toString());
                            sendMoneyIntent.putExtra("language", language);
                            finishIfRequired();
                            startActivity(sendMoneyIntent);
                            break;
                        case REQUEST_MONEY:
                            Intent requestMoneyIntent = new Intent(getApplicationContext(), RequestMoneyActivity.class);
                            requestMoneyIntent.putExtra("jsonData", userData.toString());
                            requestMoneyIntent.putExtra("language", language);
                            finishIfRequired();
                            startActivity(requestMoneyIntent);
                            break;
                        case READ_INFO:
                            // todo
                            ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
                            List<String> textInScreen = new ArrayList<>();
                            extractTextFromView(rootView, textInScreen);
                            StringBuilder stringBuilder = new StringBuilder();
                            textInScreen.forEach(new Consumer<String>() {
                                @Override
                                public void accept(String s) {
                                    stringBuilder.append(s.trim());
                                    stringBuilder.append("\n");
                                }
                            });
                            String textToRead = stringBuilder.toString();
                            Log.d("textToRead", textToRead);
                            textToSpeech(textToRead);
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "Unrecognized command: " + trans, Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (isOnline()) {
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
        Log.d("debug", "listeners added");
    }

    @Nullable
    private Command getCommand(String transcription) {
        Future<Command> commandResult = executorService.submit(new SpeechToCommandTask(transcription, language));
        Command command = null;
        try {
            while (!commandResult.isDone() && !commandResult.isCancelled()) {
                Thread.sleep(100);
            }
            if (commandResult.isDone()) {
                command = commandResult.get();
            }
        } catch (InterruptedException e) {
            // ignore error
            Log.d("error", e.toString());
        } catch (ExecutionException e) {
            // ignore error
            Log.d("error", e.toString());
        }
        return command;
    }

    private boolean verifySpeaker() {
        String spkId = "";
        try {
            spkId = userData.getString("nick_name");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Future<Boolean> result = executorService.submit(new SpeakerVerificationTask(filePath, spkId, false));
        boolean verified = false;
        try {
            while (!result.isDone() && !result.isCancelled()) {
                Thread.sleep(100);
            }
            if (result.isDone()) {
                verified = result.get();
            }
        } catch (InterruptedException e) {
            // ignore error
            Log.d("error", e.toString());
        } catch (ExecutionException e) {
            // ignore error
            Log.d("error", e.toString());
        }
        return verified;
    }

    private String getTranscription() {
        Future<String> result = executorService.submit(new ASRTask(filePath, language));
        String trans = "";
        try {
            while (!result.isDone() && !result.isCancelled()) {
                Thread.sleep(100);
            }
            if (result.isDone()) {
                trans = result.get();
            }
        } catch (InterruptedException e) {
            // ignore error
            Log.d("error", e.toString());
        } catch (ExecutionException e) {
            // ignore error
            Log.d("error", e.toString());
        }
        return trans;
    }

    private void textToSpeech(String text) {
        String normalizedText = normalizeText(text);
        JSONObject jsonPayload = new JSONObject();
        try {
            jsonPayload.put("input", normalizedText);
            jsonPayload.put("lang", language);
            jsonPayload.put("gender", "female");
            jsonPayload.put("alpha", 1);
            jsonPayload.put("segmentwise", "False");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Future<String> result = executorService.submit(new TTSTask(jsonPayload));
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
                JSONObject parsedOutput = new JSONObject(response);
                String base64EncodedAudioString = parsedOutput.getString("audio");
                byte[] decodedAudioBytes = Base64.decode(base64EncodedAudioString, Base64.DEFAULT);
                File tempAudioFile;
                tempAudioFile = File.createTempFile("tts", ".3gp", getCacheDir());
                tempAudioFile.deleteOnExit();
                FileOutputStream fos = new FileOutputStream(tempAudioFile);
                fos.write(decodedAudioBytes);
                fos.close();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mediaPlayer.release();
                    }
                });
                mediaPlayer.setDataSource(tempAudioFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error: "+response, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                // ignore error
            }
        }
    }

    private String normalizeText(String text) {
        JSONObject jsonPayload = new JSONObject();
        String langCode = ("english".equalsIgnoreCase(language)) ? "en" : "ta";
        try {
            jsonPayload.put("text", text);
            jsonPayload.put("lang", langCode);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Future<String> result = executorService.submit(new TextNormalizationTask(jsonPayload));
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
                JSONObject parsedOutput = new JSONObject(response);
                return parsedOutput.getString("output");
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error: "+response, Toast.LENGTH_LONG).show();
            }
        }
        return text;
    }

    private void finishIfRequired() {
        if(this.getClass().getName() != HomeActivity.class.getName()) {
            finish();
        }
    }

    public ArrayList<TransactionHistory> getTransactionHistory() {
        JSONObject jsonPayload = new JSONObject();
        try {
            String providedNickname = userData.getString("nick_name");
            jsonPayload.put("action", "history");
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
            try {
                JSONArray history = new JSONArray(response);
                ArrayList<TransactionHistory> list = new ArrayList<>();
                for (int i = 0; i < history.length() && i < 10; i++) {
                    JSONObject historyEntry = history.getJSONObject(i);
                    Date dateTime = dateParser.parse(historyEntry.getString("date") + " " + historyEntry.getString("time").substring(0,8));
                    list.add(new TransactionHistory(dateTime, historyEntry.getString("to-from"), historyEntry.getDouble("amount"), historyEntry.getDouble("balance")));
                }
                return list;
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error: "+response, Toast.LENGTH_LONG).show();
            } catch (ParseException e) {
                // ignore error
            }
        }
        return null;
    }

    public ArrayList<TransactionHistory> getTransactionHistoryWithConditions(String dateFilter, String userFilter) {
        JSONObject jsonPayload = new JSONObject();
        try {
            String providedNickname = userData.getString("nick_name");
            jsonPayload.put("action", "history");
            jsonPayload.put("nick_name", providedNickname);
            jsonPayload.put("date_filter", dateFilter);
            jsonPayload.put("user_filter", userFilter);
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
                JSONArray history = new JSONArray(response);
                ArrayList<TransactionHistory> list = new ArrayList<>();
                for (int i = 0; i < history.length(); i++) {
                    JSONObject historyEntry = history.getJSONObject(i);
                    Date dateTime = dateParser.parse(historyEntry.getString("date") + " " + historyEntry.getString("time").substring(0,8));
                    list.add(new TransactionHistory(dateTime, historyEntry.getString("to_from"), historyEntry.getDouble("amount"), historyEntry.getDouble("balance")));
                }
                System.out.println("list ready");
                return list;
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error: "+response, Toast.LENGTH_LONG).show();
            } catch (ParseException e) {
                // ignore error
            }
        }
        return null;
    }

    protected double getBalance() {
        JSONObject jsonPayload = new JSONObject();
        try {
            String providedNickname = userData.getString("nick_name");
            jsonPayload.put("action", "balance");
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
            try {
                JSONObject jsonResponse = new JSONObject(response);
                return jsonResponse.getDouble("balance");
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error: "+response, Toast.LENGTH_LONG).show();
            }
        }
        return 0;
    }

    protected boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    protected void createNetworkWarningMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    protected void createVoiceVerificationFailureMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your voice could not be verified with your enrolled voice. If you think it's a mistake, please try again from a less noisy environment");
        builder.setTitle("Voice Verification Error !");
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
            Log.d("debug", "media recorder set for prepare");
        } catch (Exception e) {
            // ignore exception
            Log.d("debug", e.toString());
        }
    }

    private boolean isViewPartOfToolbar(View view) {
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (parent instanceof Toolbar) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private void extractTextFromView(ViewGroup viewGroup, List<String> extractedTexts) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            // Skip the view if it's part of the toolbar
            if (isViewPartOfToolbar(child)) {
                continue;
            }
            if (child instanceof ViewGroup) {
                extractTextFromView((ViewGroup) child, extractedTexts);
            } else if (child instanceof TextView) {
                // Check if the TextView is a direct child of a Spinner
                boolean isTextViewChildOfSpinner = child.getParent() instanceof Spinner;

                if (!isTextViewChildOfSpinner) {
                    String text = ((TextView) child).getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        extractedTexts.add(text);
                    }
                }
            }
        }
    }

}