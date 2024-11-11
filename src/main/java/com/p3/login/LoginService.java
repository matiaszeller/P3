package com.p3.login;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {
    public boolean validateUser(String username, RoleHolder roleHolder) {
        boolean isValid = false;

/*
        com.p3.networking.Net request = new com.p3.networking.Net();
        String Response = request.sendRequestToServer(username);
        if (Response.equals("correct") == true) {
            return true;
        } else {
            return false;
        }
    }
}
*/


        String sql = "SELECT role FROM users WHERE username = ?";
        try (Connection con = LoginDAO.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    roleHolder.setEmploymentRole(rs.getString("role"));
                    isValid = true;
                }
            }
        } catch (SQLException e) {
         e.printStackTrace(System.out);
        }

        return isValid;
    }


    public boolean validateManager(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ? AND role = 'manager'";

        try (Connection connection = LoginDAO.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                // Bcrypt checks the inputted password against the hashed password in the database
                return BCrypt.checkpw(password, storedPassword);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
            return false;
        }
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }


}


