package com.kainosdub.rpbank;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Future;

public class SendMoneyActivity extends BaseActivity {

    private EditText receiverNicknameEditText, sendAmountValueEditText;
    private Button sendMoneyButton;
    private TextView sendMoneyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);

        initialize();
        addListeners();

        receiverNicknameEditText = findViewById(R.id.receiverNickname);
        sendAmountValueEditText = findViewById(R.id.sendAmountValue);
        sendMoneyButton = findViewById(R.id.sendMoneyButton);
        sendMoneyMessage = findViewById(R.id.sendMoneyMessage);

        sendMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateFields()) {
                    String receiverNickname = receiverNicknameEditText.getText().toString().trim();
                    double amount = Double.valueOf(sendAmountValueEditText.getText().toString().trim());
                    JSONObject jsonPayload = new JSONObject();
                    try {
                        String senderNickname = userData.getString("nick_name");
                        jsonPayload.put("action", "transfer");
                        jsonPayload.put("from_user", senderNickname);
                        jsonPayload.put("to_user", receiverNickname);
                        jsonPayload.put("amount", amount);
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
                                sendMoneyMessage.setText(String.format("Successfully transferred %.2f to %s", amount, receiverNickname));
                            } else if(status != null && status.compareTo("failed") == 0){
                                String reason = jsonResponse.has("reason")? ": " + jsonResponse.getString("reason") : "";
                                sendMoneyMessage.setText("Update failed" + reason);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: " + response, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private boolean validateFields() {
        if(receiverNicknameEditText.length() == 0) {
            receiverNicknameEditText.setError("This field is required");
            return false;
        }
        if(sendAmountValueEditText.length() == 0) {
            sendAmountValueEditText.setError("This field is required");
            return false;
        }
        return true;
    }
}