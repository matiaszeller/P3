package com.p3.userEditor;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
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

    public void loadMenuPage(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(UserEditService.class.getResource("/com.p3.menu/MenuPage.fxml"));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadManagerDailyPage(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(UserEditService.class.getResource("/com.p3.managerDaily/ManagerDaily.fxml"));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadWeeklyPage(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(UserEditService.class.getResource("/com.p3.overview/WeeklyOverview.fxml"));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadLoginPage(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(UserEditService.class.getResource("/com.p3.login/LoginPage.fxml"));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene loginScene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(loginScene);
            stage.setTitle("Time Registration System");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
