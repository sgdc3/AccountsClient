package com.mojang.api.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

/*
    TODO: refactor so unit tests can be written :)
 */
public class BasicHttpClient implements HttpClient {

    private static final int CONNECT_TIMEOUT_MILLIS = 3000;
    private static final int READ_TIMEOUT_MILLIS = 4000;

    private static BasicHttpClient instance;

    private BasicHttpClient() {
    }

    public static BasicHttpClient getInstance() {
        if (instance == null) {
            instance = new BasicHttpClient();
        }
        return instance;
    }

    @Override
    public String post(URL url, HttpBody body, List<HttpHeader> headers) throws IOException {
        return post(url, null, body, headers);
    }

    @Override
    public String post(URL url, Proxy proxy, HttpBody body, List<HttpHeader> headers) throws IOException {
        if (proxy == null) proxy = Proxy.NO_PROXY;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        connection.setRequestMethod("POST");

        return getResponse(connection, body, headers);
    }

    @Override
    public String get(URL url, List<HttpHeader> headers) throws IOException {
        return get(url, null, headers);
    }

    @Override
    public String get(URL url, Proxy proxy, List<HttpHeader> headers) throws IOException {
        if (proxy == null) proxy = Proxy.NO_PROXY;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);

        return getResponse(connection, null, headers);
    }

    private static String getResponse(HttpURLConnection connection, HttpBody body, List<HttpHeader> headers) throws IOException {
        for (HttpHeader header : headers) {
            connection.setRequestProperty(header.getName(), header.getValue());
        }

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);

        if (body != null) {
            connection.setDoOutput(true);
            try (DataOutputStream writer = new DataOutputStream(connection.getOutputStream())) {
                writer.write(body.getBytes());
            }
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
        }
        return response.toString();
    }
    
}
