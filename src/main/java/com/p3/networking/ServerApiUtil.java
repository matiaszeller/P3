package com.p3.networking;

import java.io.IOException;
import java.net.http.*;


// This class is for error handling, making it generic and non-repeating for controller classes

public class ServerApiUtil {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static HttpResponse<String> execReq(HttpRequest request){    // Allows for REST from serverAPI class
        try{
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}
