package com.p3.history;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class HistoryDAO {

    private final String url = "jdbc:mysql://localhost:3306/database";
    private final String user = "root";
    private final String password = "Westcoast33!";

    public boolean checkDatabaseConnection() {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            return connection != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
