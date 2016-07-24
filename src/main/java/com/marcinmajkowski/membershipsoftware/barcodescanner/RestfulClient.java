package com.marcinmajkowski.membershipsoftware.barcodescanner;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.json.JsonObject;
import java.io.IOException;

public class RestfulClient {

    private final String apiUrl;

    private final CloseableHttpClient httpClient;

    public RestfulClient(String apiUrl, CloseableHttpClient httpClient) {
        this.apiUrl = apiUrl;
        this.httpClient = httpClient;
    }

    public int post(JsonObject payload) throws IOException {
        StringEntity entity = new StringEntity(payload.toString());
        entity.setContentType("application/json");

        HttpPost postRequest = new HttpPost(apiUrl + "/api/v1/codeInputs");
        postRequest.setEntity(entity);

        System.out.print("Sending POST request with payload: ");
        System.out.print(payload.toString());
        System.out.println(".");
        CloseableHttpResponse response = httpClient.execute(postRequest);

        int statusCode = response.getStatusLine().getStatusCode();

        response.close();

        return statusCode;
    }
}
