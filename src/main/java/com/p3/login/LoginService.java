package com.p3.login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {
    public boolean validateUser(String username, RoleHolder roleHolder) {
        boolean isValid = false;

        String sql = "SELECT employment_role FROM user_table WHERE username = ?";
        try (Connection con = LoginDTO.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    roleHolder.setEmploymentRole(rs.getString("employment_role"));
                    isValid = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isValid;
    }
}
