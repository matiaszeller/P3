package com.p3.login;

import com.p3.networking.ServerApi;
import java.net.http.HttpResponse;

public class LoginDAO {
    private final ServerApi api = new ServerApi();

    /*
    *           Get Requests
    */

    public String getUserRole(String username) {
        String url = "user/" + username + "/role";
        HttpResponse<String> response = api.get(url, null, true);

        if (response.statusCode() == 200) {
            String jsonResponse = response.body();
            org.json.JSONObject json = new org.json.JSONObject(jsonResponse);
            return json.getString("role");
        } else {
            throw new RuntimeException("Failed to get user role: " + response.statusCode());
        }
    }

    public String getManagerPassword(String username) {
        String url = "user/" + username + "/password";
        HttpResponse response = api.get(url, null, true);

        return (String) response.body();
    }

    public String getUserId(String username) {
        String url = "user/" + username + "/id";
        HttpResponse response = api.get(url, null, true);

        return (String) response.body();
    }

    public String getUserFullName(String username) {
        String url = "user/" + username + "/fullName";
        HttpResponse response = api.get(url, null, true);

        return (String) response.body();
    }

    public String getClockedInStatus(String username) {
        String url = "user/" + username + "/clockInStatus";
        HttpResponse response = api.get(url, null, true);

        return (String) response.body();
    }

    public String getApiKey(String username) {
        String url = "user/apiKey/" + username;
        HttpResponse response = api.get(url, null, false);
        return (String) response.body();
    }

    /*
     *           Post Requests
     */

    public void postCheckInEvent(int userId) {
        String url = "timelog/checkIn";

        String jsonBody = "{ \"user_id\": " + userId + ", " +   // Event time and date will be handled on server
                "\"event_type\": \"check_in\" " +
                "}";
        try {
            api.post(url, null, jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setClockedInStatus(String username, boolean status) {
        String url = "user/clockInStatus/" + username + "?status=" + status;

        try{
            api.put(url, null, null);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}