package com.dewansmsgateway;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button uploadFileButton;
    Button sendSmsButton;
    TableLayout dataTableLayout;
    private static final int READ_REQUEST_CODE = 42;
    List<String> numbers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uploadFileButton = (Button) findViewById(R.id.uploadButton);
        sendSmsButton = (Button) findViewById(R.id.sendSmsButton);
        dataTableLayout = (TableLayout) findViewById(R.id.dataTableLayout);
        uploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });
        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms();
            }
        });
    }

    public void performFileSearch() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                String extension = uri.getPath().substring(uri.getPath().lastIndexOf("."));
                if (!extension.equals(".csv")) {
                    Toast.makeText(getApplicationContext(), "File format is not supported. Please upload a csv file.", Toast.LENGTH_LONG).show();
                    return;
                }
                numbers = readCsv(uri);
                if (numbers != null) {
                    for (int i = 0; i < numbers.size(); i++) {

                        TableRow row = new TableRow(this);

                        TextView t1 = new TextView(this);
                        t1.setText(Integer.toString(i + 1));
                        row.addView(t1);

                        TextView t2 = new TextView(this);
                        t2.setText(numbers.get(i));
                        row.addView(t2);

                        dataTableLayout.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    }
                }
            }
        }
    }

    public List<String> readCsv(Uri uri) {
        String line = "";
        String cvsSplitBy = ",";
        BufferedReader br = null;
        List<String> numbers = new ArrayList<>();
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                String[] commaSeparatedStrings = line.split(cvsSplitBy);
                numbers.add(commaSeparatedStrings[0]);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return numbers;
    }

    public void sendSms() {
        if (numbers == null || numbers.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Number list is empty. Please upload a file.", Toast.LENGTH_LONG).show();
            return;
        }

        String address = "";
        for (String number : numbers) {
            address = address + number + ";";
        }

        try {
            Intent i = new Intent(android.content.Intent.ACTION_VIEW);
            i.putExtra("address", address);
            i.setType("vnd.android-dir/mms-sms");
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
