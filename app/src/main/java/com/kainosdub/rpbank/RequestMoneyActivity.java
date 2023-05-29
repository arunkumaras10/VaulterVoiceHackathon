package com.kainosdub.rpbank;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class RequestMoneyActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_money);

        initialize();
        addListeners();
    }
}