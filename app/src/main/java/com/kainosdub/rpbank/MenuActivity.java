package com.kainosdub.rpbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MenuActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initialize();
        addListeners();

        Button checkBalanceButton, lastTenHistoryButton, updateUserDetailsButton, sendMoneyButton, receiveMoneyButton;

        checkBalanceButton = findViewById(R.id.checkBalanceButton);
        lastTenHistoryButton = findViewById(R.id.lastTenButton);
        updateUserDetailsButton = findViewById(R.id.updateUserDetailsButton);
        sendMoneyButton = findViewById(R.id.sendMoneyMenuButton);
        receiveMoneyButton = findViewById(R.id.receiveMoneyMenuButton);

        checkBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double balance = getBalance();
                Intent checkBalanceIntent = new Intent(getApplicationContext(), CheckBalanceActivity.class);
                checkBalanceIntent.putExtra("balance", balance);
                checkBalanceIntent.putExtra("jsonData", userData.toString());
                checkBalanceIntent.putExtra("language", language);
                startActivity(checkBalanceIntent);
            }
        });

        lastTenHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ArrayList<TransactionHistory> history = getTransactionHistory();
//                Intent historyIntent = new Intent(getApplicationContext(), TransactionHistoryActivity.class);
//                historyIntent.putExtra("history", gson.toJson(history));
//                historyIntent.putExtra("jsonData", userData.toString());
//                historyIntent.putExtra("language", language);
//                startActivity(historyIntent);
                Intent historyFormIntent = new Intent(getApplicationContext(), TransactionHistoryFormActivity.class);
                historyFormIntent.putExtra("jsonData", userData.toString());
                historyFormIntent.putExtra("language", language);
                startActivity(historyFormIntent);
            }
        });

        updateUserDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateIntent = new Intent(getApplicationContext(), UpdateDetailsActivity.class);
                updateIntent.putExtra("jsonData", userData.toString());
                updateIntent.putExtra("language", language);
                startActivity(updateIntent);
            }
        });

        sendMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendMoneyIntent = new Intent(getApplicationContext(), SendMoneyActivity.class);
                sendMoneyIntent.putExtra("jsonData", userData.toString());
                sendMoneyIntent.putExtra("language", language);
                startActivity(sendMoneyIntent);
            }
        });

        receiveMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent requestMoneyIntent = new Intent(getApplicationContext(), RequestMoneyActivity.class);
                requestMoneyIntent.putExtra("jsonData", userData.toString());
                requestMoneyIntent.putExtra("language", language);
                startActivity(requestMoneyIntent);
            }
        });
    }
}