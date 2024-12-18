package com.p3.managerDaily;

import org.json.JSONArray;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.time.LocalDate;

public class ManagerDailyService {

    private static final ManagerDailyDAO dao = new ManagerDailyDAO();
    private static List<Map<String, Object>> timelogs = new ArrayList<>();

        public void loadTimelogsForRange(LocalDate startDate, int daysCount) {
            timelogs.clear();
        
            for (int i = 0; i < daysCount; i++) {
                LocalDate currentDate = startDate.minusDays(i);

                List<Map<String, Object>> dailyTimelogs = dao.getTimelogsForDate(currentDate);
                    timelogs.addAll(dailyTimelogs);
            }
        }

        public List<Map<String, Object>> getTimelogs() {
            return timelogs;
        }


    public int getEarliestTime(LocalDate date) {
        int earliestCheckInHour = Integer.MAX_VALUE;

        for (Map<String, Object> log : timelogs) {
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

        // Handle case where no 'check_in' events are found
        return earliestCheckInHour == Integer.MAX_VALUE ? 0 : earliestCheckInHour;
    }

    public int getLatestTime(LocalDate date) {
        int latestCheckOutHour = Integer.MIN_VALUE;

        for (Map<String, Object> log : timelogs) {
            String eventTimeStr = (String) log.get("event_time");
            String eventType = (String) log.get("event_type");

            if (eventTimeStr != null && eventType != null) {
                try {
                    // Parse event time
                    LocalDateTime eventTime = LocalDateTime.parse(eventTimeStr);

                    // Check if the event matches the specified date and type (check-out)
                    if (eventTime.toLocalDate().equals(date) && "check_out".equalsIgnoreCase(eventType)) {
                        int logHour = eventTime.getHour();
                        latestCheckOutHour = Math.max(latestCheckOutHour, logHour);

                        // Special case: check-out at 23:59
                        if (eventTime.getHour() == 23 && eventTime.getMinute() == 59) {
                            latestCheckOutHour = 18;
                        }
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid event_time format: " + eventTimeStr);
                } catch (Exception e) {
                    System.err.println("Unexpected error processing log: " + log);
                }
            }
        }

        // Handle case where no 'check_out' events are found
        return latestCheckOutHour == Integer.MIN_VALUE ? 0 : latestCheckOutHour;
    }

    public String getUserFullName(int user_id) {
        return dao.getUserFullNameById(user_id);
    }

    public JSONArray getDayNotes(LocalDate date, int userId) {
        return new JSONArray(dao.getDayNotes(date, userId));
    }

    public JSONArray getDayTimelogs(LocalDate date, int userId) {
        return new JSONArray(dao.getDayTimelogs(date, userId));
    }

}

