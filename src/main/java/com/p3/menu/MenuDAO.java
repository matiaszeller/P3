package com.p3.menu;

import com.p3.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

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
}
