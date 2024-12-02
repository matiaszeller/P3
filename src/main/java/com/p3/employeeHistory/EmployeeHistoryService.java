package com.p3.employeeHistory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;


public class EmployeeHistoryService {
    EmployeeHistoryDAO employeeHistoryDAO = new EmployeeHistoryDAO();

    // Return list of lists - Each list contains all timelogs for a day. Allows for looping through each day (no more than 7) and append to pane on fxml
    public JSONArray getWeekTimelogs(LocalDate localDate, int userId) {
        // Response is in string format and is converted to a jsonArray
        return new JSONArray(employeeHistoryDAO.getWeekTimelogs(localDate, userId));
    }

    public Duration calculateDayWorkHours(JSONArray dayTimelogs) {
        LocalDateTime checkInTime = null;
        LocalDateTime breakStartTime = null;
        Duration totalBreakTime = Duration.ZERO;
        Duration dayWorkHours = Duration.ZERO;

        for (int j = 0; j < dayTimelogs.length(); j++) {
            JSONObject timelog = dayTimelogs.getJSONObject(j);
            String eventType = timelog.getString("event_type");
            LocalDateTime eventTime = LocalDateTime.parse(timelog.getString("event_time"));

            switch (eventType) {
                case "check_in":
                    checkInTime = eventTime;
                    break;
                case "break_start":
                    breakStartTime = eventTime;
                    break;
                case "break_end":
                    if (breakStartTime != null) {
                        totalBreakTime = totalBreakTime.plus(Duration.between(breakStartTime, eventTime));
                        breakStartTime = null;
                    }
                    break;
                case "check_out":
                    if (checkInTime != null && !(eventTime.getHour() == 23 && eventTime.getMinute() == 59)) {
                        Duration shiftDuration = Duration.between(checkInTime, eventTime);
                        dayWorkHours = dayWorkHours.plus(shiftDuration.minus(totalBreakTime));
                        checkInTime = null;
                        totalBreakTime = Duration.ZERO;
                    }
                    break;
                default:
                    break;
            }
        }
        return dayWorkHours;
    }

    public String formatWorkHours(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }

    public LocalDate toMonday(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public String getMinuteBoxStyleClassFromEventType(String eventType) {
        switch (eventType) {
            case "check_in", "break_end":
                return "shiftBoxRegistered";
            case "break_start":
                return "shiftBoxBreak";
            case "check_out", "none":
                return "shiftBoxNoneRegistered";
        }

        return null;
    }

    public String setStringForShiftSequenceLabel(ShiftSequence shiftSequence, List<String> editedTimelogs) {
        String returnString = "";
        String eventType = shiftSequence.sequenceType;
        String editedString = (shiftSequence.getEdited()) ? "\n(Redigeret)" : "";


        switch (eventType) {
            // Do not add any label for empty types
            case "empty":
                break;
            case "check_in":
                returnString = String.format("Start %s\n%02d:%02d",
                        editedString,
                        shiftSequence.startTime.getHour(),
                        shiftSequence.startTime.getMinute());
                break;
            case "check_out":
                returnString = String.format("Slut %s\n%02d:%02d",
                        editedString,
                        shiftSequence.endTime.getHour(),
                        shiftSequence.endTime.getMinute());
                break;

            case "break_start":
                returnString = String.format("Pause %s\n%02d:%02d\n%02d:%02d",
                        editedString,
                        shiftSequence.startTime.getHour(),
                        shiftSequence.startTime.getMinute(),
                        shiftSequence.endTime.getHour(),
                        shiftSequence.endTime.getMinute());
                break;
            case "break_end":
                if(editedTimelogs.contains("check_out")){break;}
                if(shiftSequence.getEndTime().getHour() == 23 && shiftSequence.getEndTime().getMinute() == 59) {
                    break;
                }
                returnString = String.format("Slut %s\n%02d:%02d",
                        editedString,
                        shiftSequence.endTime.getHour(),
                        shiftSequence.endTime.getMinute());
                break;
        }
        return returnString;
    }

    public List<String> getEditedTimelog(JSONArray timelogs) {
        List<String> editedTimelogs = new ArrayList<>();

        for (int i = 0; i < timelogs.length(); i++) {
            String edited = timelogs.getJSONObject(i).optString("edited_time", null);
            if(edited != null) {
                editedTimelogs.add(timelogs.getJSONObject(i).getString("event_type"));
            }
        }

        return editedTimelogs;
    }
}
