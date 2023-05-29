package com.kainosdub.rpbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransactionHistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        initialize();
        addListeners();

        TableLayout historyTable = findViewById(R.id.historyTable);
        Intent intent = getIntent();
        String historyString = intent.getStringExtra("history");
        JSONArray history = null;
        try {
            history = new JSONArray(historyString);
            addHeader(historyTable);
            for (int i = 0; i < history.length(); i++) {
                JSONObject entry = history.getJSONObject(i);
                TableRow row = new TableRow(getApplicationContext());
                TextView textView = new TextView(getApplicationContext());
                textView.setText(entry.getString("dateTime"));
                row.addView(textView);

                textView = new TextView(getApplicationContext());
                textView.setText(entry.getString("toFrom"));
                row.addView(textView);

                textView = new TextView(getApplicationContext());
                textView.setText(entry.getString("amount"));
                row.addView(textView);

                textView = new TextView(getApplicationContext());
                textView.setText(entry.getString("balance"));
                row.addView(textView);

                historyTable.addView(row);
            }

        } catch (JSONException e) {
            // ignore error
        }
    }

    private void addHeader(TableLayout historyTable) {
        TableRow row = new TableRow(getApplicationContext());
        TextView textView = new TextView(getApplicationContext());
        textView.setText("Date");
        row.addView(textView);

        textView = new TextView(getApplicationContext());
        textView.setText("To/From");
        row.addView(textView);

        textView = new TextView(getApplicationContext());
        textView.setText("Amount");
        row.addView(textView);

        textView = new TextView(getApplicationContext());
        textView.setText("Balance");
        row.addView(textView);

        historyTable.addView(row);
    }
}