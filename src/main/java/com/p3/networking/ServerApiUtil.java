package com.p3.networking;

import java.io.IOException;
import java.net.http.*;


// This class is for error handling, making it generic and non-repeating for controller classes
// TODO should it be async?

public class ServerApiUtil {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static HttpResponse<String> execReq(HttpRequest request){    // Allows for REST from serverAPI class
        try{
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return null;    // For now null but like we talked about, if database allows for null, this is not viable + should also be another way of error handling
        }
    }
}
