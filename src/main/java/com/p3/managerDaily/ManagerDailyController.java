package com.p3.managerDaily;

import com.p3.session.Session;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import java.util.Map;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;


public class ManagerDailyController {

    ManagerDailyDAO dao = new ManagerDailyDAO();
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
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int col = 0; col < daysOfWeek.length; col++) {
            Label dayLabel = new Label(daysOfWeek[col]);
            calendarGrid.add(dayLabel, col, 0); // Add to the first row
        }

        // Get the first day of the month and the total number of days
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Determine the starting day of the week (0=Sunday, 1=Monday, etc.)
        int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        // Set grid constraints to ensure equal column widths
        for (int col = 0; col < 7; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(14.2857); // 100% / 7 columns = 14.2857% duhh..
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
            if (col == 7) { // Move to the next row after Saturday
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

        // Load timelogs for the range of days
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
        // Filter the timelogs for the given date
        List<Map<String, Object>> dailyTimelogs = timelogs.stream()
                .filter(timelog -> {
                    Object shiftDate = timelog.get("shift_date");

                    // If shift_date is a String, parse it to LocalDate for comparison
                    if (shiftDate instanceof String) {
                        try {
                            LocalDate shiftDateParsed = LocalDate.parse((String) shiftDate);
                            return shiftDateParsed.equals(date); // Compare the parsed date
                        } catch (Exception e) {
                            System.err.println("Error parsing shift date: " + shiftDate);
                        }
                    }
                    // If shift_date is already a LocalDate, directly compare it
                    else if (shiftDate instanceof LocalDate) {
                        return shiftDate.equals(date);
                    }
                    return false; // Default case if shift_date is not a String or LocalDate
                })
                .collect(Collectors.toList());

        // Date Formatting
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        // Create the title pane for the day
        TitledPane dayPane = new TitledPane();
        dayPane.setText(formattedDate);
        dayPane.getStyleClass().add("collapseDateBox");
        dayPane.setExpanded(false);

        // VBox to hold all employee rows
        VBox employeeRows = new VBox();
        employeeRows.setSpacing(15); // Space between rows
        System.out.println("Timelogs for " + date + ": " + dailyTimelogs);

        // Iterate through each timelog entry
        for (Map<String, Object> timelog : dailyTimelogs) {
            Integer userId = (Integer) timelog.getOrDefault("user_id", 0);
            String userName = (String) timelog.getOrDefault("username", "Unknown Name");

            // Create an HBox for the employee row
            HBox employeeRow = new HBox();
            employeeRow.getStyleClass().add("managerDailyEmployeeBox");
            employeeRow.setSpacing(15);

            // Text element for the user's name
            Text usernameText = new Text("Ansat: " + userName);

            // Create a GridPane to display hourly times
            GridPane timeGrid = new GridPane();
            timeGrid.setHgap(5); // Space between columns
            timeGrid.setVgap(5); // Space between rows

            // Define hours (e.g., 9 AM to 5 PM)
            int startHour = ManagerDailyService.getEarliestTime(userId);
            int endHour = ManagerDailyService.getLatestTime(userId);

            // Populate the grid with hourly slots
            for (int hour = startHour; hour <= endHour; hour++) {
                String timeLabel = String.format("%02d:00", hour);
                Label timeSlot = new Label(timeLabel);
                timeSlot.getStyleClass().add("hourLabel");
                timeGrid.add(timeSlot, hour - startHour, 0);

                String eventForHour = ManagerDailyService.getEventForHour(userId, hour);
                Label eventLabel = new Label(eventForHour);
                eventLabel.getStyleClass().add("eventLabel");
                timeGrid.add(eventLabel, hour - startHour, 1);
            }
            System.out.println("Start hour:" + startHour);
            System.out.println("End hour:" + endHour);

            // Add buttons to the employeeRow
            Button editButton = new Button();
            editButton.setOnAction(e -> showEditModal(userId));

            Button noteButton = new Button();
            noteButton.setOnAction(e -> showNoteModal(userId));

            // Add all components to the employeeRow
            employeeRow.getChildren().addAll(usernameText, timeGrid, editButton, noteButton);

            // Add the row to the VBox
            employeeRows.getChildren().add(employeeRow);
        }

        // Add all employee rows to the day pane
        dayPane.setContent(employeeRows);

        // Add the day pane to the center panel
        centerPanel.getChildren().add(dayPane);
    }

    // Example placeholders for modal methods
    private void showEditModal(int userId) {
        System.out.println("Editing for User ID: " + userId);
        // Implement modal logic
    }

    private void showNoteModal(int userId) {
        System.out.println("Adding note for User ID: " + userId);
        // Implement modal logic
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
