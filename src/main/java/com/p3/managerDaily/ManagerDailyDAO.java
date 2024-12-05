package com.p3.managerDaily;

import com.p3.networking.ServerApi;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;

public class ManagerDailyDAO {
    private final ServerApi api = new ServerApi();

    public List<Map<String, Object>> getTimelogsForDate(LocalDate date) {
        String url = "timelog/getTimelogsByDate?date=" + date;


        HttpResponse response = api.get(url, null, true);

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
    public String getUserFullNameById(int user_id) {
        String url = "user/fullNameId/" + user_id;
        HttpResponse response = api.get(url, null, true);

        return (String) response.body();
    }

    public String getDayNotes(LocalDate date, int userId){
        String url = "note/day?date=" + date + "&userId=" + userId;
        HttpResponse response = api.get(url, null, true);

        return (String) response.body();
    }
}
