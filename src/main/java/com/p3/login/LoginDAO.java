package com.p3.login;

import com.p3.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*  TODO OBS!!!! Disse queries ligger lokalt lige NU. Men de skal flyttes til at køre på serveren.
    Denne class skal kalde de forskellige queries og sende dem tilbage til LoginService.
    Her skal vi måske bruge Jakobs kode til at oprette connections(?)
    Husk på: Controllers: Håndterer UI og kalder services
             Services: Logic
             DAO: Database kald
*/
public class LoginDAO {
    public String getUserRole(String username) {
        String role = null;
        String sql = "SELECT role FROM users WHERE username = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    role = rs.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return role;
    }

    public String getManagerPassword(String username) {
        String password = null;
        String sql = "SELECT password FROM users WHERE username = ? AND role = 'manager'";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    password = rs.getString("password");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return password;
    }
}