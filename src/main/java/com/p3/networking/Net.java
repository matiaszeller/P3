package com.p3.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Net {

    private String IP;
    private String endpoint;
    private String urlString = "http://" + IP + endpoint;

    public void sendRequestToServer(String request) throws IOException {

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("PASSWORD", "secret");

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);






        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );


    }
}