package com.p3.managerDaily;

import com.p3.session.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.event.ActionEvent;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import java.util.Map;
import java.util.List;
import java.time.format.DateTimeFormatter;



public class ManagerDailyController {

    ManagerDailyDAO dao = new ManagerDailyDAO();

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
        yearMonthLabel.setText(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + yearMonth.getYear());

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
        // Generate timelog boxes for each day within the range
        for (int i = 0; i < daysCount; i++) {
            LocalDate currentDate = startDate.minusDays(i);
            generateTimelogBox(currentDate);
        }
    }
    private void generateTimelogBox(LocalDate date) {
        List<Map<String, Object>> timelogs = dao.getTimelogsForDate(date);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        String formattedDate = date.format(formatter);

        TitledPane dayPane = new TitledPane();
        dayPane.setText("Timelogs for " + formattedDate);

        dayPane.setExpanded(false);

        VBox employeeRows = new VBox();
        employeeRows.setSpacing(10); // Space between rows


        for (Map<String, Object> timelog : timelogs) {

            // Use safe methods to get values from the map and handle potential nulls
            Integer userId = (Integer) timelog.get("userId");
            String eventType = (String) timelog.get("eventType");
            String shiftDate = (String) timelog.get("shiftDate");

            if (userId == null) {
                userId = 0;
            }

            if (eventType == null) {
                eventType = "Unknown Event";
            }
            if (shiftDate == null) {
                shiftDate = "Unknown Date";
            }


            HBox employeeRow = new HBox();
            employeeRow.setSpacing(15);

            Text userText = new Text("User ID: " + userId);
            Text eventText = new Text("Event: " + eventType);
            Text dateText = new Text("Shift Date: " + shiftDate);


            employeeRow.getChildren().addAll(userText, eventText, dateText);


            employeeRows.getChildren().add(employeeRow);
        }

        dayPane.setContent(employeeRows);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(employeeRows);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        dayPane.setContent(scrollPane);

        centerPanel.getChildren().add(dayPane);
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
