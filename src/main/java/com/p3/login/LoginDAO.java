package com.p3.login;

import com.p3.config.DatabaseConfig;
import java.sql.*;
import java.time.LocalDateTime;

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

    public int getUserId(String username) {
        int userId = -1;
        String sql = "SELECT user_id FROM users WHERE username = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userId;
    }

    public String getUserFullName(String username) {
        String fullName = null;
        String sql = "SELECT full_name FROM users WHERE username = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    fullName = rs.getString("full_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return fullName;
    }

    public boolean getClockedInStatus(String username) {
        boolean clockedIn = false;
        String sql = "SELECT clocked_in FROM users WHERE username = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    clockedIn = rs.getBoolean("clocked_in");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clockedIn;
    }

    public void setClockedInStatus(String username, boolean status) {
        String sql = "UPDATE users SET clocked_in = ? WHERE username = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, status);
            ps.setString(2, username);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertCheckInEvent(int userId, LocalDateTime eventTime) {
        String sql = "INSERT INTO timelog (user_id, shift_date, event_time, event_type) VALUES (?, CURDATE(), ?, 'check_in')";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(eventTime));
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}