package com.p3.login;

import com.p3.networking.ServerApi;
import java.net.http.HttpResponse;

public class LoginDAO {
    private final ServerApi api = new ServerApi();

    public String getUserRole(String username) {
        String url = "user/role/" + username;
        HttpResponse<String> response = api.get(url, null, true);

        if (response.statusCode() == 200) {
            String jsonResponse = response.body();
            org.json.JSONObject json = new org.json.JSONObject(jsonResponse);
            return json.getString("role");
        } else {
            throw new RuntimeException("Failed to get user role: " + response.statusCode());
        }
    }

    public String getManagerPassword(String username) { // TODO Prob not correct way to secure password
        String url = "user/pass/" + username;
        HttpResponse response = api.get(url, null, true);

        return (String) response.body();        // TODO har ikke lige testet om den decrypter ordentligt på service men burde virke
    }

    public String getUserId(String username) {
        String url = "user/id/" + username;
        HttpResponse response = api.get(url, null, true);

        return (String) response.body();
    }

    public String getUserFullName(String username) {
        String url = "user/fullName/" + username;
        HttpResponse response = api.get(url, null, true);

        return (String) response.body();
    }

    public String getClockedInStatus(String username) {
        String url = "user/clockInStatus/" + username;
        HttpResponse response = api.get(url, null, true);

        return (String) response.body();
    }

    public void setClockedInStatus(String username, boolean status) {   // TODO forstår ikke hvorfor vi bruger username her, men id i menuDAO
        String url = "user/clockInStatus/" + username + "?status=" + status;

        try{
            api.put(url, null, null);   // TODO Kan ikke lige få headers til at virke som jeg troede
        }
        catch(Exception e){
            e.printStackTrace();    // Jeg er træt og forstår ikke hvorfor den kræver exception her - tror det fordi det put but idk fucksss
        }
    }

    public void postCheckInEvent(int userId) {
        String url = "timelog/checkIn";

        String jsonBody = "{ \"user_id\": " + userId + ", " +   // Event time and date will be handled on server to ensure consitency
                "\"event_type\": \"check_in\" " +
                "}";
        try {
            api.post(url, null, jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getApiKey(String username) {
        String url = "user/apiKey/" + username;
        HttpResponse response = api.get(url, null, false);
        return (String) response.body();
    }
}