package com.p3.userEditor;
import org.json.JSONObject;


public class User {
    private int user_id;
    private String username;
    private String full_name;
    private boolean clockedIn;
    private boolean onBreak;
    private boolean loggedIn;
    private String password;
    private String role;

    // Constructor
    public User(int userId, String username, String fullName, boolean clockedIn, boolean onBreak, boolean loggedIn, String password, String role) {
        this.user_id = userId;
        this.username = username;
        this.full_name = fullName;
        this.clockedIn = clockedIn;
        this.onBreak = onBreak;
        this.loggedIn = loggedIn;
        this.password = password;
        this.role = role;
    }

    // Getters & Setters
    public int getUserId() { return user_id; }
    public void setUserId(int userId) { this.user_id = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return full_name; }
    public void setFullName(String fullName) { this.full_name = fullName; }

    public boolean isClockedIn() { return clockedIn; }
    public void setClockedIn(boolean clockedIn) { this.clockedIn = clockedIn; }



    public boolean isOnBreak() { return onBreak; }
    public void setOnBreak(boolean onBreak) { this.onBreak = onBreak; }

    public boolean isLoggedIn() { return loggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }


    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("user_id", user_id);
        json.put("username", username);
        json.put("full_name", full_name);
        json.put("clocked_in", clockedIn);
        json.put("on_break", onBreak);
        json.put("logged_in", loggedIn);
        json.put("password", password);
        json.put("role", role);
        return json.toString();
    }
}
