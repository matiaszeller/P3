package com.p3.managerDaily;

import com.p3.session.Session;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.Locale;
import java.util.Iterator;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import java.util.Map;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.shape.Rectangle;

public class ManagerDailyController {

    ManagerDailyService service = new ManagerDailyService();

    @FXML
    private VBox managerDailyRoot;
    @FXML
    private VBox centerPanel;

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


    private YearMonth currentMonth;

    @FXML
    public void initialize() {
        managerLogOutButton.setOnAction(event -> handleLogOut());
        dailyOverviewButton.setOnAction(event -> handleDailyOverview());
        weeklyOverviewButton.setOnAction(event -> handleWeeklyOverview());
        editEmployeesButton.setOnAction(event -> handleEditEmployees());
        exportDataButton.setOnAction(event -> handleExportData());

        currentMonth = YearMonth.now();
        generateCalendar(currentMonth);
        generateTimelogBoxes(LocalDate.now(), 50);

        // Add navigation button actions
        prevMonthButton.setOnAction(event -> changeMonth(-1));
        nextMonthButton.setOnAction(event -> changeMonth(1));
    }

    private void changeMonth(int offset) {
        currentMonth = currentMonth.plusMonths(offset);
        generateCalendar(currentMonth);

    }


    public void generateCalendar(YearMonth yearMonth) {
        // Clear the current grid
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // Update the month label
        yearMonthLabel.setText(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + yearMonth.getYear());

        // Add day-of-week headers
        String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}; // Start with Monday
        for (int col = 0; col < daysOfWeek.length; col++) {
            Label dayLabel = new Label(daysOfWeek[col]);
            calendarGrid.add(dayLabel, col, 0); // Add to the first row
        }

        // Get the first day of the month and the total number of days
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Determine the starting day of the week (1=Monday, 2=Tuesday, ..., 7=Sunday)
        int startDayOfWeek = (firstDayOfMonth.getDayOfWeek().getValue() - 1) % 7;

        // Set grid constraints to ensure equal column widths
        for (int col = 0; col < 7; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(14.2857); // 100% / 7 columns = 14.2857%
            calendarGrid.getColumnConstraints().add(colConstraints);
        }

        // Populate the calendar with buttons for each date
        int row = 1;
        int col = startDayOfWeek;
        for (int day = 1; day <= daysInMonth; day++) {
            Button dateButton = new Button(String.valueOf(day));
            dateButton.setOnAction(this::handleDateClick); // Add click handler
            dateButton.setMinSize(40, 40);
            dateButton.getStyleClass().add("dateButton");

            calendarGrid.add(dateButton, col, row);

            col++;
            if (col == 7) { // Move to the next row after Sunday
                col = 0;
                row++;
            }
        }

