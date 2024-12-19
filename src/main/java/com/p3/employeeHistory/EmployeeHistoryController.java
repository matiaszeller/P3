package com.p3.employeeHistory;

import com.p3.session.Session;

import com.p3.util.ModalUtil;
import com.p3.noteModal.NoteModalController;
import com.p3.util.StageLoader;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeHistoryController {

    private final EmployeeHistoryService employeeHistoryService = new EmployeeHistoryService();
    private final StageLoader stageLoader = new StageLoader();
    private int weeklyMaxAmountSingleShiftHours;
    private LocalDate date;
    private int weeklyStartHour;
    private int weeklyEndHour;
    private Button selectedDateButton = null;
    private LocalDate selectedDate = null;
    private LocalDate weeklyDate;
    private Stage stage;

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
    private GridPane calendarGrid;
    @FXML
    private Label yearMonthLabel;
    @FXML
    private Button prevMonthButton;
    @FXML
    private Button nextMonthButton;

    @FXML
    private void initialize() {
        setActionHandlers();
        date = LocalDate.now();
        selectedDate = date;
        weeklyDate = date;

        generateCalendar(YearMonth.of(date.getYear(), date.getMonthValue()));
        handleWeekTimelogs(date); // First time will always default to current day, could be stored as session data if this is not preferred
    }

    private void setActionHandlers() {
        prevWeekButton.setOnAction(event -> fetchWeekHistory(-1));
        nextWeekButton.setOnAction(event -> fetchWeekHistory(1));
        prevMonthButton.setOnAction(event -> changeMonth(-1));
        nextMonthButton.setOnAction(event -> changeMonth(1));

        Platform.runLater(() -> {
            stage = (Stage) logOutButton.getScene().getWindow();

            goBackButton.setOnAction(event -> {
                try {
                    stageLoader.loadStage("/com.p3.menu/MenuPage.fxml", stage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            logOutButton.setOnAction(event -> {
                Session.clearSession();
                try {
                    stageLoader.loadStage("/com.p3.login/LoginPage.fxml", stage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        });
    }

    private void handleWeekTimelogs(LocalDate date) {
        weeklyDate = date;
        JSONArray jsonArrayTimelogs = new JSONArray(employeeHistoryService.getWeekTimelogs(date, Session.getCurrentUserId()));
        JSONArray jsonArrayNotes = new JSONArray(employeeHistoryService.getWeekNotes(date, Session.getCurrentUserId()));

        contentContainer.getChildren().removeIf(node -> node instanceof VBox); // Clears previous VBox
        weekNumberLabel.setText(String.format("%d | Uge nr: %d", date.getYear(), date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)));

        Duration weekWorkHours = Duration.ZERO;
        calculateWeeklyDayValues(jsonArrayTimelogs);

        // Timelogs and notes should (is) always same length as server searches and returns for week. Empty lists are included in main list
        for (int i = 0; i < jsonArrayTimelogs.length(); i++) {
            JSONArray dayEvents = jsonArrayTimelogs.getJSONArray(i);
            JSONArray dayNotes = jsonArrayNotes.getJSONArray(i);

            VBox dayBox = dayEvents.isEmpty() ?
                    createEmptyDayShiftBox(employeeHistoryService.toMonday(date).plusDays(i)) :
                    createDayShiftBox(dayEvents, dayNotes);

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
        // test
        return dayVBox;
    }

    private VBox createDayShiftBox(JSONArray dayTimelogs, JSONArray dayNotes) {
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
        boolean isLastEvent = false;

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

                if(shiftSequenceIndex == shiftSequences.size() - 2) {
                    isLastEvent = true;
                }


                switch (shiftSequences.get(shiftSequenceIndex).getSequenceType()) {
                    case "check_in" -> {
                        if (remainingDuration == shiftSequences.get(shiftSequenceIndex).getDuration() - 1) {
                            createSequenceLabel(hourBoxContainer, shiftSequences.get(shiftSequenceIndex), editedTimelogs, isLastEvent);
                        }
                    }
                    case "break_end" -> {
                        if (remainingDuration == 2) {
                            createSequenceLabel(hourBoxContainer, shiftSequences.get(shiftSequenceIndex), editedTimelogs, isLastEvent);
                        }
                    }
                    case "break_start" ->
                            createSequenceLabel(hourBoxContainer, shiftSequences.get(shiftSequenceIndex), editedTimelogs, isLastEvent);
                    case "check_out" -> {
                        if(shiftSequences.get(shiftSequenceIndex).getEdited() && remainingDuration == 2){
                            createSequenceLabel(hourBoxContainer, shiftSequences.get(shiftSequenceIndex), editedTimelogs, isLastEvent);
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

            // If no breaks during day
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


        HBox noteBox = createHBox("noteHBox", true, false);
        noteBox.setAlignment(Pos.BOTTOM_RIGHT);

        // Get the last note if available, otherwise it will be null (JAVAFX IS FUCKED SO THIS FIXES GROW ISSUES)
        JSONObject lastNote = (!dayNotes.isEmpty()) ? dayNotes.getJSONObject(dayNotes.length() - 1) : null;

        VBox lastNoteBox = createVBox("lastNoteBox", true, false);
        lastNoteBox.getStyleClass().add("lastNoteBox");

        Label lastNoteSenderLabel = new Label();
        lastNoteSenderLabel.getStyleClass().add("noteSenderLabel");

        if (lastNote != null) {
            lastNoteSenderLabel.setText(String.format("%s:", lastNote.getString("full_name")));
        } else {
            lastNoteSenderLabel.setText("");
        }
        lastNoteBox.getChildren().add(lastNoteSenderLabel);

        Label lastNoteTextLabel = new Label();
        lastNoteTextLabel.getStyleClass().add("lastNoteTextLabel");

        if (lastNote != null) {
            lastNoteTextLabel.setText(String.format("%s", lastNote.getString("written_note")));
        } else {
            lastNoteTextLabel.setText("");
        }
        lastNoteBox.getChildren().add(lastNoteTextLabel);

        noteBox.getChildren().add(lastNoteBox);

        Button noteButton = new Button();
        noteButton.getStyleClass().add("chevronButton");
        Image noteImage = new Image(getClass().getResourceAsStream("/icons/add-note-svgrepo-com.png"));
        ImageView noteImageView = new ImageView(noteImage);
        noteImageView.getStyleClass().add("chevronImage");
        noteButton.setGraphic(noteImageView);


        noteBox.getChildren().add(noteButton);
        dayHoursHBox.getChildren().add(noteBox);

        // On buttonclick, show modal.
        noteButton.setOnAction(event -> {
            ModalUtil.ModalResult<NoteModalController> modalResult = ModalUtil.showModal("/com.p3.global/NoteHistoryModal.fxml", stage, "Noter");
            if(modalResult != null){
                NoteModalController controller = modalResult.getController();
                controller.generateModal(dayNotes, firstTime.toLocalDate(), Session.getCurrentUserId());

                Stage modalStage = modalResult.getStage();
                modalStage.showAndWait();
            }
        });

        return dayVBox;
    }


    public void fetchWeekHistory(int weeksToAdd) {
        if(weeklyDate.getMonthValue() != date.getMonthValue()){
            date = weeklyDate;
        }
        date = date.plusWeeks(weeksToAdd);
        handleWeekTimelogs(date);
        generateCalendar(YearMonth.of(date.getYear(), date.getMonthValue()));
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
            if (endTime.getHour() >= weeklyEndHour && !(endTime.getHour() == 23 && endTime.getMinute() == 59)) {
                weeklyEndHour = endTime.getHour() + 1;
            }

            weeklyMaxAmountSingleShiftHours = weeklyEndHour - weeklyStartHour + 1;
        }

        if(weeklyEndHour > 23){
            weeklyEndHour = 23;
        }
        if (weeklyStartHour < 1) {
            weeklyStartHour = 1;
        }
    }

    private void createSequenceLabel(StackPane hourBoxContainer, ShiftSequence shiftSequence, List<String> editedTimelogs, boolean isLastEvent) {
        Label label = createLabel("shiftSequenceLabel", employeeHistoryService.setStringForShiftSequenceLabel(shiftSequence, editedTimelogs, isLastEvent));
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


    // Har lige lavet min egen implementation, synes der manglede lidt :)) - Sakarias
    public void generateCalendar(YearMonth yearMonth) {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();


        yearMonthLabel.setText(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + yearMonth.getYear());

        // Add day-of-week headers
        String[] daysOfWeek = {"Man", "Tir", "Ons", "Tor", "Fre", "Lør", "Søn"};
        for (int col = 0; col < daysOfWeek.length; col++) {
            Label dayLabel = new Label(daysOfWeek[col]);
            calendarGrid.add(dayLabel, col, 0);
        }

        // Get the first day of the month and the total number of days
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Determine the starting day of the week (1=Monday, 2=Tuesday, ..., 7=Sunday)
        int startDayOfWeek = (firstDayOfMonth.getDayOfWeek().getValue() - 1) % 7;

        // Get the previous month
        YearMonth previousMonth = yearMonth.minusMonths(1);
        int daysInPreviousMonth = previousMonth.lengthOfMonth();

        // Set grid constraints to ensure equal column widths
        for (int col = 0; col < 7; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(14.2857); // 100% / 7 columns = 14.2857% :DD
            calendarGrid.getColumnConstraints().add(colConstraints);
        }

        // Adds buttons for days from last month until row is filled combined with days from current month
        // Buttons are disabled :D
        int row = 1;
        int col = 0;
        for (int i = daysInPreviousMonth - startDayOfWeek + 1; i <= daysInPreviousMonth; i++) {
            Button prevMonthButton = new Button(String.valueOf(i));
            prevMonthButton.setMinSize(40, 40);
            prevMonthButton.getStyleClass().add("notCurrentMonthDate");
            prevMonthButton.setDisable(true);
            calendarGrid.add(prevMonthButton, col++, row);
        }

        // Add buttons for the current month
        for (int day = 1; day <= daysInMonth; day++) {
            Button dateButton = new Button(String.valueOf(day));
            dateButton.setOnAction(this::handleDateClick);
            dateButton.setMinSize(40, 40);
            dateButton.getStyleClass().add("dateButton");

            // If this is the selected date, mark it
            if (selectedDate != null && selectedDate.equals(LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), day))) {
                selectedDateButton = dateButton;
                selectedDateButton.getStyleClass().add("selectedDateButton");
            }

            calendarGrid.add(dateButton, col, row);

            col++;
            if (col == 7) { // Move to the next row after Sunday
                col = 0;
                row++;
            }
        }

        // Add remaining days if row columns is not filled
        int nextMonthDay = 1;
        while (col < 7) {
            Button nextMonthButton = new Button(String.valueOf(nextMonthDay++));
            nextMonthButton.setMinSize(40, 40);
            nextMonthButton.getStyleClass().add("notCurrentMonthDate");
            nextMonthButton.setDisable(true);
            calendarGrid.add(nextMonthButton, col++, row);
        }

        for (int r = 0; r <= row; r++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(rowConstraints);
        }
    }


    private void handleDateClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        if(selectedDateButton != null) {
            selectedDateButton.getStyleClass().remove("selectedDateButton");
        }
        selectedDateButton = clickedButton;
        selectedDateButton.getStyleClass().add("selectedDateButton");

        String day = clickedButton.getText();
        int dayOfMonth = Integer.parseInt(day.trim());
        setDate(date.withDayOfMonth(dayOfMonth));
        selectedDate = date.withDayOfMonth(dayOfMonth);

        handleWeekTimelogs(date);
    }

    private void setDate(LocalDate date){
        this.date = date;
    }

    private void changeMonth(int offset) {
        setDate(date.plusMonths(offset));
        generateCalendar(YearMonth.of(date.getYear(), date.getMonth()));

    }
}
