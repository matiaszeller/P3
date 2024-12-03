package com.p3.userEditor;

import com.p3.networking.ServerApi;
import java.net.http.HttpResponse;


public class UserEditDAO {

    public static String userNames() {
        ServerApi api = new ServerApi();
        HttpResponse<String> response = api.get("user/info/users", null,true);

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            System.err.println("Error fetching usernames: " + response.statusCode() + " - " + response.body());
            return null;
        }
    }

    public static boolean updateUser(User user) {
        ServerApi api = new ServerApi();


        String userJson = user.toJson();
        System.out.println(userJson);

        try {
            HttpResponse<String> response = api.put("user/update", null, userJson);
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
    public static boolean createUser(User user) {
        ServerApi api = new ServerApi();


        String userJson = user.toJson();
        System.out.println(userJson);

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

} // merging main