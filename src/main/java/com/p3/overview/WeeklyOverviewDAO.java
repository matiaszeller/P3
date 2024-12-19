package com.p3.overview;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p3.networking.ServerApi;
import java.net.http.HttpResponse;
import java.util.*;

public class WeeklyOverviewDAO {

    private final ServerApi api = new ServerApi();

    public List<Map<String, Object>> getAllWeeklyLogs() {
        String url = "weeklytimelog";
        HttpResponse<String> response = api.get(url, null, true);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Map<String, Object>> logs = objectMapper.readValue(
                    response.body(), new TypeReference<List<Map<String, Object>>>() {}
            );
            return logs;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting weekly timelogs", e);
        }
    }
}