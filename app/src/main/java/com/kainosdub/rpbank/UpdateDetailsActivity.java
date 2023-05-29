package com.kainosdub.rpbank;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class UpdateDetailsActivity extends BaseActivity {

    private EditText newValueEditText;
    private TextView existingValueTextView;
    private Spinner updateKeySpinner;
    private TextView updateErrorMessage;
    Button updateDetailsButton;
    private String selectedKey;
    private Map<String, String> keyMap = new HashMap<String, String>() {{
        put("Full Name", "full_name");
        put("Nickname", "nick_name");
        put("Username", "user_name");
        put("Mobile number", "mob_number");
        put("PIN number", "pin_number");
    }};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_details);

        initialize();
        addListeners();
        existingValueTextView = findViewById(R.id.existingValueTextView);
        newValueEditText = findViewById(R.id.newValue);
        updateErrorMessage = findViewById(R.id.updateErrorMessage);
        updateKeySpinner = findViewById(R.id.updateKeySpinner);
        updateDetailsButton = findViewById(R.id.updateButton);

        updateDetailsButton.setClickable(false);

        updateKeySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedKey = updateKeySpinner.getSelectedItem().toString();
                //enable the button
                updateDetailsButton.setClickable(true);
                // update existing value
                try {
                    String key = keyMap.get(selectedKey);
                    String existingValue = userData.getString(key);
                    existingValueTextView.setText(existingValue);
                } catch (JSONException e) {
                    // ignore error
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        updateDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = keyMap.get(selectedKey);
                String newValue = newValueEditText.getText().toString().trim();

                JSONObject jsonPayload = new JSONObject();
                try {
                    String nickname = userData.getString("nick_name");
                    jsonPayload.put("action", "update");
                    jsonPayload.put("nick_name", nickname);
                    jsonPayload.put("key", key);
                    jsonPayload.put("value", newValue);
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
                            // update success
                            updateErrorMessage.setText("Successfully updated the details.");
                            // update value in state
                            userData.put(key, newValue);
                        } else if(status != null && status.compareTo("failed") == 0){
                            String reason = jsonResponse.has("reason")? ": " + jsonResponse.getString("reason") : "";
                            updateErrorMessage.setText("Update failed" + reason);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error: " + response, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                }
            }
        });
        updateDetailsButton.setClickable(false);
    }

}