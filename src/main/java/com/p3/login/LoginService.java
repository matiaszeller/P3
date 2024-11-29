package com.p3.login;

import org.mindrot.jbcrypt.BCrypt;

//todo implement networking feature, to talk with server.
public class LoginService {
    private final LoginDAO loginDAO = new LoginDAO();

    public String validateUser(String username){
        String jsonResponse = loginDAO.getUserRole(username);
        org.json.JSONObject json = new org.json.JSONObject(jsonResponse);
        return json.getString("role");
    }

    public boolean validateManager(String username, String password) {
        String jsonResponse = loginDAO.getManagerPassword(username);
        org.json.JSONObject json = new org.json.JSONObject(jsonResponse);
        String storedPassword = json.getString("password");
        return storedPassword != null && BCrypt.checkpw(password, storedPassword);
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public int getUserId(String username) {
        String jsonResponse = loginDAO.getUserId(username);
        org.json.JSONObject json = new org.json.JSONObject(jsonResponse);
        return json.getInt("user_id");
    }

    public String getUserFullName(String username) {
        String jsonResponse = loginDAO.getUserFullName(username);
        org.json.JSONObject json = new org.json.JSONObject(jsonResponse);
        return json.getString("full_name");
    }

    public boolean getClockedInStatus(String username) {
        String jsonResponse = loginDAO.getClockedInStatus(username);
        org.json.JSONObject json = new org.json.JSONObject(jsonResponse);
        return json.getBoolean("clocked_in");
    }

    public void setClockedInStatus(String username, boolean status) {
        loginDAO.setClockedInStatus(username, status);
    }

    public void postCheckInEvent(int userId) {
        loginDAO.postCheckInEvent(userId);
    }

    public String getApiKey(String username) {
        String jsonResponse = loginDAO.getApiKey(username);
        org.json.JSONObject json = new org.json.JSONObject(jsonResponse);
        return json.getString("api_key");
    }
}