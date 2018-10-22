package com.hack.fooapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hack.fooapp.payload.Connector;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    AppCompatButton reloadBtn;
    AppCompatEditText configUrlEt;
    private SharedPreferences sPref;
    private Gson gson;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.activity = this;
        this.gson = new Gson();
        sPref = getSharedPreferences("BackDoor", Context.MODE_PRIVATE);

        reloadBtn = findViewById(R.id.bd_reload_btn);
        configUrlEt = findViewById(R.id.bd_config_url_et);

        configUrlEt.setText(getConfigUrl());
        reloadBtn.setOnClickListener(v -> {
            RequestTask task = new RequestTask();
            task.setRequestTaskListener(new RequestTask.RequestTaskListener() {
                @Override
                public void onRequestTaskComplete(boolean status) {
                    if (status) {
                        Toast.makeText(activity, "Config reloaded successfully.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(activity, "Reloading Failed.", Toast.LENGTH_LONG).show();
                    }

                }
            });

            String configUrl =  configUrlEt.getText().toString();
            saveConfigUrl(configUrl);

            task.execute(configUrl, sPref);

            printSPref();
        });
    }

    void printSPref() {
        String respnseString = sPref.getString("connectors", null);
        if (respnseString == null) {
            return;
        }
        Type listType = new TypeToken<ArrayList<Connector>>(){}.getType();
        List<Connector> connectors = gson.fromJson(
                respnseString, listType);

        System.out.println("Connectors read from shared pref.");
        for (Connector item : connectors) {
            System.out.println(item.toString());
        }

    }

    String getConfigUrl() {
        return sPref.getString("config_url", "");
    }

    void saveConfigUrl(String url) {
        System.out.println("Entered config Url : "+ url);
        SharedPreferences.Editor xPrefEdit = sPref.edit();
        xPrefEdit.putString("config_url", url);
        xPrefEdit.apply();
    }

    static String getFrom(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    static class RequestTask extends AsyncTask<Object, String, String> {

        private SharedPreferences sPref;
        private RequestTaskListener requestTaskListener;

        @Override
        protected String doInBackground(Object... objects) {
            sPref = (SharedPreferences) objects[1];
            String response = null;
            try {
                response = getFrom((String) objects[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println("Discovered connectors : \n"+ result);

            boolean status;
            if (result != null) {
                SharedPreferences.Editor xPrefEdit = sPref.edit();
                xPrefEdit.putString("connectors", result);
                xPrefEdit.apply();
                status = true;
            } else {
                status = false;
            }

            if (requestTaskListener != null) {
                requestTaskListener.onRequestTaskComplete(status);
            }
        }

        void setRequestTaskListener(RequestTaskListener requestTaskListener) {
            this.requestTaskListener = requestTaskListener;
        }

        private interface RequestTaskListener {
            void onRequestTaskComplete(boolean status);
        }
    }
}
