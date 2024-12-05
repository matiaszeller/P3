package com.p3.managerDaily;

import com.p3.menu.MenuService;
import com.p3.overview.WeeklyOverviewService;
import com.p3.session.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javafx.event.ActionEvent;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.shape.Rectangle;

public class ManagerDailyController {

    private static final ManagerDailyService service = new ManagerDailyService();

    private Map<LocalDate, TitledPane> dayPaneMap = new HashMap<>();

    private Map<LocalDate, Map<Integer, VBox>> timelogBoxMap = new HashMap<>();

    private ScrollPane scrollPane;

    private LocalDate lastLoadedDate = null;

    @FXML
    private VBox managerDailyRoot;
    @FXML
    private VBox centerPanel;

    @FXML
    private Button BackButton;

    @FXML
    private Button managerLogOutButton;

    @FXML
    private Button dailyOverviewButton;

    @FXML
    private Button weeklyOverviewButton;

    @FXML
    private Button editEmployeesButton;

    @FXML
    private Button exportDataButton;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label yearMonthLabel;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Button nextMonthButton;

    @FXML
    private BorderPane BorderPaneOuter;

    private YearMonth YearMonth;

    private YearMonth currentMonth;

    private LocalDateTime originalTime = null;

    private VBox timelogBox = new VBox();

    @FXML
    public void initialize() {
        resetVariables();
        managerLogOutButton.setOnAction(event -> handleLogOut());
        weeklyOverviewButton.setOnAction(event -> handleWeeklyOverview());
        editEmployeesButton.setOnAction(event -> handleEditEmployees());
        exportDataButton.setOnAction(event -> handleExportData());
        BackButton.setOnAction(event -> handleBackButton());
        handleDailyOverview();
        currentMonth = YearMonth.now();
        generateCalendar(currentMonth);
        generateTimelogBoxes(LocalDate.now(), 30);

        // Add navigation button actions
        prevMonthButton.setOnAction(event -> changeMonth(-1));
        nextMonthButton.setOnAction(event -> changeMonth(1));

    }

    private void changeMonth(int offset) {
        currentMonth = currentMonth.plusMonths(offset);
        generateCalendar(currentMonth);

    }

    private void resetVariables() {
        dayPaneMap.clear();
        lastLoadedDate = null;
    }

    public void generateCalendar(YearMonth yearMonth) {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        yearMonthLabel.setText(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + yearMonth.getYear());

        // Add day-of-week headers
        String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int col = 0; col < daysOfWeek.length; col++) {
            Label dayLabel = new Label(daysOfWeek[col]);
            calendarGrid.add(dayLabel, col, 0);
        }

        // Get the first day of the month and the total number of days
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Determine the starting day of the week (1=Monday, 2=Tuesday, ..., 7=Sunday)
        int startDayOfWeek = (firstDayOfMonth.getDayOfWeek().getValue() - 1) % 7;

        // Set grid constraints to ensure equal column widths
        for (int col = 0; col < 7; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(14.2857); // 100% / 7 columns = 14.2857% :DD
            calendarGrid.getColumnConstraints().add(colConstraints);
        }

        // Populate the calendar with buttons for each date
        int row = 1;
        int col = startDayOfWeek;
        for (int day = 1; day <= daysInMonth; day++) {
            Button dateButton = new Button(String.valueOf(day));
            dateButton.setOnAction(this::handleDateClick);
            dateButton.setMinSize(40, 40);
            dateButton.getStyleClass().add("dateButton");

            calendarGrid.add(dateButton, col, row);

            col++;
            if (col == 7) { // Move to the next row after Sunday
                col = 0;
                row++;
            }
        }

