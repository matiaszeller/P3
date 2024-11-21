package com.p3.managerDaily;

import com.p3.networking.ServerApi;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ManagerDailyDAO {
    private final ServerApi api = new ServerApi();

    public List<Map<String, Object>> getTimelogsForDate(LocalDate date) {
        String url = "timelog/getTimelogsByDate?date=" + date;

        HttpResponse response = api.get(url, null);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(
                    response.body().toString(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Shit aint good cuh.. DAO gal på den..", e);
        }
    }
}
