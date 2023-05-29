package com.kainosdub.rpbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class CheckBalanceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_balance);

        initialize();
        addListeners();

        Intent intent = getIntent();
        TextView accountBalanceTextView = findViewById(R.id.accountBalanceTextView);
        double balance = intent.getDoubleExtra("balance", -1);
        if(balance == -1) {
            accountBalanceTextView.setText("Balance could bot be updated");
        } else {
            accountBalanceTextView.setText(String.valueOf(balance));
        }

    }
}