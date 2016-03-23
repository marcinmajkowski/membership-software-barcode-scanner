package com.marcinmajkowski.membershipsoftware.barcodescanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.json.JsonObject;
import java.io.IOException;

public class RestfulClient {

    private final String apiUrl;

    public RestfulClient(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public int post(JsonObject payload) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();

        StringEntity entity = new StringEntity(payload.toString());
        entity.setContentType("application/json");

        HttpPost postRequest = new HttpPost(apiUrl + "/api/v1/codeInputs");
        postRequest.setEntity(entity);

        System.out.println("Sending POST request with payload:");
        System.out.println(payload.toString());
        HttpResponse response = httpClient.execute(postRequest);

        return response.getStatusLine().getStatusCode();
    }
}
