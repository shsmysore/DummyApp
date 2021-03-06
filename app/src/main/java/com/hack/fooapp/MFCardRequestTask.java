package com.hack.fooapp;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hack.fooapp.payload.CardRequestBody;
import com.hack.fooapp.payload.Connector;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Headers;

/**
 * File Created by harshas on 10/22/18.
 */
public class MFCardRequestTask extends AsyncTask<Object, String, String> {

    private String VIDM_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJhZjgxMzg2NS01NzgxLTQzMTAtODI1Zi0zNTk5ZjJjOTQ0MTYiLCJwcm4iOiJsb2NhbF9hZG1pbkBIRVJPQ0FSRCIsImRvbWFpbiI6IlN5c3RlbSBEb21haW4iLCJ1c2VyX2lkIjoiMTA5ODk1NSIsImF1dGhfdGltZSI6MTQ4ODkwOTQyMiwiaXNzIjoiaHR0cHM6Ly9oZXJvY2FyZC52bXdhcmVpZGVudGl0eS5jb20vU0FBUy9hdXRoIiwiYXVkIjoiaHR0cHM6Ly9oZXJvY2FyZC52bXdhcmVpZGVudGl0eS5jb20vU0FBUy9hdXRoL29hdXRodG9rZW4iLCJjdHgiOiJbe1wibXRkXCI6XCJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZFByb3RlY3RlZFRyYW5zcG9ydFwiLFwiaWF0XCI6MTQ4ODkwOTMzMixcImlkXCI6NzU2MDE5fV0iLCJzY3AiOiJvcGVuaWQgcHJvZmlsZSB1c2VyIGVtYWlsIiwiaWRwIjoiMCIsImVtbCI6ImphcmVkY29va0B2bXdhcmUuY29tIiwiY2lkIjoiUGVyZm9ybWFuY2VUZXN0QXV0aFRlbXBsYXRlQDI1M2RhOGU4LTQ5ZjEtNGFlOS04OWM5LTNlNDhjNTE2ZmY2ZCIsImRpZCI6ImxvY2FsX2FkbWluLTVGOUI0RDg5LUNBMTUtNDM4OC1CRTU1LTFDN0Q3QzU2MDlDNiIsIndpZCI6IiIsImV4cCI6MTA5NDk3MDk0MjIsImlhdCI6MTQ4ODkwOTQyMiwic3ViIjoiNWQ1ZGZhODMtZGJjMi00Y2QxLWI1NzktZDUxYjZhMjUwMjFlIiwicHJuX3R5cGUiOiJVU0VSIn0.tqb9WRrDz22xz27JVtNKmdVCcAwybSeRFaibNUErHDekNhKxa3YmDn7yBrEqGIV8o45LE1CLIr5_I0Tmk1gXOZtJy2bYpb9r844MH3M9Mk03QJnCAp8gh7YJI2oq5-PxORV1K6UPkHsWGOyX7rAj8yCHzHYqjGoSTqI2nFMtzvo";
    private String token;
    private List<Connector> connectors;
    private RequestTaskListener requestTaskListener;
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    MFCardRequestTask(String token, List<Connector> connectors) {
        this.token = token;
        this.connectors = connectors;
    }

    public String doPostRequest(String url, Headers headers, String bodyJson) throws IOException {
        System.out.println("Card request url : "+ url);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, bodyJson);
        Request request = new Request.Builder()
                .headers(headers)
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    protected String doInBackground(Object... objects) {
        String response = null;
        Connector connector = getReleventConnector();
        if (connector == null) {
            return response;
        }
        String connUrl = connector.getUrl();
        String baseUrl = connector.getxBaseUrl();
        String xAuth = connector.getxAuthorization();
        System.out.println("Header values : "+ connUrl + "\n" + baseUrl + "\n" + xAuth);
        Headers headers = new Headers.Builder()
                            .add("Authorization", VIDM_TOKEN)
                            .add("X-Connector-Base-Url", baseUrl)
                            .add("X-Connector-Authorization", xAuth)
                            .add("x-routing-prefix", connUrl)
                            .add("Content-Type", "application/json")
                            .build();
        CardRequestBody cardRequestBody = new CardRequestBody();
        Map<String, List<String>> reqMap = new HashMap<>();
        List<String> tokenItems = new ArrayList<>();
        tokenItems.add(this.token);
        reqMap.put("issue_id", tokenItems);
        cardRequestBody.setTokens(reqMap);
        Gson gson = new Gson();
        String reqBodyJson = gson.toJson(cardRequestBody);
        System.out.println(">> Req body Json : "+ reqBodyJson);


        try {
            response = doPostRequest(connUrl + "cards/requests", headers, reqBodyJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public Connector getReleventConnector() {
        Gson gson = new Gson();
        if (token == null) {
            System.out.println("Token is null.");
            return null;
        }
        // token = "APF-1404"
        for (Connector connector: connectors) {
            Map<String, Object> fieldMap = connector.getFields();
            // Try to match regex on the field map.
            for (String fItemKey: fieldMap.keySet()) {
                Object fObject = fieldMap.get(fItemKey);
                Type mapType = new TypeToken<HashMap<String, String>>(){}.getType();
                Map<String, String> fMap = gson.fromJson(gson.toJson(fObject), mapType);
                String regex = fMap.get("regex");
                System.out.println("Regex : "+ regex);
                if (regex != null) {
                    Pattern pathPattern = Pattern.compile(regex);
                    Matcher matcher = pathPattern.matcher(token);
                    if (matcher.matches()) {
                        System.out.println(">> Regex matched item : "+ matcher.group(1));
                        return connector;
                    } else {
                        System.out.println(">> Regex matched item : NO MATCH on token.");
                    }
                }

            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        System.out.println(">> Card response : " +  result);
        if (this.requestTaskListener != null) {
            this.requestTaskListener.onCardRequestSuccess(result);
        }
    }

    void setRequestTaskListener(RequestTaskListener requestTaskListener) {
        this.requestTaskListener = requestTaskListener;
    }

    public interface RequestTaskListener {
        void onCardRequestSuccess(String cardResponse);
    }
}
