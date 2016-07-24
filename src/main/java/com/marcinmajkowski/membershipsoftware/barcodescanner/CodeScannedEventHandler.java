package com.marcinmajkowski.membershipsoftware.barcodescanner;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;

public class CodeScannedEventHandler {

    private final RestfulClient restfulClient;

    public CodeScannedEventHandler(RestfulClient restfulClient) {
        this.restfulClient = restfulClient;
    }

    void handle(String code) {
        JsonObject payload = Json.createObjectBuilder()
                .add("code", code)
                .build();
        try {
            int statusCode = restfulClient.post(payload);
            System.out.println("Server responded with " + statusCode + ".");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
