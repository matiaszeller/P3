package com.p3.login;

import com.p3.networking.ServerApi;
import java.net.http.HttpResponse;

public class LoginDAO {
    private final ServerApi api = new ServerApi();

    public String getUserRole(String username) {
        String url = "user/role/" + username;
        HttpResponse response = api.get(url, null);

        org.json.JSONObject json = new org.json.JSONObject((String) response.body());
        return json.getString("role"); // Works for this method, but all repsonses from server should return json
    }

    public String getManagerPassword(String username) { // TODO Prob not correct way to secure password
        String url = "user/pass/" + username;
        HttpResponse response = api.get(url, null);

        return (String) response.body();        // TODO har ikke lige testet om den decrypter ordentligt på service men burde virke
    }

    public String getUserId(String username) {
        String url = "user/id/" + username;
        HttpResponse response = api.get(url, null);

        return (String) response.body();
    }

    public String getUserFullName(String username) {
        String url = "user/name/" + username;
        HttpResponse response = api.get(url, null);

        return (String) response.body();
    }

    public String getClockedInStatus(String username) {
        String url = "user/clockInStatus/" + username;
        HttpResponse response = api.get(url, null);

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
        HttpResponse response = api.get(url, null);
        return (String) response.body();
    }
}