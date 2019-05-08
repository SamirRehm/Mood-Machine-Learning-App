package com.example.srehm.moodappui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.Vector;

public class feature_selection_activity extends AppCompatActivity {

    private static final String[] Activities = new String[]{
            "Run", "Hang out with friends", "Tennis", "Study", "Music"
    };
    Vector<String> selected_features = new Vector<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_selection_activity);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, Activities);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.feature_list);
        textView.setAdapter(adapter);
    }

    public void add_feature(View v) {
        //AutoCompleteTextView a = (AutoCompleteTextView)v;
        //selected_features.add(a.getText().toString());
    }

}
