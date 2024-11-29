package com.p3.history;

import com.p3.networking.ServerApi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryDAO {
    private final ServerApi api = new ServerApi();

    public Map<LocalDate, Map<String, LocalDateTime>> getWeeklyEvents(LocalDate weekStart, LocalDate weekEnd, int userId) {
        String url = "timelog/getWeeklyEvents?userId=" + userId + "&weekStart=" + weekStart + "&weekEnd=" + weekEnd;
        HttpResponse<String> response = api.get(url, null);
        Map<LocalDate, Map<String, LocalDateTime>> events = new HashMap<>();

        if (response != null && response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            for (String key : jsonResponse.keySet()) {
                LocalDate date = LocalDate.parse(key);
                JSONObject dayEvents = jsonResponse.getJSONObject(key);
                Map<String, LocalDateTime> dayEventMap = new HashMap<>();
                for (String eventKey : dayEvents.keySet()) {
                    dayEventMap.put(eventKey, LocalDateTime.parse(dayEvents.getString(eventKey)));
                }
                events.put(date, dayEventMap);
            }
        }
        return events;
    }

    public int calculateMaxEndHour(LocalDate weekStart, LocalDate weekEnd, int userId) {
        String url = "timelog/calculateMaxEndHour?userId=" + userId + "&weekStart=" + weekStart + "&weekEnd=" + weekEnd;
        HttpResponse<String> response = api.get(url, null);

        if (response != null && response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getInt("maxHour");
        }
        return 24; // Default end hour
    }

    public Map<LocalDate, Map<String, LocalDateTime>> getWeeklyTimelogEvents(int userId, LocalDate weekStart, LocalDate weekEnd) {
        String url = "timelog/getTimelogEvents?userId=" + userId + "&weekStart=" + weekStart + "&weekEnd=" + weekEnd;
        HttpResponse<String> response = api.get(url, null);
        Map<LocalDate, Map<String, LocalDateTime>> events = new HashMap<>();
        System.out.println("Response: " + (response != null ? response.body() : "No response")); // Print response for debugging
        System.out.println("Request URL: " + url);  // To verify the URL being sent

        if (response != null && response.statusCode() == 200) {
            JSONArray jsonResponse = new JSONArray(response.body());

            for (int i = 0; i < jsonResponse.length(); i++) {
                JSONObject eventJson = jsonResponse.getJSONObject(i);
                LocalDate date = LocalDate.parse(eventJson.getString("shift_date"));
                String eventType = eventJson.getString("event_type");
                LocalDateTime eventTime = LocalDateTime.parse(eventJson.getString("event_time"));

                events.putIfAbsent(date, new HashMap<>());
                events.get(date).put(eventType, eventTime);
            }
        }
        return events;
    }

    // Note-related methods

    // Add a new note
    public boolean addNote(int writerId, int recipientId, String fullName, String writtenNote) {
        String url = "note";

        // Construct the JSON body
        String jsonBody = new JSONObject()
                .put("writerId", writerId)
                .put("recipientId", recipientId)
                .put("noteDate", LocalDate.now().toString())
                .put("fullName", fullName)
                .put("writtenNote", writtenNote)
                .toString();

        try {
            System.out.println("Sending POST request to: " + url);
            System.out.println("Request Body: " + jsonBody);

            HttpResponse<String> response = api.post(url, null, jsonBody);

            if (response != null) {
                System.out.println("Response Code: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
            } else {
                System.err.println("No response received from the server.");
            }

            return response != null && response.statusCode() == 201; // HTTP 201 Created
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update an existing note
    public boolean updateNote(int noteId, String updatedNoteContent) {
        String url = "note/" + noteId;

        // Construct the JSON body
        String jsonBody = new JSONObject()
                .put("writtenNote", updatedNoteContent)
                .toString();

        try {
            HttpResponse<String> response = api.put(url, null, jsonBody); // Assuming PUT is used for updates
            return response != null && response.statusCode() == 200; // HTTP 200 OK
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Fetch a specific note by ID
    public JSONObject getNoteById(int noteId) {
        String url = "note/" + noteId;

        try {
            HttpResponse<String> response = api.get(url, null);
            if (response != null && response.statusCode() == 200) {
                return new JSONObject(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Delete a note by ID
    public boolean deleteNoteById(int noteId) {
        String url = "note/" + noteId;

        try {
            HttpResponse<String> response = api.delete(url, null);
            return response != null && response.statusCode() == 204; // HTTP 204 No Content
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
