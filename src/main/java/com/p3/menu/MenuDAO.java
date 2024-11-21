package com.p3.menu;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.p3.networking.ServerApi;
import com.p3.session.Session;
import org.json.JSONObject;

public class MenuDAO {

    private final ServerApi api = new ServerApi();


    public String getOnBreakStatus(int userId) {
        String url = "user/breakStatus/" + userId;
        HttpResponse response = api.get(url, null);

        return (String) response.body();
    }

    public void setOnBreakStatus(int userId, boolean status) {
        String url = "user/breakStatus/" + userId + "?status=" + status;
        try {
            api.put(url, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postBreakStartEvent(int userId) {
        String url = "timelog/breakStart";

        String jsonBody = "{ \"user_id\": " + userId + ", " +
                "\"event_type\": \"break_start\" " +
                "}";
        try {
            api.post(url, null, jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postBreakEndEvent(int userId) {
        String url = "timelog/breakEnd";

        String jsonBody = "{ \"user_id\": " + userId + ", " +
                "\"event_type\": \"break_end\" " +
                "}";

        try {
            api.post(url, null, jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTodaysEventsForUser(int userId, LocalDate today) {
        String url = "timelog/ALL?user_id=" + userId + "&date=" + today;
        HttpResponse response = api.get(url, null);

        return (String) response.body();
    }

    public static class Event {     // TODO Lav class i sin egen fil
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

    public void postCheckOutEvent(int userId) {
        String url = "timelog/checkOut";

        String jsonBody = "{ \"user_id\": " + userId + ", " +
                "\"event_type\": \"check_out\" " +
                "}";
        try {
            api.post(url, null, jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void putClockedInStatusById(int userId, boolean status) {
        String url = "user/clockInStatus/userId/" + userId + "?status=" + status;
        try {
            api.put(url, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLastCheckOutEvent(int userId) {
        String url = "timelog/lastCheckOut?user_id=" + userId;
        HttpResponse response = api.get(url, null);
        return (String) response.body();
    }

    public void postMissedCheckoutNote(int userId, String note, LocalDate missedShiftDate) {
        String url = "note";
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("note_date", missedShiftDate.toString());
        jsonBody.put("writer_id", userId);
        jsonBody.put("recipient_id", userId); // er det her rigtigt?
        jsonBody.put("full_name", Session.getCurrentUserFullName());
        jsonBody.put("written_note", note);

        try {
            api.post(url, null, jsonBody.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String noteExistsForDate(int userId, LocalDate noteDate) {
        String url = "note/exists?userId=" + userId + "&noteDate=" + noteDate;
        HttpResponse response = api.get(url, null);
        return (String) response.body();
    }
}
