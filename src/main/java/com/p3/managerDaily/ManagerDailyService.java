package com.p3.managerDaily;

import com.p3.menu.MenuService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDateTime;
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
                System.out.println(dailyTimelogs);
                System.out.println(startDate);
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

        public static int getEarliestTime(Integer userId) {
            int earliestHour = Integer.MAX_VALUE;
            for (Map<String, Object> log : timelogs) {
                Integer logUserId = (Integer) log.get("user_id");
                if (logUserId.equals(userId)) {

                    String eventTimeStr = (String) log.get("event_time");
                    try {
                        LocalDateTime eventTime = LocalDateTime.parse(eventTimeStr);
                        int logHour = eventTime.getHour();
                        earliestHour = Math.min(earliestHour, logHour);
                    } catch (Exception e) {
                        System.err.println("Error parsing event time: " + eventTimeStr);
                    }
                }
            }
            return earliestHour == Integer.MAX_VALUE ? 0 : earliestHour;
        }

        public static int getLatestTime(Integer userId) {
            int latestHour = Integer.MIN_VALUE;
            for (Map<String, Object> log : timelogs) {
                Integer logUserId = (Integer) log.get("user_id");
                if (logUserId.equals(userId)) {
                    String eventTimeStr = (String) log.get("event_time");
                    try {
                        LocalDateTime eventTime = LocalDateTime.parse(eventTimeStr);
                        int logHour = eventTime.getHour();
                        latestHour = Math.max(latestHour, logHour);
                    } catch (Exception e) {
                        System.err.println("Error parsing event time: " + eventTimeStr);
                    }
                }
            }
            return latestHour == Integer.MIN_VALUE ? 0 : latestHour;
        }
    }

