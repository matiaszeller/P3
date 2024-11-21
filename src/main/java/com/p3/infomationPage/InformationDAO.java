package com.p3.infomationPage;

import com.p3.networking.ServerApi;
import java.net.http.HttpResponse;
import com.p3.infomationPage.User; // Ensure correct reference

public class InformationDAO {

    public static String userNames() {
        ServerApi api = new ServerApi();
        HttpResponse<String> response = api.get("user/info/users", null);

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            System.err.println("Error fetching usernames: " + response.statusCode() + " - " + response.body());
            return null;
        }
    }

    public static boolean updateUser(User user) {
        ServerApi api = new ServerApi();

        // Serialize the user object to JSON
        String userJson = user.toJson();

        try {
            HttpResponse<String> response = api.post("user/update", null, userJson); // Pass null for headers
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
}