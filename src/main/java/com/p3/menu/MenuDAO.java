package com.p3.menu;

import com.p3.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    public boolean getOnBreakStatus(int userId) {
        boolean onBreak = false;
        String sql = "SELECT on_break FROM users WHERE user_id = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    onBreak = rs.getBoolean("on_break");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return onBreak;
    }

    public void setOnBreakStatus(int userId, boolean status) {
        String sql = "UPDATE users SET on_break = ? WHERE user_id = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertBreakStartEvent(int userId, LocalDateTime eventTime) {
        String sql = "INSERT INTO timelog (user_id, shift_date, event_time, event_type) VALUES (?, CURDATE(), ?, 'break_start')";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(eventTime));
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertBreakEndEvent(int userId, LocalDateTime eventTime) {
        String sql = "INSERT INTO timelog (user_id, shift_date, event_time, event_type) VALUES (?, CURDATE(), ?, 'break_end')";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(eventTime));
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Event> getTodaysEventsForUser(int userId) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT event_time, event_type FROM timelog WHERE user_id = ? AND shift_date = CURDATE() ORDER BY event_time";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime eventTime = rs.getTimestamp("event_time").toLocalDateTime();
                    String eventType = rs.getString("event_type");
                    events.add(new Event(eventTime, eventType));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return events;
    }

    public static class Event {
        private LocalDateTime eventTime;
        private String eventType;

        public Event(LocalDateTime eventTime, String eventType) {
            this.eventTime = eventTime;
            this.eventType = eventType;
        }

        public LocalDateTime getEventTime() {
            return eventTime;
        }

        public String getEventType() {
            return eventType;
        }
    }

    public void insertCheckOutEvent(int userId, LocalDateTime eventTime) {
        String sql = "INSERT INTO timelog (user_id, shift_date, event_time, event_type) VALUES (?, CURDATE(), ?, 'check_out')";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(eventTime));
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setClockedInStatusById(int userId, boolean status) {
        String sql = "UPDATE users SET clocked_in = ? WHERE user_id = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
