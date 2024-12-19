package com.p3.overview;

import java.time.LocalDate;
import java.util.*;

public class WeeklyOverviewService {

    private static final WeeklyOverviewDAO dao = new WeeklyOverviewDAO();
    private static List<Map<String, Object>> weeklyTimelogs = new ArrayList<>();

    // Load for year
    public static void loadWeeklyTimelogs(int year) {
        weeklyTimelogs = dao.getAllWeeklyLogs();

        // Filter logs by the selected year
        weeklyTimelogs = weeklyTimelogs.stream()
                .filter(log -> {
                    LocalDate weekStart = LocalDate.parse((String) log.get("weekStart"));
                    return weekStart.getYear() == year;
                })
                .toList();
    }

    public static List<Map<String, Object>> getWeeklyTimelogs() {
        return weeklyTimelogs;
    }

}