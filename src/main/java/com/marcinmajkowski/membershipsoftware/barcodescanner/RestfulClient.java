package com.marcinmajkowski.membershipsoftware.barcodescanner;

import javax.json.JsonObject;

public class RestfulClient {

    private final String apiUrl;

    public RestfulClient(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public int post(JsonObject payload) {
        System.out.println("Sending:");
        System.out.println(payload);
        return 500;
    }
}
