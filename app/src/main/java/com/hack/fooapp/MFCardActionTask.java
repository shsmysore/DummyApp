package com.hack.fooapp;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.hack.fooapp.payload.Connector;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * File Created by harshas on 10/22/18.
 */
public class MFCardActionTask extends AsyncTask<Object, String, Integer> {

    private String VIDM_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJhZjgxMzg2NS01NzgxLTQzMTAtODI1Zi0zNTk5ZjJjOTQ0MTYiLCJwcm4iOiJsb2NhbF9hZG1pbkBIRVJPQ0FSRCIsImRvbWFpbiI6IlN5c3RlbSBEb21haW4iLCJ1c2VyX2lkIjoiMTA5ODk1NSIsImF1dGhfdGltZSI6MTQ4ODkwOTQyMiwiaXNzIjoiaHR0cHM6Ly9oZXJvY2FyZC52bXdhcmVpZGVudGl0eS5jb20vU0FBUy9hdXRoIiwiYXVkIjoiaHR0cHM6Ly9oZXJvY2FyZC52bXdhcmVpZGVudGl0eS5jb20vU0FBUy9hdXRoL29hdXRodG9rZW4iLCJjdHgiOiJbe1wibXRkXCI6XCJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZFByb3RlY3RlZFRyYW5zcG9ydFwiLFwiaWF0XCI6MTQ4ODkwOTMzMixcImlkXCI6NzU2MDE5fV0iLCJzY3AiOiJvcGVuaWQgcHJvZmlsZSB1c2VyIGVtYWlsIiwiaWRwIjoiMCIsImVtbCI6ImphcmVkY29va0B2bXdhcmUuY29tIiwiY2lkIjoiUGVyZm9ybWFuY2VUZXN0QXV0aFRlbXBsYXRlQDI1M2RhOGU4LTQ5ZjEtNGFlOS04OWM5LTNlNDhjNTE2ZmY2ZCIsImRpZCI6ImxvY2FsX2FkbWluLTVGOUI0RDg5LUNBMTUtNDM4OC1CRTU1LTFDN0Q3QzU2MDlDNiIsIndpZCI6IiIsImV4cCI6MTA5NDk3MDk0MjIsImlhdCI6MTQ4ODkwOTQyMiwic3ViIjoiNWQ1ZGZhODMtZGJjMi00Y2QxLWI1NzktZDUxYjZhMjUwMjFlIiwicHJuX3R5cGUiOiJVU0VSIn0.tqb9WRrDz22xz27JVtNKmdVCcAwybSeRFaibNUErHDekNhKxa3YmDn7yBrEqGIV8o45LE1CLIr5_I0Tmk1gXOZtJy2bYpb9r844MH3M9Mk03QJnCAp8gh7YJI2oq5-PxORV1K6UPkHsWGOyX7rAj8yCHzHYqjGoSTqI2nFMtzvo";
    private String actionUrl;
    private Map<String, String> formItems;
    private List<Connector> connectors;
    private RequestTaskListener requestTaskListener;
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    MFCardActionTask(String actionUrl, Map<String, String> formItems, List<Connector> connectors) {
        this.actionUrl = actionUrl;
        this.formItems = formItems;
        this.connectors = connectors;
    }

    public int doPostRequest(String url, Headers headers, Map<String, String> formItems) throws IOException {
        System.out.println("Card action url : "+ url);
        OkHttpClient client = new OkHttpClient();

        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (String key: formItems.keySet()) {
            formBodyBuilder.add(key, formItems.get(key));
        }
        RequestBody requestBody = formBodyBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        return response.code();
    }

    @Override
    protected Integer doInBackground(Object... objects) {
        String connUrl = connectors.get(0).getUrl();
        String baseUrl = connectors.get(0).getxBaseUrl();
        String xAuth = connectors.get(0).getxAuthorization();
        System.out.println("Header values : "+ connUrl + "\n" + baseUrl + "\n" + xAuth);
        Headers headers = new Headers.Builder()
                .add("Authorization", VIDM_TOKEN)
                .add("X-Connector-Base-Url", baseUrl)
                .add("X-Connector-Authorization", xAuth)
                .add("x-routing-prefix", connUrl)
                .add("Content-Type", "application/x-www-form-urlencoded")
                .build();

        Gson gson = new Gson();

        Integer responseCode = 500;
        try {
            responseCode = doPostRequest(actionUrl, headers, formItems);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseCode;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        System.out.println(">> Action response : " +  result);
        if (this.requestTaskListener != null) {
            this.requestTaskListener.onActionSuccess(result);
        }
    }

    void setRequestTaskListener(RequestTaskListener requestTaskListener) {
        this.requestTaskListener = requestTaskListener;
    }

    public interface RequestTaskListener {
        void onActionSuccess(Integer responseCode);
    }
}
