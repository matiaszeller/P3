package com.p3.networking;

import com.p3.session.Session;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class ServerApi {
    private final String BASE_URL = "http://localhost:8080/api/";

    public HttpResponse<String> get(String path, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET();

        if (headers != null) {
            headers.forEach((key, value) -> {
                if (key != null && value != null) {
                    builder.header(key, value);
                }
            });
        }

        return ServerApiUtil.execReq(builder.build());
    }

    public HttpResponse<String> post(String path, Map<String, String> headers, String body) throws Exception {
        if (headers == null) headers = new HashMap<>();
        headers.put("API-Key", Session.getApiKey());
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        headers.forEach(builder::header);
        return ServerApiUtil.execReq(builder.build());
    }

    public HttpResponse<String> put(String path, Map<String, String> headers, String body) throws Exception {
        if (headers == null) headers = new HashMap<>();
        headers.put("API-Key", Session.getApiKey());
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json");

        if (body != null && !body.isEmpty()) {
            builder.PUT(HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.PUT(HttpRequest.BodyPublishers.noBody());
        }

        headers.forEach(builder::header);
        return ServerApiUtil.execReq(builder.build());
    }

    public HttpResponse<String> delete(String path, Map<String, String> headers) throws Exception {
        if (headers == null) headers = new HashMap<>();
        headers.put("API-Key", Session.getApiKey());
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE();

        headers.forEach(builder::header);
        return ServerApiUtil.execReq(builder.build());
    }
}