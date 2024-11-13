package com.p3.session;

public class Session {
    private static int currentUserId;
    private static String currentUserFullName;

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

    public static void clearSession() {
        currentUserId = 0;
        currentUserFullName = null;
    }
}
