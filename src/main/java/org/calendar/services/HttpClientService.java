package org.calendar.services;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.InputStream;

public class HttpClientService {
    private final CloseableHttpClient client;

    public HttpClientService() {
        this.client = HttpClientBuilder.create().build();
    }

    public InputStream get(String url) throws Exception {
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = this.client.execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            response.close();
            throw new RuntimeException("HTTP error: " + response.getStatusLine().getStatusCode());
        }
        return response.getEntity().getContent();
    }

    public void close() throws Exception {
        client.close();
    }
}
