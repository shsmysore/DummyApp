package com.hack.fooapp.payload;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * File Created by harshas on 10/22/18.
 */
public class CardRequestBody {

    @SerializedName("tokens")
    private Map<String, List<String>> tokens;

    public Map<String, List<String>> getTokens() {
        return tokens;
    }

    public void setTokens(Map<String, List<String>> tokens) {
        this.tokens = tokens;
    }
}
