package com.p3.session;

public class Session {
    private static int currentUserId;
    private static String currentUserFullName;
    private static String currentUserRole;
    private static String apiKey;

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    public static String getCurrentUserFullName() {
        return currentUserFullName;
    }

    public static void setCurrentUserFullName(String fullName) {
        currentUserFullName = fullName;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static void setCurrentUserRole(String role) {
        currentUserRole = role;
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static void setApiKey(String key) {
        apiKey = key;
    }

    public static void clearSession() {
        currentUserId = 0;
        currentUserFullName = null;
        currentUserRole = null;
        apiKey = null;
    }
}