package com.p3.managerDaily;

import com.p3.session.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;


public class ManagerDailyController {

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
       /* handleLogOut();
        handleDailyOverview();
        handleWeeklyOverview();
        handleEditEmployees();
        handleExportData();
*/
        currentMonth = YearMonth.now();
        generateCalendar(currentMonth);


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
