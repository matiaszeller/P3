package com.p3.employeeHistory;

import com.p3.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.Objects;

public class EmployeeHistoryController {

    private final EmployeeHistoryService employeeHistoryService = new EmployeeHistoryService();
    private int weeklyMaxAmountSingleShiftHours;

    @FXML
    private Button logOutButton;
    @FXML
    private Button goBackButton;
    @FXML
    private VBox contentContainer;
    @FXML
    private Button prevWeekButton;
    @FXML
    private Button nextWeekButton;
    @FXML
    private Label weekNumberLabel;
    @FXML
    private Label weekWorkHoursLabel;

    private LocalDate date;

    @FXML
    private void initialize() {
        setActionHandlers();

        date = LocalDate.now();
        handleWeekTimelogs(date);   // First time will always default to current day, could be stored as session data if this is not preffered
    }

    private void setActionHandlers() {
        logOutButton.setOnAction(event -> handleLogOut());
        goBackButton.setOnAction(event -> loadStage("/com.p3.menu/MenuPage.fxml"));
        prevWeekButton.setOnAction(event -> fetchWeekHistory(-1));
        nextWeekButton.setOnAction(event -> fetchWeekHistory(1));
    }

    private void handleLogOut() {
        Session.clearSession();
        loadStage("/com.p3.login/LoginPage.fxml");
    }

    private void loadStage(String fxmlPath) {   // Loads any stage given by the path provided
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = (Stage) logOutButton.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWeekTimelogs(LocalDate date) {
        JSONArray jsonArray = new JSONArray(employeeHistoryService.getWeekTimelogs(date, Session.getCurrentUserId()));

        contentContainer.getChildren().removeIf(node -> node instanceof VBox);  // Clears previous vbox
        weekNumberLabel.setText(String.format("%d | Uge nr: %d", date.getYear(), date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)));

        Duration weekWorkHours = Duration.ZERO;
        weeklyMaxAmountSingleShiftHours = 0;

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray dayEvents = jsonArray.getJSONArray(i);


            VBox dayBox = dayEvents.isEmpty() ? createEmptyDayShiftBox(toMonday(date).plusDays(i)) : createDayShiftBox(dayEvents);
            contentContainer.getChildren().add(dayBox);
            weekWorkHours = weekWorkHours.plus(calculateDayWorkHours(dayEvents));
        }

