package com.kainosdub.rpbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class TransactionHistoryFormActivity extends BaseActivity {

    EditText fromDate, toDate, userFilter;
    Button getHistoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history_form);

        initialize();
        addListeners();

        fromDate = findViewById(R.id.fromDate);
        toDate = findViewById(R.id.toDate);
        userFilter = findViewById(R.id.userFilter);
        getHistoryButton = findViewById(R.id.getHistoryButton);

        getHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String from = "";
                String to = "";
                String user = userFilter.getText().toString();
                String dateFilter = from + ":00-00," + to + ":23-59";
                ArrayList<TransactionHistory> history = getTransactionHistoryWithConditions(dateFilter, user);
                Intent historyIntent = new Intent(getApplicationContext(), TransactionHistoryActivity.class);
                historyIntent.putExtra("history", gson.toJson(history));
                historyIntent.putExtra("jsonData", userData.toString());
                historyIntent.putExtra("language", language);
                startActivity(historyIntent);
            }
        });
    }
}