        // Set row constraints to ensure equal row heights
        for (int r = 0; r < row; r++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS); // Make the rows grow evenly
            calendarGrid.getRowConstraints().add(rowConstraints);
        }
    }

    public void generateTimelogBoxes(LocalDate startDate, int daysCount) {
        centerPanel.getChildren().clear();
        ScrollPane scrollPane = new ScrollPane();
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);

        TitledPane dayPane = new TitledPane();
        dayPane.setText(formattedDate);
        dayPane.getStyleClass().add("collapseDateBox");
        dayPane.setExpanded(false);

        VBox employeeRows = new VBox();
        employeeRows.setSpacing(15);

        // Iterate over each user_id and their timelogs
        for (Map.Entry<Integer, List<Map<String, Object>>> entry : timelogsByUserId.entrySet()) {
            Integer userId = entry.getKey();
            List<Map<String, Object>> userTimelogs = entry.getValue();
            String fullName = service.getUserFullName(userId);

            // Create a structured employee row
            HBox employeeRow = new HBox();
            employeeRow.getStyleClass().add("managerDailyEmployeeBox");
            employeeRow.setSpacing(20); // Add spacing between the three parts
            employeeRow.setAlignment(Pos.CENTER_LEFT); // Align to the left

            // Box with employee's name
            VBox nameBox = new VBox();
            nameBox.setAlignment(Pos.CENTER); // Align name to the left
            nameBox.setMinSize(125, 100);
            Text fullNameText = new Text(fullName);
            nameBox.getChildren().add(fullNameText);

            // Box with the timelog (hourly shifts)
            VBox timelogBox = new VBox();
            timelogBox.setSpacing(2);  // Add spacing between hour boxes
            timelogBox.setAlignment(Pos.CENTER_LEFT); // Align the hourly boxes to the left
            timelogBox.setMaxWidth(Double.POSITIVE_INFINITY);

            // Get start and end times for the user's shift
            int startTime = ManagerDailyService.getEarliestTime(userId);
            int endTime = ManagerDailyService.getLatestTime(userId);

            // Sort timelogs by event time
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

            // Create an HBox for hourly boxes
            HBox hourlyBoxes = new HBox();
            // Track the current color
            String currentColor = "#F9F6EE"; // Default color
            Iterator<Map<String, Object>> timelogIterator = userTimelogs.iterator();
            Map<String, Object> nextTimelog = timelogIterator.hasNext() ? timelogIterator.next() : null;

            for (int hour = startTime; hour < endTime; hour++) {
                StackPane hourBox = new StackPane();
                hourBox.getStyleClass().add("hourBox");

                // Label to display the time
                Label timeLabel = new Label(hour + ":00");
                timeLabel.getStyleClass().add("hourBoxTimeLabel");
                StackPane.setAlignment(timeLabel, Pos.TOP_LEFT);

                // Create a VBox to stack event type labels
                VBox eventLabels = new VBox();
                eventLabels.setSpacing(5);  // Spacing between multiple event labels
                eventLabels.setAlignment(Pos.CENTER); // Align labels in the center of the hour box


                // Check if we have a new event in this hour
                while (nextTimelog != null) {
                    Object eventTimeObj = nextTimelog.get("event_time");
                    LocalDateTime eventTime = null;

                    // Parse the event_time to LocalDateTime
                    if (eventTimeObj instanceof String) {
                        eventTime = LocalDateTime.parse((String) eventTimeObj, DateTimeFormatter.ISO_DATE_TIME);
                    } else if (eventTimeObj instanceof LocalDateTime) {
                        eventTime = (LocalDateTime) eventTimeObj;
                    }

                    if (eventTime != null && eventTime.getHour() == hour) {
                        // Handle event_type processing
                        Object eventTypeObj = nextTimelog.get("event_type");
                        String eventType = null;

                        if (eventTypeObj instanceof String) {
                            try {
                                String eventTypeStr = ((String) eventTypeObj).toUpperCase().replace("_", "_");
                                EventType eventTypeEnum = EventType.valueOf(eventTypeStr); // Parse the Enum
                                eventType = eventTypeEnum.name(); // Get the Enum's name as a String
                            } catch (IllegalArgumentException e) {
                                System.err.println("Unknown event_type: " + eventTypeObj);
                            }
                        }

                        if (eventType != null) {
                            // Display event time and event type
                            Label eventLabel = new Label(eventTime.getMinute() + " - " + eventType);
                            eventLabel.getStyleClass().add("eventTypeLabel");

                            // Add the event label to the VBox
                            eventLabels.getChildren().add(eventLabel);

                            // Determine the color of the event
                            currentColor = getColorForEventType(eventType); // Update the current color
                        }

                        nextTimelog = timelogIterator.hasNext() ? timelogIterator.next() : null; // Move to the next event
                    } else {
                        break; // No more events in this hour
                    }
                }

                hourBox.setStyle("-fx-background-color: " + currentColor + "; -fx-border-color: #000000;");
                hourBox.getChildren().add(timeLabel);
                hourBox.getChildren().add(eventLabels); // Add event labels

                hourlyBoxes.getChildren().add(hourBox);
            }

            // Add hourly boxes to the timelogBox
            timelogBox.getChildren().add(hourlyBoxes);

            // Box with the buttons (Edit and Note)
            VBox buttonBox = new VBox();
            buttonBox.setSpacing(10);  // Add spacing between the buttons
            buttonBox.setAlignment(Pos.CENTER); // Center the buttons
            buttonBox.setMinSize(100, 100);

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

        dayPane.setContent(employeeRows);
        centerPanel.getChildren().add(dayPane);
    }




    // Helper method to determine color based on event type
    public String getColorForEventType(String eventType) {
        if (eventType == null) {
            return "#F9F6EE"; // Default white
        }
        switch (eventType) {
            case "CHECK_IN":
                return "#28a745"; // Green
            case "CHECK_OUT":
                return "#28a745"; // Green
            case "BREAK_START":
                return "#ffc107"; // Yellow
            case "BREAK_END":
                return "#28a745"; // Green
            default:
                return "#F9F6EE"; // Default white
        }
    }

    public enum EventType {
        CHECK_IN,
        CHECK_OUT,
        BREAK_START,
        BREAK_END,
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

        // Den skal highlighte dagen som bliver clicket på i center delen og expand dagen's ting, hvad end man kalder de der udfolder ting ya know..

    }

    private void handleLogOut() {
        Session.clearSession();

        Stage stage = (Stage) managerLogOutButton.getScene().getWindow();
        ManagerDailyService.loadLoginPage(stage);
    }
    private void handleDailyOverview() {
        //Ny side skal loades
    }
    private void handleWeeklyOverview() {
        //Ny side skal loades
    }
    private void handleEditEmployees() {
        //Ny side skal loades
    }
    private void handleExportData() {
        // Modal
    }
}
