package com.p3.managerDaily;

import com.p3.menu.MenuService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;
import java.util.List;
import java.time.LocalDate;

public class ManagerDailyService {

    private static final ManagerDailyDAO dao = new ManagerDailyDAO();
    private static List<Map<String, Object>> timelogs = new ArrayList<>();

    public static void loadLoginPage(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MenuService.class.getResource("/com.p3.login/LoginPage.fxml"));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene loginScene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(loginScene);

            stage.setTitle("Time Registration System");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void setTimelogs(List<Map<String, Object>> logs) {
        timelogs = logs;
    }

        public static void loadTimelogsForRange(LocalDate startDate, int daysCount) {
            for (int i = 0; i < daysCount; i++) {
                LocalDate currentDate = startDate.minusDays(i);
                List<Map<String, Object>> dailyTimelogs = dao.getTimelogsForDate(currentDate);
                    timelogs.addAll(dailyTimelogs);
            }
        }

        public static List<Map<String, Object>> getTimelogs() {
            return timelogs;
        }


        public static String getEventForHour(Integer userId, int hour) {
            for (Map<String, Object> log : timelogs) {
                Integer logUserId = (Integer) log.get("user_id");
                if (logUserId.equals(userId)) {

                    String eventTimeStr = (String) log.get("event_time");
                    try {

                        LocalDateTime eventTime = LocalDateTime.parse(eventTimeStr);
                        int logHour = eventTime.getHour();
                        if (logHour == hour) {
                            return (String) log.get("event_type");
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing event time: " + eventTimeStr);
                    }
                }
            }
            return "No Event";
        }

    public int getEarliestTime(Integer userId, LocalDate date) {
        int earliestCheckInHour = Integer.MAX_VALUE;

        for (Map<String, Object> log : timelogs) {
            Integer logUserId = (Integer) log.get("user_id");
            if (logUserId != null && logUserId.equals(userId)) {
                String eventTimeStr = (String) log.get("event_time");
                String eventType = (String) log.get("event_type");

                if (eventTimeStr != null && eventType != null) {
                    try {
                        // Parse event time
                        LocalDateTime eventTime = LocalDateTime.parse(eventTimeStr);

                        // Check if the event matches the specified date and type (check-in)
                        if (eventTime.toLocalDate().equals(date) && "check_in".equalsIgnoreCase(eventType)) {
                            int logHour = eventTime.getHour();
                            earliestCheckInHour = Math.min(earliestCheckInHour, logHour);
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Invalid event_time format: " + eventTimeStr);
                    } catch (Exception e) {
                        System.err.println("Unexpected error processing log: " + log);
                    }
                }
            }
        }

        // Handle case where no 'check_in' events are found
        return earliestCheckInHour == Integer.MAX_VALUE ? 0 : earliestCheckInHour;
    }


    public int getLatestTime(Integer userId, LocalDate date) {
        int latestCheckOutHour = Integer.MIN_VALUE;

        for (Map<String, Object> log : timelogs) {
            Integer logUserId = (Integer) log.get("user_id");
            if (logUserId != null && logUserId.equals(userId)) {
                String eventTimeStr = (String) log.get("event_time");
                String eventType = (String) log.get("event_type");

                if (eventTimeStr != null && eventType != null) {
                    try {
                        // Parse event time
                        LocalDateTime eventTime = LocalDateTime.parse(eventTimeStr);

                        // Check if the event matches the specified date
                        if (eventTime.toLocalDate().equals(date) && "check_out".equalsIgnoreCase(eventType)) {
                            int logHour = eventTime.getHour();
                            latestCheckOutHour = Math.max(latestCheckOutHour, logHour);
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Invalid event_time format: " + eventTimeStr);
                    } catch (Exception e) {
                        System.err.println("Unexpected error processing log: " + log);
                    }
                }
            }
        }

        // Handle case where no 'check_out' events are found
        return latestCheckOutHour == Integer.MIN_VALUE ? 0 : latestCheckOutHour;
    }




    public String getUserFullName(int user_id) {
        String jsonResponse = dao.getUserFullNameById(user_id);
        org.json.JSONObject json = new org.json.JSONObject(jsonResponse);
        return json.getString("full_name");
    }

    }

