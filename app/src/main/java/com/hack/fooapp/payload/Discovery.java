package com.hack.fooapp.payload;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * File Created by harshas on 10/21/18.
 */
public class Discovery {

    @SerializedName("connectors")
    private List<Connector> connectors;

    public List<Connector> getConnectors() {
        return connectors;
    }

    public void setConnectors(List<Connector> connectors) {
        this.connectors = connectors;
    }
}
