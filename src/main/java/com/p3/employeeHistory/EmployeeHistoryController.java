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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeHistoryController {

    private final EmployeeHistoryService employeeHistoryService = new EmployeeHistoryService();
    private int weeklyMaxAmountSingleShiftHours;
    private LocalDate date;
    private int weeklyStartHour;
    private int weeklyEndHour;

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


    @FXML
    private void initialize(){
        setActionHandlers();

        date = LocalDate.now();
        handleWeekTimelogs(date);   // First time will always default to current day, could be stored as session data if this is not preffered
    }

    private void setActionHandlers(){


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
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWeekTimelogs(LocalDate date) {
        JSONArray jsonArray = new JSONArray(employeeHistoryService.getWeekTimelogs(date, Session.getCurrentUserId()));

        contentContainer.getChildren().removeIf(node -> node instanceof VBox);  // Clears previous vbox
        weekNumberLabel.setText(String.format("%d | Uge nr: %d", date.getYear(), date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)));

        Duration weekWorkHours = Duration.ZERO;

        calculateWeeklyDayValues(jsonArray);

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
        LocalDateTime firstTime = LocalDateTime.parse(firstIndexObject.getString("event_time"));


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

        dayLabel.setText(weekDayCapitalized + " | " + LocalDate.parse(firstIndexObject.getString("shift_date")));
        dayBoxHeader.getChildren().add(dayLabel);
        dayVBox.getChildren().add(dayBoxHeader);

        // Create HBox for day hours
        HBox dayHoursHBox = new HBox();
        HBox.setHgrow(dayHoursHBox, Priority.ALWAYS);
        VBox.setVgrow(dayHoursHBox, Priority.ALWAYS);
        dayHoursHBox.getStyleClass().add("dayHoursHBox");

        int shiftSequenceIndex = 0;
        List<ShiftSequence> shiftSequences = generateShiftSequence(dayTimelogs);
        String currentStyleClass = shiftSequences.get(shiftSequenceIndex).styleClass;
        int remainingDuration = shiftSequences.get(shiftSequenceIndex).duration;

// Loop through each hour to create minute boxes
        for (int i = 0; i < weeklyMaxAmountSingleShiftHours; i++) {
            LocalDateTime workingTime = firstTime.withHour(weeklyStartHour).plusHours(i);

            // Hour box, contains stackpane
            HBox hourBox = new HBox();
            HBox.setHgrow(hourBox, Priority.ALWAYS);
            VBox.setVgrow(hourBox, Priority.ALWAYS);
            hourBox.getStyleClass().add("hourShiftBox");

            // Stackpane, contains minute boxes
            StackPane hourBoxContainer = new StackPane();
            HBox.setHgrow(hourBoxContainer, Priority.ALWAYS);
            VBox.setVgrow(hourBoxContainer, Priority.ALWAYS);

            for (int j = 0; j < 60; j++) {
                HBox minuteBox = new HBox();
                HBox.setHgrow(minuteBox, Priority.ALWAYS);
                VBox.setVgrow(minuteBox, Priority.ALWAYS);

                minuteBox.getStyleClass().add(currentStyleClass);

                hourBox.getChildren().add(minuteBox);

                remainingDuration--;

                // Move to the next shift sequence if the current one is complete
                if (remainingDuration == 0 && shiftSequenceIndex < shiftSequences.size() - 1) {
                    shiftSequenceIndex++;
                    currentStyleClass = shiftSequences.get(shiftSequenceIndex).styleClass;
                    remainingDuration = shiftSequences.get(shiftSequenceIndex).duration;
                }
            }

            // Add hour box to hour container
            hourBoxContainer.getChildren().add(hourBox);

            // Label for each hour
            Label hourLabel = new Label();
            hourLabel.getStyleClass().add("hourShiftBoxLabel");
            hourLabel.setText(String.format("%02d:00", workingTime.getHour()));

            // Add and position label
            hourBoxContainer.getChildren().add(hourLabel);
            StackPane.setAlignment(hourLabel, Pos.TOP_LEFT);

            // Add container of hour box and label to the day hours HBox
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
        noteButton.getStyleClass().add("chevronButton");

        Image noteImage = new Image(getClass().getResourceAsStream("/icons/add-note-svgrepo-com.png"));

        ImageView noteImageView = new ImageView(noteImage);
        noteImageView.getStyleClass().add("chevronImage");

        noteButton.setGraphic(noteImageView);
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

    private String getMinuteBoxStyleClassFromEventType(String eventType) {
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

    private List<ShiftSequence> generateShiftSequence(JSONArray timelogs) {
        List<ShiftSequence> shiftSequences = new ArrayList<>();

        // Start with the initial "empty" duration before the first event
        LocalDateTime firstEventTime = LocalDateTime.parse(timelogs.getJSONObject(0).getString("event_time"));
        Duration initialEmptyDuration = Duration.between(
                firstEventTime.withHour(weeklyStartHour).withMinute(0).withSecond(0).withNano(0),
                firstEventTime.withSecond(0).withNano(0)
        );
        if (!initialEmptyDuration.isZero()) {
            shiftSequences.add(new ShiftSequence("none", (int) initialEmptyDuration.toMinutes()));
        }

        // Iterate through the timelogs and create shift sequences
        for (int i = 1; i < timelogs.length(); i++) {
            LocalDateTime previousTime = LocalDateTime.parse(timelogs.getJSONObject(i - 1).getString("event_time"));
            LocalDateTime currentTime = LocalDateTime.parse(timelogs.getJSONObject(i).getString("event_time"));
            String currentEditedTime = timelogs.getJSONObject(i - 1).optString("edited_time", null);
            String nextEditedTime = timelogs.getJSONObject(i).optString("edited_time", null);
            String eventType = timelogs.getJSONObject(i - 1).getString("event_type");
            String nextEventType = timelogs.getJSONObject(i).getString("event_type");
            Duration duration = Duration.between(previousTime.withSecond(0).withNano(0), currentTime.withSecond(0).withNano(0));

            if (nextEventType.equals("check_out") && nextEditedTime != null) {
                shiftSequences.add(new ShiftSequence("shiftBoxEdited", (int) duration.toMinutes()));
            }

            if (currentTime.getHour() == 23 && currentTime.getMinute() == 59) {
                shiftSequences.add(new ShiftSequence("shiftBoxMissedEvent", (int) duration.toMinutes()));
            } else {
                if(currentEditedTime != null) {
                    shiftSequences.add(new ShiftSequence("shiftBoxEdited", (int) duration.toMinutes()));
                } else {
                    shiftSequences.add(new ShiftSequence(getMinuteBoxStyleClassFromEventType(eventType), (int) duration.toMinutes()));
                }
            }
        }

        // Handle the final event in the day
        JSONObject lastTimelog = timelogs.getJSONObject(timelogs.length() - 1);
        LocalDateTime lastEventTime = LocalDateTime.parse(lastTimelog.getString("event_time"));
        String lastEventType = lastTimelog.getString("event_type");
        String lastEditedTime = lastTimelog.optString("edited_time", null);

        Duration finalEmptyDuration = Duration.between(
                lastEventTime.withSecond(0).withNano(0),
                lastEventTime.withHour(weeklyEndHour).withMinute(0).withSecond(0).withNano(0)
        );
        if (lastEventType.equals("check_out") && lastEditedTime != null) {
            shiftSequences.add(new ShiftSequence("shiftBoxEdited", (int) finalEmptyDuration.toMinutes()));
        } else if (!finalEmptyDuration.isZero()) {
            shiftSequences.add(new ShiftSequence("none", (int) finalEmptyDuration.toMinutes()));
        }

        return shiftSequences;
    }

    private void calculateWeeklyDayValues(JSONArray weekTimelogs) {
        // Default values of hours
        weeklyStartHour = 7;
        weeklyEndHour = 17;

        for(int i = 0; i < weekTimelogs.length(); i++){
            JSONArray dayTimelogs = weekTimelogs.getJSONArray(i);

            if(dayTimelogs.isEmpty()) continue;

            LocalDateTime startTime = LocalDateTime.parse(dayTimelogs.getJSONObject(0).getString("event_time"));
            LocalDateTime endTime = LocalDateTime.parse(dayTimelogs.getJSONObject(dayTimelogs.length() - 1).getString("event_time"));

            if(startTime.getHour() < weeklyStartHour){
                weeklyStartHour = startTime.getHour();
            }
            if (endTime.getHour() > weeklyEndHour && !(endTime.getHour() == 23 && endTime.getMinute() == 59)){
                weeklyEndHour = endTime.getHour();
            }

            // So empty box with the next hour marker is always shown
            weeklyMaxAmountSingleShiftHours = weeklyEndHour - weeklyStartHour + 1;
        }
    }
}
