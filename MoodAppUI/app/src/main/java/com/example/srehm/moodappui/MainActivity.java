package com.example.srehm.moodappui;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    FirebaseStorage storage;
    FirebaseModelInterpreter firebaseModelInterpreter;
    FirebaseModelInputOutputOptions IOOptions;
    ProgressDialog pd;
    String jsonResponse;
    float prediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage = FirebaseStorage.getInstance();
        set_up_firebase_model();
    }

    public void set_up_firebase_model() {
        FirebaseModelDownloadConditions.Builder conditionsBuilder = new FirebaseModelDownloadConditions.Builder().requireWifi();
        FirebaseModelDownloadConditions conditions = conditionsBuilder.build();

        FirebaseRemoteModel cloudSource = new FirebaseRemoteModel.Builder("model-predictor")
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build();
        FirebaseModelManager.getInstance().registerRemoteModel(cloudSource);

        FirebaseModelOptions options = new FirebaseModelOptions.Builder().setRemoteModelName("model-predictor").build();
        firebaseModelInterpreter = null;
        IOOptions = null;
        try {
            firebaseModelInterpreter = FirebaseModelInterpreter.getInstance(options);
            IOOptions = new FirebaseModelInputOutputOptions.Builder()
                    .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1})
                    .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1})
                    .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
    }

    public void predict(float[][] input) {
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
                                    prediction = output[0][0];
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

    private void upload(final float temperature) {
        StorageReference root = storage.getReference();
        final StorageReference fileRef = root.child("data.txt");

        fileRef.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String res = new String(bytes);
                RatingBar ratingBar = findViewById(R.id.feeling);
                String newEntry = ratingBar.getRating() + "," + temperature + "\n";
                res = res + newEntry;
                UploadTask uploadTask = fileRef.putBytes(res.getBytes());
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //kkkk
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final float[][] input = new float[1][1];
                        input[0][0] = temperature;
                        predict(input);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                RatingBar ratingBar = findViewById(R.id.feeling);
                String newEntry = ratingBar.getRating() + "," + temperature + "\n";
                fileRef.putBytes(newEntry.getBytes()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final float[][] input = new float[1][1];
                        input[0][0] = temperature;
                        predict(input);
                    }
                });
            }
        });
    }

    public void add_data(View v) throws ExecutionException, InterruptedException, JSONException {

        long startTime = System.currentTimeMillis();
        String weather = new JsonTask().execute("http://api.openweathermap.org/data/2.5/weather?q=Chilliwack&APPID=cb6a3e93bc1ef4e6f64672a923cd240a").get();
        JSONObject reader = new JSONObject(weather);

        float temperature = (float)reader.getJSONObject("main").getDouble("temp")-273.15f;
        long operationTime = System.currentTimeMillis() - startTime;
        upload(temperature);
    }

    public void feature_addition(View v) {
        Intent intent = new Intent(v.getContext(), feature_selection_activity.class);
        startActivity(intent);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            jsonResponse = result;
        }
    }
}
