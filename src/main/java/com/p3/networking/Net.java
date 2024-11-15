package com.p3.networking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Net {
    private static final String SERVER_IP = "localhost:";
    private static final String PORT = "8080";
    public static String sendPostRequest(String endpoint, String jsonData) {
        String response = null;
        try {

            String urlString = "http://" + SERVER_IP + PORT + endpoint + jsonData;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder responseBuilder = new StringBuilder();
                    String responseLine;

                    while ((responseLine = br.readLine()) != null) {
                        responseBuilder.append(responseLine.trim());
                    }
                    response = responseBuilder.toString();
                }
            } else {
                System.err.println("Server returned non-OK status: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}