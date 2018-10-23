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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    AppCompatEditText configUrlEt;
    AppCompatButton reloadBtn;

    AppCompatEditText cardInputEt;
    AppCompatButton getCardsBtn;

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

        cardInputEt = findViewById(R.id.bd_card_input_et);
        getCardsBtn = findViewById(R.id.bd_get_cards_btn);

        configUrlEt.setText(getConfigUrl());

        // Load connector configurations.
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

        // Card request.
        getCardsBtn.setOnClickListener(v -> {
            String cardToken =  cardInputEt.getText().toString();
            // ToDo: make a card request and get json on the call back interface.
            MFCardRequestTask cardRequestTask = new MFCardRequestTask(cardToken, getConnectors());
            cardRequestTask.setRequestTaskListener(new MFCardRequestTask.RequestTaskListener() {
                @Override
                public void onCardRequestSuccess(String cardResponse) {
                    // Send this card-response to web view.
                }
            });
            cardRequestTask.execute();
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Card action.
        String actionUrl = "http://6681bc1a.ngrok.io/api/v1/issues/235720/comment";
        Map<String, String> params = new HashMap<>();
        params.put("body", "Comment from Android app");
        MFCardActionTask actionTask = new MFCardActionTask(actionUrl, params, getConnectors());
        actionTask.setRequestTaskListener(new MFCardActionTask.RequestTaskListener() {
            @Override
            public void onActionSuccess(Integer responseCode) {
                System.out.println(">> Callback action response : " + responseCode);
                // Any 2xx response is good to show Toast.
                // For 4xx or 5xx show Toast as "Action failure".
            }
        });
        //actionTask.execute();

    }

    private List<Connector> getConnectors() {
        String respnseString = sPref.getString("connectors", null);
        if (respnseString == null) {
            return null;
        }
        Type listType = new TypeToken<ArrayList<Connector>>(){}.getType();
        return gson.fromJson(
                respnseString, listType);
    }

    void printSPref() {
        List<Connector> connectors = getConnectors();

        if (connectors == null) {
            return;
        }
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