        for (int r = 0; r < row; r++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(rowConstraints);
        }
    }

    public void generateTimelogBoxes(LocalDate startDate, int daysCount) {
        centerPanel.getChildren().clear();

        if (scrollPane == null) {
            scrollPane = new ScrollPane(); // Initialize only if it's not already initialized
        }
        scrollPane.setContent(centerPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane root = BorderPaneOuter;
        root.setCenter(scrollPane);

        ManagerDailyService.loadTimelogsForRange(startDate, daysCount);
        List<Map<String, Object>> timelogs = ManagerDailyService.getTimelogs();

        for (int i = 0; i < daysCount; i++) {

            LocalDate currentDate = startDate.minusDays(i);
            generateTimelogBox(currentDate, timelogs);
        }

        String cssPath = getClass().getResource("/style.css").toExternalForm();
        root.getStylesheets().add(cssPath);
    }

    public void generateTimelogBox(LocalDate date, List<Map<String, Object>> timelogs) {
        // Group timelogs by user_id
        Map<Integer, List<Map<String, Object>>> timelogsByUserId = timelogs.stream()
                .filter(timelog -> {
                    Object shiftDate = timelog.get("shift_date");
                    if (shiftDate instanceof String) {
                        try {
                            LocalDate shiftDateParsed = LocalDate.parse((String) shiftDate);
                            return shiftDateParsed.equals(date);
                        } catch (Exception e) {
                            System.err.println("Error parsing shift date: " + shiftDate);
                        }
                    } else if (shiftDate instanceof LocalDate) {
                        return shiftDate.equals(date);
                    }
                    return false;
                })
                .collect(Collectors.groupingBy(timelog -> (Integer) timelog.getOrDefault("user_id", 0)));

        timelogBoxMap.putIfAbsent(date, new HashMap<>());
        Map<Integer, VBox> userTimelogBoxes = timelogBoxMap.get(date);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);

        TitledPane dayPane = new TitledPane();
        dayPane.setText(formattedDate);
        dayPane.getStyleClass().add("collapseDateBox");
        dayPane.setExpanded(false);
        VBox employeeRows = new VBox();
        employeeRows.setSpacing(15);
        // Debug: Check if the dayPaneMap already contains the date
        for (Map.Entry<Integer, List<Map<String, Object>>> entry : timelogsByUserId.entrySet()) {
            Integer userId = entry.getKey();
            List<Map<String, Object>> userTimelogs = entry.getValue();
            String fullName = service.getUserFullName(userId);

            HBox employeeRow = new HBox();
            employeeRow.getStyleClass().add("managerDailyEmployeeBox");
            employeeRow.setSpacing(20);
            employeeRow.setAlignment(Pos.CENTER_LEFT);

            VBox nameBox = new VBox();
            nameBox.setAlignment(Pos.CENTER);
            nameBox.setMinSize(150, 150);
            Text fullNameText = new Text(fullName);
            fullNameText.getStyleClass().add("managerDailyFullName");
            nameBox.getChildren().add(fullNameText);

            timelogBox.setSpacing(2);
            timelogBox.setAlignment(Pos.CENTER_LEFT);
            timelogBox.setMaxWidth(Double.POSITIVE_INFINITY);
            
            // Get start and end times for the user's shift
            int startTime = service.getEarliestTime(date);
            int endTime = service.getLatestTime(date);

            userTimelogs.sort(Comparator.comparing(timelog -> {
                Object eventTimeObj = timelog.get("event_time");
                if (eventTimeObj instanceof String) {
                    try {
                        return LocalDateTime.parse((String) eventTimeObj, DateTimeFormatter.ISO_DATE_TIME);
                    } catch (Exception e) {
                        System.err.println("Error parsing event time: " + eventTimeObj);
                    }
                } else if (eventTimeObj instanceof LocalDateTime) {
                    return (LocalDateTime) eventTimeObj;
                }
                return LocalDateTime.MIN;
            }));

            HBox hourlyBoxes = new HBox();

            // Tracks the current color
            String currentColor = "#F9F6EE";
            int index = 0;

            while (index < userTimelogs.size()) {
                Map<String, Object> currentTimelog = userTimelogs.get(index);
                Map<String, Object> nextTimelog = (index + 1 < userTimelogs.size()) ? userTimelogs.get(index + 1) : null;

                Object editedTimeObj = currentTimelog.get("edited_time");
                LocalDateTime editedTime = null;
                if (editedTimeObj instanceof String) {
                    editedTime = LocalDateTime.parse((String) editedTimeObj, DateTimeFormatter.ISO_DATE_TIME);
                } else if (editedTimeObj instanceof LocalDateTime) {
                    editedTime = (LocalDateTime) editedTimeObj;
                }

                if (editedTime != null) {
                    startTime = editedTime.getHour();
                }

                StackPane lastHourBox = null;
                Label deferredCheckOutLabel = null;
                Label missingCheckOutLabel = null;
                boolean editedOnce = false;
                originalTime = null;
                Set<String> addedLabels = new HashSet<>();
                // Iterate through hours in the time range
                for (int hour = startTime; hour < endTime; hour++) {
                    StackPane hourBox = new StackPane();
                    hourBox.getStyleClass().add("hourBox");

                    Label timeLabel = new Label(hour + ":00");
                    timeLabel.getStyleClass().add("hourBoxTimeLabel");
                    StackPane.setAlignment(timeLabel, Pos.TOP_LEFT);

                    VBox eventLabels = new VBox();
                    eventLabels.setSpacing(3);
                    eventLabels.setAlignment(Pos.CENTER);

                    HBox minuteBoxes = new HBox();
                    minuteBoxes.setSpacing(0);
                    minuteBoxes.setAlignment(Pos.CENTER_LEFT);
                    minuteBoxes.setPrefWidth(150);
                    String[] minuteColors = new String[60];
                    Arrays.fill(minuteColors, currentColor);

                    while (currentTimelog != null) {
                        Object eventTimeObj = currentTimelog.get("event_time");
                        LocalDateTime eventTime = null;
                        LocalDateTime originalTime = null;

                        if (eventTimeObj instanceof String) {
                            eventTime = LocalDateTime.parse((String) eventTimeObj, DateTimeFormatter.ISO_DATE_TIME);
                        } else if (eventTimeObj instanceof LocalDateTime) {
                            eventTime = (LocalDateTime) eventTimeObj;
                        }
                        Object eventTypeObj = currentTimelog.get("event_type");
                        String eventType = eventTypeObj instanceof String ? (String) eventTypeObj : null;
                        if (editedTime != null && !editedOnce) {
                            originalTime = eventTime;
                            eventTime = editedTime;
                            eventType = "edited_" + eventType;
                            editedOnce = true;
                        }

                        if (eventTime != null && eventTime.getHour() == hour) {
                            if (nextTimelog != null) {
                                Object nextEventTypeObj = nextTimelog.get("event_type");
                                String nextEventType = nextEventTypeObj instanceof String ? (String) nextEventTypeObj : null;

                                Object nextEventTimeObj = nextTimelog.get("event_time");
                                LocalDateTime nextEventTime = null;

                                if (nextEventTimeObj instanceof String) {
                                    nextEventTime = LocalDateTime.parse((String) nextEventTimeObj, DateTimeFormatter.ISO_DATE_TIME);
                                } else if (nextEventTimeObj instanceof LocalDateTime) {
                                    nextEventTime = (LocalDateTime) nextEventTimeObj;
                                }
                                if ("check_out".equals(nextEventType) && nextEventTime != null) {
                                    String customMessage = getEventLabelForEventType(nextEventType);
                                    String displayMessage = customMessage + String.format("%02d:%02d", nextEventTime.getHour(), nextEventTime.getMinute());

                                    deferredCheckOutLabel = new Label(displayMessage);
                                    deferredCheckOutLabel.getStyleClass().add("eventTypeLabel");
                                    int nextHour = hour + 2;
                                    // Check for the condition where nextTimelog is a 23:59 check_out
                                    if (nextEventTime.getHour() == 23 && nextEventTime.getMinute() == 59) {
                                        eventType = "missing_check_out";
                                        String missingCheckOutCustomMessage = getEventLabelForEventType("missing_check_out");

                                        missingCheckOutLabel = new Label(missingCheckOutCustomMessage);
                                        missingCheckOutLabel.getStyleClass().add("eventTypeLabel");
                                    } else if (nextEventTime.getHour() == nextHour && nextEventTime.getMinute() > 0) {
                                        endTime = endTime + 1;
                                    }
                                }
                            }

                            if (eventType != null) {
                                String customMessage = getEventLabelForEventType(eventType);
                                String displayMessage;
                                    String formattedTime = String.format("%02d:%02d", eventTime.getHour(), eventTime.getMinute());
                                    displayMessage = customMessage + formattedTime;

                                if (originalTime != null && !originalTime.equals(eventTime)) {
                                    String formattedOriginalTime = String.format("%02d:%02d", originalTime.getHour(), originalTime.getMinute());
                                    displayMessage += "\nOriginal: " + formattedOriginalTime;
                                }

                                if ("missing_check_out".equals(eventType)) {
                                    displayMessage = "-\n" + formattedTime;
                                }

                                Label eventLabel = new Label(displayMessage);
                                eventLabel.getStyleClass().add("eventTypeLabel");
                                addUniqueLabel(eventLabels, eventLabel, addedLabels);

                                currentColor = getColorForEventType(eventType);
                                int startMinute = eventTime.getMinute();
                                int endMinute = (nextTimelog != null
                                        && LocalDateTime.parse((String) nextTimelog.get("event_time"), DateTimeFormatter.ISO_DATE_TIME).getHour() == hour)
                                        ? LocalDateTime.parse((String) nextTimelog.get("event_time"), DateTimeFormatter.ISO_DATE_TIME).getMinute() - 1
                                        : 59;

                                for (int minute = startMinute; minute <= endMinute; minute++) {
                                    minuteColors[minute] = currentColor;
                                }
                            }
                        }
                        // Move to the next timelog
                        if (nextTimelog != null && eventTime != null && eventTime.getHour() == hour) {
                            index++;
                            currentTimelog = nextTimelog;
                            nextTimelog = (index + 1 < userTimelogs.size()) ? userTimelogs.get(index + 1) : null;
                        } else {
                            break;
                        }
                    }

                    for (int minute = 0; minute < 60; minute++) {
                        StackPane minuteBox = new StackPane();
                        minuteBox.setPrefHeight(150);
                        HBox.setHgrow(minuteBox, Priority.ALWAYS);

                        minuteBox.getStyleClass().add("minuteBox");
                        minuteBox.setStyle("-fx-background-color: " + minuteColors[minute] + ";");
                        minuteBoxes.getChildren().add(minuteBox);
                    }

                    hourBox.getChildren().add(minuteBoxes);
                    hourBox.getChildren().add(timeLabel);
                    hourBox.getChildren().add(eventLabels);

                    hourlyBoxes.getChildren().add(hourBox);
                    lastHourBox = hourBox; // Update last hour box reference for eventLabels below
                }

                    // Add the deferredCheckOutLabel and missingCheckOutLabel to the last hour box
                if (lastHourBox != null) {
                    VBox eventLabelsForLastHour = new VBox();
                    eventLabelsForLastHour.setSpacing(3);
                    eventLabelsForLastHour.setAlignment(Pos.CENTER);

                    addUniqueLabel(eventLabelsForLastHour, deferredCheckOutLabel, addedLabels);
                    addUniqueLabel(eventLabelsForLastHour, missingCheckOutLabel, addedLabels);

                    lastHourBox.getChildren().add(eventLabelsForLastHour);
                }
                // Increment index to move to the next timelog
                index++;
            }
            timelogBox.getChildren().add(hourlyBoxes);
            VBox buttonBox = new VBox();
            buttonBox.setSpacing(10);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setMinSize(120, 150);

            // Edit Button
            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> showEditModal(userId));
            editButton.getStyleClass().add("managerDailyButtons");
            // Note Button
            Button noteButton = new Button("Note");
            noteButton.setOnAction(e -> showNoteModal(userId));
            noteButton.getStyleClass().add("managerDailyButtons");
            buttonBox.getChildren().addAll(editButton, noteButton);

            // Add all the parts to the employeeRow
            employeeRow.getChildren().addAll(nameBox, timelogBox, buttonBox);
            employeeRows.getChildren().add(employeeRow);
        }

        HBox.setHgrow(employeeRows, Priority.ALWAYS);
        employeeRows.prefWidthProperty().bind(dayPane.widthProperty());

        dayPaneMap.put(date, dayPane);
        lastLoadedDate = date;
        dayPane.setContent(employeeRows);
        centerPanel.getChildren().add(dayPane);
    }

    // Helper method to determine color based on event type
    public String getColorForEventType(String eventType) {
        if (eventType == null) {
            return "#F9F6EE"; // Default white
        }
        switch (eventType) {
            case "check_in", "break_end":
                return "#28a745"; // Green
            case "break_start":
                return "yellow"; // Yellow
            case "missing_check_out":
                return "#ff0000"; // Red
            case "check_out":
                return "#F9F6EE"; // White
            case "edited_check_in", "edited_check_out", "edited_break_start", "edited_break_end":
                return "#fdbb3c";
            default:
                return "#F9F6EE"; // Default white
        }
    }

   private String getEventLabelForEventType(String eventType) {
       String customMessage = "";
       switch (eventType) {
           case "check_in":
               customMessage = "Start: ";
               break;
           case "break_start":
               customMessage = "Pause:\n";
               break;
           case "break_end":
               customMessage = "-\n";
               break;
           case "check_out":
               customMessage = "Slut: ";
               break;
           case "missing_check_out":
               customMessage = "Mangler\n check-ud";
               break;
           case "edited_check_in":
               customMessage = "Redigeret \nStart: ";
               break;
           case "edited_break_start":
               customMessage = "Redigeret \nPause: ";
               break;
           case "edited_break_end":
               customMessage = "Redigeret \n";
               break;
           case "edited_check_out":
               customMessage = "Redigeret \nSlut: ";
               break;
           default:
               break;
       }
       return customMessage;
   }
    public enum EventType {
        check_in, check_out, break_start, break_end, missing_check_out;
    }

    private void addUniqueLabel(VBox container, Label labelToAdd, Set<String> addedLabels) {
        if (labelToAdd == null || addedLabels.contains(labelToAdd.getText())) {
            return;
        }
        addedLabels.add(labelToAdd.getText());
        container.getChildren().add(labelToAdd);
        // Check if a label with the same text already exists
        boolean exists = container.getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> (Label) node)
                .anyMatch(existingLabel -> existingLabel.getText().equals(labelToAdd.getText()));

        // Add the label only if it doesn't exist
        if (!exists) {
            container.getChildren().add(labelToAdd);
        }
    }


    private void showEditModal(int userId) {
        System.out.println("Editing for User ID: " + userId);
        // Implement modal right here Flemming
    }

    private void showNoteModal(int userId) {
        System.out.println("Adding note for User ID: " + userId);
        // Implement modal Flemming
    }


    private void handleDateClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String day = clickedButton.getText();
        System.out.println("Clicked: " + day);
        try {
            int dayOfMonth = Integer.parseInt(day.trim());
            LocalDate selectedDate = currentMonth.atDay(dayOfMonth);
            TitledPane dayPane = dayPaneMap.get(selectedDate);

            if (dayPane == null){
                if (lastLoadedDate == null) {
                lastLoadedDate = selectedDate;
            }
            long daysBetween = ChronoUnit.DAYS.between(lastLoadedDate, selectedDate) - 1;
            if (daysBetween != 0) {
                generateTimelogBoxes(lastLoadedDate, Math.abs((int) daysBetween));
            }
            lastLoadedDate = selectedDate;
                dayPane = dayPaneMap.get(selectedDate);

                if (dayPane != null) {
                    dayPane.setExpanded(true);
                        scrollToBottom();
                    return;
                }
            }

            if (dayPane != null) {

                dayPane.setExpanded(true);
                Bounds dayPaneBounds = dayPane.localToScene(dayPane.getBoundsInLocal());
                double yOffset = dayPaneBounds.getMinY();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();

                // Calculate the amount to scroll to center the dayPane vertically
                double targetVValue = (yOffset - (viewportHeight / 2)) / (centerPanel.getHeight() - viewportHeight);
                targetVValue = Math.max(0, Math.min(targetVValue, 1));
                scrollPane.setVvalue(targetVValue);
            } else {
                System.out.println("DayPane not found for date: " + selectedDate);
            }
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: " + day);
        }
    }
        private void scrollToBottom() {
            // Force layout updates
            centerPanel.applyCss();
            centerPanel.layout();

            // Defer scrolling to allow for any remaining layout adjustments
            Platform.runLater(() -> {
                // Apply scroll
                scrollPane.setVvalue(1.0);
            });
        }

    private void handleLogOut() {
        Session.clearSession();

        Stage stage = (Stage) managerLogOutButton.getScene().getWindow();
        ManagerDailyService.loadLoginPage(stage);
    }
    private void handleBackButton() {
        Stage stage = (Stage) BackButton.getScene().getWindow();
        WeeklyOverviewService.loadMenuPage(stage);
    }
    private void handleDailyOverview() {
        dailyOverviewButton.getStyleClass().add("managerSelectedBox");
    }
    private void handleWeeklyOverview() {
        Stage stage = (Stage) weeklyOverviewButton.getScene().getWindow();
        service.loadWeeklyPage(stage);
    }
    private void handleEditEmployees() {
        Stage stage = (Stage) editEmployeesButton.getScene().getWindow();
        service.loadAdminPage(stage);
    }
    private void handleExportData() {
        // Modal
    }
}
