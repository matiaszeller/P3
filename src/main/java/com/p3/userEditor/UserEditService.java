package com.p3.userEditor;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;


public class UserEditService {
    private final UserEditDAO userEditDAO = new UserEditDAO();

    public List<User> getAllUsers() {
        String jsonResponse = UserEditDAO.userNames();

        if (jsonResponse == null) {
            System.err.println("No valid JSON response from backend.");
            return new ArrayList<>();
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            List<User> users = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject userJson = jsonArray.getJSONObject(i);
                User user = new User(
                        userJson.getInt("user_id"),
                        userJson.getString("username"),
                        userJson.getString("full_name"),
                        userJson.getBoolean("clocked_in"),
                        userJson.getBoolean("on_break"),
                        userJson.getBoolean("logged_in"),
                        userJson.optString("password", null),
                        userJson.getString("role")
                );
                users.add(user);
            }
            return users;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean updateUser(User user) {
        return UserEditDAO.updateUser(user);
    }

    public boolean createUser(User user) {
        return UserEditDAO.createUser(user);
    }

}