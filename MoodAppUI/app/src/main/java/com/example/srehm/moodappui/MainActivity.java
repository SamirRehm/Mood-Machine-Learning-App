package com.example.srehm.moodappui;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FileInputStream iStream;
        StringBuilder sb = new StringBuilder();
        try {
            iStream = openFileInput("myfile");
            InputStreamReader inputStreamReader = new InputStreamReader(iStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        }
        catch(Exception e) {

        }
        String data = sb.toString();
        System.out.println(data);
    }

    public void save_data(View v) {
        String filename = "myfile";
        FileOutputStream outputStream;
        RatingBar ratingBar = (RatingBar)findViewById(R.id.feeling);
        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(String.valueOf(ratingBar.getRating()).getBytes());
            outputStream.write(", ".getBytes());
            outputStream.write(mydate.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void feature_addition(View v) {
        Intent intent = new Intent(v.getContext(), feature_selection_activity.class);
        startActivity(intent);
    }
}
