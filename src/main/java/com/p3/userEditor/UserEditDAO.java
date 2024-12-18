package com.p3.userEditor;

import com.p3.networking.ServerApi;
import java.net.http.HttpResponse;


public class UserEditDAO {

    private final ServerApi api = new ServerApi();

    public String userNames() {
        HttpResponse<String> response = api.get("user/info/users", null,true);

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            System.err.println("Error fetching usernames: " + response.statusCode() + " - " + response.body());
            return null;
        }
    }

    public boolean updateUser(User user) {
        ServerApi api = new ServerApi();
        String userJson = user.toJson();

        try {
            HttpResponse<String> response = api.put("user/update", null, userJson);
            if (response.statusCode() == 200) {
                System.out.println("User updated successfully: " + response.body());
                return true;
            } else {
                System.err.println("Error updating user: " + response.statusCode() + " - " + response.body());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean createUser(User user) {
        ServerApi api = new ServerApi();

        String userJson = user.toJson();

        try {
            HttpResponse<String> response = api.post("user/newUser", null, userJson);
            if (response.statusCode() == 200) {
                System.out.println("User updated successfully: " + response.body());
                return true;
            } else {
                System.err.println("Error up dating user: " + response.statusCode() + " - " + response.body());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}