        weekWorkHoursLabel.setText(formatWorkHours(weekWorkHours));
    }

    private VBox createEmptyDayShiftBox(LocalDate date) {
        // Create VBox for day
        VBox dayVBox = new VBox();
        HBox.setHgrow(dayVBox, Priority.ALWAYS);
        VBox.setVgrow(dayVBox, Priority.ALWAYS);
        dayVBox.getStyleClass().add("dayShiftBox");

        // Create header for day
        HBox dayBoxHeader = new HBox();
        HBox.setHgrow(dayBoxHeader, Priority.ALWAYS);
        dayBoxHeader.getStyleClass().add("dayShiftBoxHeader");

        HBox emptyDayBox = new HBox();
        HBox.setHgrow(emptyDayBox, Priority.ALWAYS);
        VBox.setVgrow(emptyDayBox, Priority.ALWAYS);
        emptyDayBox.getStyleClass().add("emptyShiftBox");
        emptyDayBox.setAlignment(Pos.CENTER);

        Label emptyShiftLabel = new Label();
        emptyShiftLabel.getStyleClass().add("emptyShiftLabel");
        emptyShiftLabel.setText("Ingen Vagt");
        emptyDayBox.getChildren().add(emptyShiftLabel);


        // Set label for day name and date
        Label dayLabel = new Label();
        String weekDay = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("da", "DK"));
        String weekDayCapitalized = weekDay.substring(0, 1).toUpperCase(Locale.ENGLISH) + weekDay.substring(1);

        dayLabel.setText( weekDayCapitalized + " | " + date);
        dayBoxHeader.getChildren().add(dayLabel);
        dayVBox.getChildren().add(dayBoxHeader);
        dayVBox.getChildren().add(emptyDayBox);


        return dayVBox;
    }

    private VBox createDayShiftBox(JSONArray dayTimelogs) {
        JSONObject firstIndexObject = dayTimelogs.getJSONObject(0);

        // Create VBox for day
        VBox dayVBox = new VBox();
        HBox.setHgrow(dayVBox, Priority.ALWAYS);
        VBox.setVgrow(dayVBox, Priority.ALWAYS);
        dayVBox.getStyleClass().add("dayShiftBox");

        // Create header for day
        HBox dayBoxHeader = new HBox();
        HBox.setHgrow(dayBoxHeader, Priority.ALWAYS);
        dayBoxHeader.getStyleClass().add("dayShiftBoxHeader");

            // Set label for day name and date
        Label dayLabel = new Label();
        String weekDay = LocalDate.parse(firstIndexObject.getString("shift_date"))
                .getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, new Locale("da", "DK"));
        String weekDayCapitalized = weekDay.substring(0, 1).toUpperCase(Locale.ENGLISH) + weekDay.substring(1);

        dayLabel.setText( weekDayCapitalized + " | " + LocalDate.parse(firstIndexObject.getString("shift_date")).toString());
        dayBoxHeader.getChildren().add(dayLabel);
        dayVBox.getChildren().add(dayBoxHeader);

        System.out.println(weekDay);

        // Create HBox for day hours
        HBox dayHoursHBox = new HBox();
        HBox.setHgrow(dayHoursHBox, Priority.ALWAYS);
        VBox.setVgrow(dayHoursHBox, Priority.ALWAYS);
        dayHoursHBox.getStyleClass().add("dayHoursHBox");


        // Default times. Start 07, End 17 - Gets day from object of array
        LocalDateTime startTime = LocalDateTime.parse(firstIndexObject.getString("event_time"))
                                                                                    .withHour(7)
                                                                                    .withMinute(0)
                                                                                    .withSecond(0)
                                                                                    .withNano(0);
        LocalDateTime endTime = LocalDateTime.parse(firstIndexObject.getString("event_time"))
                                                                                    .withHour(17)
                                                                                    .withMinute(0)
                                                                                    .withSecond(0)
                                                                                    .withNano(0);

        for (int j = 0; j < dayTimelogs.length(); j++) { // Gets amount of objects to add to dayShiftBox
            JSONObject timelog = dayTimelogs.getJSONObject(j);
            LocalDateTime eventTime = LocalDateTime.parse(timelog.getString("event_time"));

            // If evenTimes are before or after default times, floor and ceil them and replace default times
            if(eventTime.isBefore(startTime)) {
                startTime = eventTime.withMinute(0).withSecond(0).withNano(0);
            }
            else if(eventTime.isAfter(endTime)) {
                endTime = eventTime.withMinute(0).withSecond(0).withNano(0);
                endTime = endTime.plusHours(1);
            }
        }


        int duration = (int) Duration.between(startTime, endTime).toHours();
        if(weeklyMaxAmountSingleShiftHours < duration) weeklyMaxAmountSingleShiftHours = duration;

        int timelogIndex = 0;
        LocalDateTime currentEventTime = LocalDateTime.parse(dayTimelogs.getJSONObject(timelogIndex).getString("event_time"));
        String currentEventType = dayTimelogs.getJSONObject(timelogIndex).getString("event_type");
        String previousEventType = "null";
        boolean eventHasBeenHit = false;
        boolean firstHit = true;

        // ADD BOXES
        // For each hour, out of max hour, add box and children
        for (int i = 0; i < weeklyMaxAmountSingleShiftHours; i++) {
            LocalDateTime workingTime = startTime.plusHours(i);

            HBox hourBox = new HBox();
            HBox.setHgrow(hourBox, Priority.ALWAYS);
            VBox.setVgrow(hourBox, Priority.ALWAYS);
            hourBox.getStyleClass().add("hourShiftBox");

            StackPane hourBoxContainer = new StackPane();
            HBox.setHgrow(hourBoxContainer, Priority.ALWAYS);
            VBox.setVgrow(hourBoxContainer, Priority.ALWAYS);

            for (int j = 0; j < 30; j++) {
                LocalDateTime minuteTime = workingTime.plusMinutes(j*2);
                HBox minuteBox = new HBox();
                HBox.setHgrow(minuteBox, Priority.ALWAYS);
                VBox.setVgrow(minuteBox, Priority.ALWAYS);

                // Apply the style class based on whether the current minute is before, at, or after an event
                if (!(minuteTime.isBefore(currentEventTime) && eventHasBeenHit)) {
                    if(Objects.equals(previousEventType, "null")){
                        minuteBox.getStyleClass().add("shiftBoxNoneRegistered");
                    } else {
                        minuteBox.getStyleClass().add(previousEventType);
                    }
                } else {
                    switch (previousEventType) {
                        case "check_in", "break_end":
                            if(firstHit) {
                                hourBox.getChildren().getLast().getStyleClass().add("shiftBoxRegistered");
                                minuteBox.getStyleClass().add("shiftBoxRegistered");
                                firstHit = false;
                            } else {
                                minuteBox.getStyleClass().add("shiftBoxRegistered");
                            }
                            break;
                        case "check_out":
                            if(firstHit) {
                                hourBox.getChildren().getLast().getStyleClass().add("shiftBoxNoneRegistered");
                                minuteBox.getStyleClass().add("shiftBoxNoneRegistered");
                                firstHit = false;
                            } else {
                                minuteBox.getStyleClass().add("shiftBoxNoneRegistered");
                            }
                            break;
                        case "break_start":
                            if(firstHit) {
                                hourBox.getChildren().getLast().getStyleClass().add("shiftBoxBreak");
                                minuteBox.getStyleClass().add("shiftBoxBreak");
                                firstHit = false;
                            } else {
                                minuteBox.getStyleClass().add("shiftBoxBreak");
                            }
                            break;
                        case "null":
                            minuteBox.getStyleClass().add("shiftBoxNoneRegistered");
                            break;
                    }
                }

                // If the minute time matches the current event time, move to the next event
                if (minuteTime.isEqual(currentEventTime) && timelogIndex < dayTimelogs.length() - 1) {
                    timelogIndex++;
                    eventHasBeenHit = true;
                    firstHit = true;
                    previousEventType = currentEventType;
                    currentEventTime = LocalDateTime.parse(dayTimelogs.getJSONObject(timelogIndex).getString("event_time"));
                    currentEventType = dayTimelogs.getJSONObject(timelogIndex).getString("event_type");
                }

                hourBox.getChildren().add(minuteBox);
            }

            // Add hourBox i to the "list" of hourBoxes
            hourBoxContainer.getChildren().add(hourBox);

            // Label for each hour
            Label hourLabel = new Label();
            hourLabel.getStyleClass().add("hourShiftBoxLabel");
            hourLabel.setText(String.format("%02d:00", workingTime.getHour()));

            // Add and position label
            hourBoxContainer.getChildren().add(hourLabel);
            StackPane.setAlignment(hourLabel, Pos.TOP_LEFT);

            // Add container of hourBox and label
            dayHoursHBox.getChildren().add(hourBoxContainer);


        }
        // Add "list" of hourboxes to dayBox
        dayVBox.getChildren().add(dayHoursHBox);

        // Create note box
        HBox noteBox = new HBox();
        HBox.setHgrow(noteBox, Priority.ALWAYS);
        VBox.setVgrow(noteBox, Priority.ALWAYS);
        noteBox.getStyleClass().add("noteHBox");
        noteBox.setAlignment(Pos.BOTTOM_RIGHT);


        Button noteButton = new Button();
        noteButton.getStyleClass().add("noteButton");
        noteButton.setText("Tilføj Kommentar");

        noteBox.getChildren().add(noteButton);
        dayHoursHBox.getChildren().add(noteBox);

        return dayVBox;
    }

    private Duration calculateDayWorkHours(JSONArray dayTimelogs) {
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
                    if (checkInTime != null) {
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

    private void fetchWeekHistory(int weeksToAdd) {
        date = date.plusWeeks(weeksToAdd);
        handleWeekTimelogs(date);
    }

    private String formatWorkHours(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }

    private LocalDate toMonday(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
