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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoryDAO {
    private final ServerApi api = new ServerApi();


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



}
