package com.p3.history;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryService {

    private final HistoryDAO historyDAO = new HistoryDAO();

    public Map<LocalDate, Map<String, LocalDateTime>> getWeeklyTimelogEvents(LocalDate weekStart, LocalDate weekEnd, int userId) {
        return historyDAO.getWeeklyTimelogEvents(userId, weekStart, weekEnd);
    }

    public int calculateMaxEndHour(LocalDate weekStart, LocalDate weekEnd, int userId) {
        return historyDAO.calculateMaxEndHour(weekStart, weekEnd, userId);
    }


        private static List<Map<String, Object>> timelogs = new ArrayList<>();


    }



