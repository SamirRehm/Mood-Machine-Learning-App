package com.example.srehm.moodappui;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.custom.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileOutputStream;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    FirebaseStorage storage;
    FirebaseModelInterpreter firebaseModelInterpreter;
    FirebaseModelInputOutputOptions IOOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage = FirebaseStorage.getInstance();
        set_up_firebase_model();
    }

    public void set_up_firebase_model() {
        FirebaseModelDownloadConditions.Builder conditionsBuilder =
                new FirebaseModelDownloadConditions.Builder().requireWifi();
        FirebaseModelDownloadConditions conditions = conditionsBuilder.build();

        FirebaseRemoteModel cloudSource = new FirebaseRemoteModel.Builder("model-predictor")
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build();
        FirebaseModelManager.getInstance().registerRemoteModel(cloudSource);

        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setRemoteModelName("model-predictor")
                .build();
        firebaseModelInterpreter = null;
        try {
            firebaseModelInterpreter = FirebaseModelInterpreter.getInstance(options);
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
        IOOptions = null;
        try {
            IOOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1})
                            .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
    }

    public void predict(float[][] input) {
        input = new float[1][1];
        input[0][0] = (float)12.0;
        try {
            FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                    .add(input)  // add() as many input arrays as your model requires
                    .build();
            firebaseModelInterpreter.run(inputs, IOOptions)
                    .addOnSuccessListener(
                            new OnSuccessListener<FirebaseModelOutputs>() {
                                @Override
                                public void onSuccess(FirebaseModelOutputs result) {
                                    float[][] output = result.getOutput(0);
                                    float[] probabilities = output[0];
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }});
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
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

    // weather api key: cb6a3e93bc1ef4e6f64672a923cd240a
    public void write_data(View v) {
        StorageReference root = storage.getReference();
        StorageReference mountainsRef = root.child("data.txt");
        UploadTask uploadTask = mountainsRef.putBytes("aaa".getBytes());
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    }
}
