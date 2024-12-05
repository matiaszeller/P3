package com.p3.employeeHistory;

import com.p3.session.Session;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    private void initialize() {
        setActionHandlers();
        date = LocalDate.now();
        handleWeekTimelogs(date); // First time will always default to current day, could be stored as session data if this is not preferred
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

    private void loadStage(String fxmlPath) {
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

        contentContainer.getChildren().removeIf(node -> node instanceof VBox); // Clears previous VBox
        weekNumberLabel.setText(String.format("%d | Uge nr: %d", date.getYear(), date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)));

        Duration weekWorkHours = Duration.ZERO;
        calculateWeeklyDayValues(jsonArray);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray dayEvents = jsonArray.getJSONArray(i);
            VBox dayBox = dayEvents.isEmpty() ? createEmptyDayShiftBox(employeeHistoryService.toMonday(date).plusDays(i)) : createDayShiftBox(dayEvents);
            contentContainer.getChildren().add(dayBox);
            weekWorkHours = weekWorkHours.plus(employeeHistoryService.calculateDayWorkHours(dayEvents));
        }

        weekWorkHoursLabel.setText(employeeHistoryService.formatWorkHours(weekWorkHours));
    }

    private VBox createVBox(String styleClass, boolean hgrow, boolean vgrow) {
        VBox vbox = new VBox();
        if (hgrow) HBox.setHgrow(vbox, Priority.ALWAYS);
        if (vgrow) VBox.setVgrow(vbox, Priority.ALWAYS);
        if (styleClass != null) vbox.getStyleClass().add(styleClass);
        return vbox;
    }

    private HBox createHBox(String styleClass, boolean hgrow, boolean vgrow) {
        HBox hbox = new HBox();
        if (hgrow) HBox.setHgrow(hbox, Priority.ALWAYS);
        if (vgrow) VBox.setVgrow(hbox, Priority.ALWAYS);
        if (styleClass != null) hbox.getStyleClass().add(styleClass);
        return hbox;
    }

    private Label createLabel(String styleClass, String text) {
        Label label = new Label();
        if (styleClass != null) label.getStyleClass().add(styleClass);
        if (text != null) label.setText(text);
        return label;
    }

    private StackPane createStackPane(String styleClass, boolean hgrow, boolean vgrow) {
        StackPane stackPane = new StackPane();
        if (hgrow) HBox.setHgrow(stackPane, Priority.ALWAYS);
        if (vgrow) VBox.setVgrow(stackPane, Priority.ALWAYS);
        if (styleClass != null) stackPane.getStyleClass().add(styleClass);
        return stackPane;
    }

    private VBox createEmptyDayShiftBox(LocalDate date) {
        VBox dayVBox = createVBox("dayShiftBox", true, true);
        HBox dayBoxHeader = createHBox("dayShiftBoxHeader", true, false);
        HBox emptyDayBox = createHBox("emptyShiftBox", true, true);
        emptyDayBox.setAlignment(Pos.CENTER);

        Label emptyShiftLabel = createLabel("emptyShiftLabel", "Ingen Vagt");
        emptyDayBox.getChildren().add(emptyShiftLabel);

        String weekDay = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("da", "DK"));
        String weekDayCapitalized = weekDay.substring(0, 1).toUpperCase(Locale.ENGLISH) + weekDay.substring(1);
        Label dayLabel = createLabel(null, weekDayCapitalized + " | " + date);

        dayBoxHeader.getChildren().add(dayLabel);
        dayVBox.getChildren().addAll(dayBoxHeader, emptyDayBox);

        return dayVBox;
    }

    private VBox createDayShiftBox(JSONArray dayTimelogs) {
        JSONObject firstIndexObject = dayTimelogs.getJSONObject(0);
        LocalDateTime firstTime = LocalDateTime.parse(firstIndexObject.getString("event_time"));
        List<String> editedTimelogs = employeeHistoryService.getEditedTimelog(dayTimelogs);

        VBox dayVBox = createVBox("dayShiftBox", true, true);
        HBox dayBoxHeader = createHBox("dayShiftBoxHeader", true, false);

        String weekDay = LocalDate.parse(firstIndexObject.getString("shift_date"))
                .getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, new Locale("da", "DK"));
        String weekDayCapitalized = weekDay.substring(0, 1).toUpperCase(Locale.ENGLISH) + weekDay.substring(1);
        Label dayLabel = createLabel(null, weekDayCapitalized + " | " + LocalDate.parse(firstIndexObject.getString("shift_date")));
        dayBoxHeader.getChildren().add(dayLabel);
        dayVBox.getChildren().add(dayBoxHeader);

        HBox dayHoursHBox = createHBox("dayHoursHBox", true, true);
        int shiftSequenceIndex = 0;
        List<ShiftSequence> shiftSequences = generateShiftSequence(dayTimelogs);
        String currentStyleClass = shiftSequences.get(shiftSequenceIndex).styleClass;
        int remainingDuration = shiftSequences.get(shiftSequenceIndex).duration;


        for (int i = 0; i < weeklyMaxAmountSingleShiftHours; i++) {
            LocalDateTime workingTime = firstTime.withHour(weeklyStartHour).plusHours(i);

            if (workingTime.getHour() >= weeklyEndHour) {
                break;
            }

            HBox hourBox = createHBox("hourShiftBox", true, true);
            StackPane hourBoxContainer = createStackPane(null, true, true);

            for (int j = 0; j < 60; j++) {
                HBox minuteBox = createHBox(currentStyleClass, true, true);
                hourBox.getChildren().add(minuteBox);
                remainingDuration--;


                switch (shiftSequences.get(shiftSequenceIndex).getSequenceType()) {
                    case "check_in" -> {
                        if (remainingDuration == shiftSequences.get(shiftSequenceIndex).getDuration() - 1) {
                            createSequenceLabel(hourBoxContainer, shiftSequences.get(shiftSequenceIndex), editedTimelogs);
                        }
                    }
                    case "break_end" -> {
                        if (remainingDuration == 2) {
                            createSequenceLabel(hourBoxContainer, shiftSequences.get(shiftSequenceIndex), editedTimelogs);
                        }
                    }
                    case "break_start" ->
                            createSequenceLabel(hourBoxContainer, shiftSequences.get(shiftSequenceIndex), editedTimelogs);
                    case "check_out" -> {
                        if(shiftSequences.get(shiftSequenceIndex).getEdited() && remainingDuration == 2){
                            createSequenceLabel(hourBoxContainer, shiftSequences.get(shiftSequenceIndex), editedTimelogs);
                        }
                    }
                }

                if (remainingDuration == 0 && shiftSequenceIndex < shiftSequences.size() - 1) {
                    shiftSequenceIndex++;
                    currentStyleClass = shiftSequences.get(shiftSequenceIndex).styleClass;
                    remainingDuration = shiftSequences.get(shiftSequenceIndex).duration;
                }
            }
            hourBoxContainer.getChildren().add(hourBox);

            Label hourLabel = createLabel("hourShiftBoxLabel", String.format("%02d:00", workingTime.getHour()));
            hourBoxContainer.getChildren().add(hourLabel);
            StackPane.setAlignment(hourLabel, Pos.TOP_LEFT);

            // If no breaks during day, TODO LAV SOM DEL AF MAIN SEQUENCELABEL METHOD
            if(shiftSequences.size() == 3
                    && shiftSequences.get(shiftSequenceIndex).getSequenceType().equals("empty")
                    && shiftSequences.get(shiftSequenceIndex).getDuration() == remainingDuration) {
                edgeCreateNoBreakEndDaySequenceLabel(hourBoxContainer, shiftSequences.get(1));
            }

            ObservableList<Node> children = hourBoxContainer.getChildren();
            Node targetNode = null;
            for(Node child : children) {
                if ("shiftSequenceLabel".equals(child.getId())){
                    targetNode = child;
                    break;
                }
            }

            if (targetNode != null) {
                children.remove(targetNode);
                children.add(targetNode);
            }

            dayHoursHBox.getChildren().add(hourBoxContainer);
        }

        dayVBox.getChildren().add(dayHoursHBox);

        HBox noteBox = createHBox("noteHBox", true, true);
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


    public void fetchWeekHistory(int weeksToAdd) {
        date = date.plusWeeks(weeksToAdd);
        handleWeekTimelogs(date);
    }

    private List<ShiftSequence> generateShiftSequence(JSONArray timelogs) {
        List<ShiftSequence> shiftSequences = new ArrayList<>();

        LocalDateTime firstEventTime = LocalDateTime.parse(timelogs.getJSONObject(0).getString("event_time"));
        addInitialEmptyDuration(shiftSequences, firstEventTime);

        for (int i = 1; i < timelogs.length(); i++) {
            LocalDateTime previousTime = LocalDateTime.parse(timelogs.getJSONObject(i - 1).getString("event_time"));
            LocalDateTime currentTime = LocalDateTime.parse(timelogs.getJSONObject(i).getString("event_time"));
            String currentEditedTime = timelogs.getJSONObject(i - 1).optString("edited_time", null);
            String eventType = timelogs.getJSONObject(i - 1).getString("event_type");
            Duration duration = Duration.between(previousTime.withSecond(0).withNano(0), currentTime.withSecond(0).withNano(0));

            addShiftSequence(shiftSequences, previousTime, currentTime, currentEditedTime, eventType, duration);
        }

        addFinalEmptyDuration(shiftSequences, timelogs);
        return shiftSequences;
    }

    private void addInitialEmptyDuration(List<ShiftSequence> shiftSequences, LocalDateTime firstEventTime) {
        Duration initialEmptyDuration = Duration.between(
                firstEventTime.withHour(weeklyStartHour).withMinute(0).withSecond(0).withNano(0),
                firstEventTime.withSecond(0).withNano(0)
        );
        if (!initialEmptyDuration.isZero()) {
            shiftSequences.add(new ShiftSequence("none", (int) initialEmptyDuration.toMinutes(), firstEventTime.withHour(weeklyStartHour).withMinute(0).withSecond(0).withNano(0), firstEventTime, "empty", false));
        }
    }

    private void addShiftSequence(List<ShiftSequence> shiftSequences, LocalDateTime previousTime, LocalDateTime currentTime, String currentEditedTime, String eventType, Duration duration) {
        if (currentTime.getHour() == 23 && currentTime.getMinute() == 59) {
            shiftSequences.add(new ShiftSequence("shiftBoxMissedEvent", (int) duration.toMinutes(), previousTime, currentTime, eventType, false));
        } else {
            String styleClass = (currentEditedTime != null) ? "shiftBoxEdited" : employeeHistoryService.getMinuteBoxStyleClassFromEventType(eventType);
            boolean isEdited = currentEditedTime != null;
            shiftSequences.add(new ShiftSequence(styleClass, (int) duration.toMinutes(), previousTime, currentTime, eventType, isEdited));
        }
    }

    private void addFinalEmptyDuration(List<ShiftSequence> shiftSequences, JSONArray timelogs) {
        JSONObject lastTimelog = timelogs.getJSONObject(timelogs.length() - 1);
        LocalDateTime lastEventTime = LocalDateTime.parse(lastTimelog.getString("event_time"));
        String lastEventType = lastTimelog.getString("event_type");
        String lastEditedTime = lastTimelog.optString("edited_time", null);

        LocalDateTime endOfDay = lastEventTime.withHour(weeklyEndHour).withMinute(0).withSecond(0).withNano(0);
        Duration finalEmptyDuration = Duration.between(lastEventTime.withSecond(0).withNano(0), endOfDay);

        if (lastEventTime.isBefore(endOfDay)) {
            if (lastEventType.equals("check_out") && lastEditedTime != null) {
                shiftSequences.get(shiftSequences.size() - 1).setStyleClass("shiftBoxEdited");
                shiftSequences.add(new ShiftSequence("shiftBoxEdited", (int) finalEmptyDuration.toMinutes(), lastEventTime, endOfDay, "check_out", true));
            } else if (!finalEmptyDuration.isZero()) {
                shiftSequences.add(new ShiftSequence("none", (int) finalEmptyDuration.toMinutes(), lastEventTime, endOfDay, "empty", false));
            }
        }
    }

    private void calculateWeeklyDayValues(JSONArray weekTimelogs) {
        weeklyStartHour = 7;
        weeklyEndHour = 17;

        for (int i = 0; i < weekTimelogs.length(); i++) {
            JSONArray dayTimelogs = weekTimelogs.getJSONArray(i);

            if (dayTimelogs.isEmpty()) continue;

            LocalDateTime startTime = LocalDateTime.parse(dayTimelogs.getJSONObject(0).getString("event_time"));
            LocalDateTime endTime = LocalDateTime.parse(dayTimelogs.getJSONObject(dayTimelogs.length() - 1).getString("event_time"));

            if (startTime.getHour() < weeklyStartHour) {
                weeklyStartHour = startTime.getHour();
            }
            if (endTime.getHour() > weeklyEndHour && !(endTime.getHour() == 23 && endTime.getMinute() == 59)) {
                weeklyEndHour = endTime.getHour();
            }

            weeklyMaxAmountSingleShiftHours = weeklyEndHour - weeklyStartHour + 1;
        }
    }

    private void createSequenceLabel(StackPane hourBoxContainer, ShiftSequence shiftSequence, List<String> editedTimelogs) {
        Label label = createLabel("shiftSequenceLabel", employeeHistoryService.setStringForShiftSequenceLabel(shiftSequence, editedTimelogs));
        label.setId("shiftSequenceLabel");
        label.setWrapText(true);
        hourBoxContainer.getChildren().add(label);
        hourBoxContainer.setPadding(new Insets(0, 0, 0, 0));
        hourBoxContainer.setMaxWidth(120);
        label.setPadding(new Insets(0, 0, 0, 0));
        StackPane.setAlignment(label, Pos.CENTER);
    }

    private void edgeCreateNoBreakEndDaySequenceLabel(StackPane hourBoxContainer, ShiftSequence shiftSequence) {
        Label label = createLabel("shiftSequenceLabel", String.format("Slut\n%02d:%02d", shiftSequence.getEndTime().getHour(), shiftSequence.getEndTime().getMinute()));
        label.setId("shiftSequenceLabel");
        label.setWrapText(true);
        hourBoxContainer.getChildren().add(label);
        hourBoxContainer.setPadding(new Insets(0, 0, 0, 0));
        hourBoxContainer.setMaxWidth(120);
        label.setPadding(new Insets(0, 0, 0, 0));
        StackPane.setAlignment(label, Pos.CENTER);
    }

}
