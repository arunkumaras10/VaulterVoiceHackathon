package com.kainosdub.rpbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initialize();
        addListeners();

        Intent intent = getIntent();
        TextView displayText = findViewById(R.id.displayText);
        try {
            displayText.setText("Welcome "+ super.userData.getString("nick_name"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Button menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // goto menu
                Intent menuIntent = new Intent(getApplicationContext(), MenuActivity.class);
                menuIntent.putExtra("jsonData", userData.toString());
                menuIntent.putExtra("language", language);
                startActivity(menuIntent);
            }
        });
//        displayText.setText(jsonString);
    }

}