package com.p3.networking;

import java.net.URI;
import java.net.http.*;
import java.util.Map;

public class ServerApi {
    private final String BASE_URL = "http://85.218.178.119:8080/api/";

    /*
    * Requests take path efter BASE_URL
    * */
    public HttpResponse<String> get(String path, Map<String, String> headers) {     // Can take headers but for now we dont have any paths with them
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET();

        if (headers != null){
            headers.forEach(builder::header);   // For each optional header argument, add to url
        }
        return ServerApiUtil.execReq(builder.build());
    }

    public HttpResponse<String> post(String path, Map<String, String> headers, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json") // Set content type so server know types of
                .POST(HttpRequest.BodyPublishers.ofString(body));

        if (headers != null){
            headers.forEach(builder::header);
        }

        return ServerApiUtil.execReq(builder.build());
    }

    public HttpResponse<String> put(String path, Map<String, String> headers, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json");

        if (headers != null){
            headers.forEach(builder::header);
        }

        if (body != null && !body.isEmpty()){
            builder.PUT(HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.PUT(HttpRequest.BodyPublishers.noBody());
        }

        return ServerApiUtil.execReq(builder.build());
    }

    public HttpResponse<String> delete(String path, Map<String, String> headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE();

        if (headers != null){
            headers.forEach(builder::header);
        }

        return ServerApiUtil.execReq(builder.build());
    }
}