package com.p3.login;

import java.sql.*;

public class LoginDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/database";
    private static final String USER = "root";
    private static final String PASSWORD = "password"; //IMPORTANT: USE YOUR OWN PASSWORD

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}