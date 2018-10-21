package com.hack.fooapp.payload;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * File Created by harshas on 10/21/18.
 */
public class Connector {

    @SerializedName("id")
    private String id;

    @SerializedName("url")
    private String url;

    @SerializedName("x_base_url")
    private String xBaseUrl;

    @SerializedName("x_authorization")
    private String xAuthorization;

    // ToDo: Instead of object should I use class ?
    @SerializedName("fields")
    private Map<String, Object> fields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getxBaseUrl() {
        return xBaseUrl;
    }

    public void setxBaseUrl(String xBaseUrl) {
        this.xBaseUrl = xBaseUrl;
    }

    public String getxAuthorization() {
        return xAuthorization;
    }

    public void setxAuthorization(String xAuthorization) {
        this.xAuthorization = xAuthorization;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "Connector {" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", xBaseUrl='" + xBaseUrl + '\'' +
                ", xAuthorization='" + xAuthorization + '\'' +
                ", fields=" + fields.toString() +
                '}';
    }
}
