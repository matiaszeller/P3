package com.p3.overview;

import com.p3.exportModal.ExportModalController;
import com.p3.managerDaily.ManagerDailyService;
import com.p3.menu.MenuService;
import com.p3.session.Session;
import com.p3.util.ModalUtil;
import com.p3.util.StageLoader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.util.Duration;

import java.io.IOException;
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.*;

public class WeeklyOverviewController {
    @FXML
    public Button BackButton;
    @FXML
    private GridPane gridPane;

    @FXML
    private VBox Weeklyroot;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button logOutButton;

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
    private final StageLoader stageLoader = new StageLoader();

    private static final int WEEK_WIDTH = 70;
    private static final int NUM_WEEKS = 52;
    private static final int EMPLOYEE_WIDTH = 150;


    @FXML
    public void initialize() {
        logOutButton.setOnAction(event -> {
            try {
                handleLogOut();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        dailyOverviewButton.setOnAction(event -> {
            try {
                handleDailyOverview();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        handleWeeklyOverview();
        editEmployeesButton.setOnAction(event -> {
            try {
                handleEditEmployees();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        exportDataButton.setOnAction(event -> handleExportData());
        BackButton.setOnAction(event -> {
            try {
                handleBackButton();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        currentMonth = YearMonth.now();
        generateCalendar(currentMonth);

        // Add navigation button actions
        prevMonthButton.setOnAction(event -> changeMonth(-1));
        nextMonthButton.setOnAction(event -> changeMonth(1));

        int currentYear = LocalDate.now().getYear();
        WeeklyOverviewService.loadWeeklyTimelogs(currentYear);

        setupGridPaneConstraints();
        loadGridPane(currentYear);
        scrollPane.setContent(gridPane);
        applyBordersToCells();
        contentPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                adjustLayout();
            }
        });
    }

    private void adjustLayout() {
        // Dynamically adjust contentPane size based on the Scene
        Scene scene = contentPane.getScene();
        if (scene != null) {
            contentPane.setPrefWidth(scene.getWidth() - 20);
            contentPane.setPrefHeight(scene.getHeight() - 60);

            // Optional: Add listeners to dynamically adjust on window resize
            scene.widthProperty().addListener((obs, oldWidth, newWidth) ->
                    contentPane.setPrefWidth(newWidth.doubleValue() - 20)
            );
            scene.heightProperty().addListener((obs, oldHeight, newHeight) ->
                    contentPane.setPrefHeight(newHeight.doubleValue() - 20)
            );
        }
    }
    private void changeMonth(int offset) {
        YearMonth newMonth = currentMonth.plusMonths(offset);
        int newYear = newMonth.getYear();

        if (newYear != currentMonth.getYear()) {
            WeeklyOverviewService.loadWeeklyTimelogs(newYear);
            loadGridPane(newYear);
        }
        currentMonth = newMonth;
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
            int finalDay = day;
            dateButton.setOnAction(event -> handleDateClick(LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), finalDay)));
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

    // Configure column constraints for the GridPane
    private void setupGridPaneConstraints() {
        // Employee column
        ColumnConstraints employeeColumn = new ColumnConstraints();
        employeeColumn.setMinWidth(EMPLOYEE_WIDTH);
        employeeColumn.setPrefWidth(EMPLOYEE_WIDTH);
        employeeColumn.setHgrow(Priority.NEVER);
        gridPane.getColumnConstraints().add(employeeColumn);

        // Week columns
        for (int i = 0; i < NUM_WEEKS; i++) {
            ColumnConstraints weekColumn = new ColumnConstraints();
            weekColumn.setMinWidth(WEEK_WIDTH);
            weekColumn.setPrefWidth(WEEK_WIDTH);
            weekColumn.setHgrow(Priority.NEVER);
            gridPane.getColumnConstraints().add(weekColumn);
        }
    }

    // Loads the GridPane
    private void loadGridPane(int year) {
        gridPane.getChildren().clear();

        scrollPane.setContent(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        if (!contentPane.getChildren().contains(scrollPane)) {
            contentPane.getChildren().add(scrollPane);
        }

        // Adds week labels
        for (int col = 1; col <= NUM_WEEKS; col++) {
            Label weekLabel = new Label("Uge " + col);
            weekLabel.setMinWidth(WEEK_WIDTH);
            weekLabel.setPrefWidth(WEEK_WIDTH);
            weekLabel.setAlignment(Pos.CENTER);
            gridPane.add(weekLabel, col, 0);
        }

        // Gets data and sets up rows for each user
        List<Map<String, Object>> timelogData = WeeklyOverviewService.getWeeklyTimelogs();

        // To avoid NullPointerException
        if (timelogData == null) {
            timelogData = new ArrayList<>();
        }

        // Keeps track of what row is connected to a user_id
        Map<Integer, Integer> userIdToRow = new HashMap<>();
        int currentRow = 1;

        // Fill data in GridPane
        for (Map<String, Object> data : timelogData) {
            Integer userId = (Integer) data.get("userId");
            String full_name = (String) data.get("fullname");
            String weekStartStr = (String) data.get("weekStart");
            String totalHoursWorkedStr = (String) data.get("totalHoursWorked");

            if (weekStartStr != null && !weekStartStr.isEmpty() &&
                    totalHoursWorkedStr != null && !totalHoursWorkedStr.isEmpty()) {

                // Converts date string to LocalDate, converts string to decimal, and finds week number
                LocalDate weekStart = LocalDate.parse(weekStartStr);
                int hoursWorked = (int) convertToDecimalHours(totalHoursWorkedStr);
                int weekNumber = getWeekOfYear(weekStart);

                // Create row for user and displays full_name
                if (!userIdToRow.containsKey(userId)) {
                    userIdToRow.put(userId, currentRow);
                    Label userIdLabel = new Label(full_name);
                    userIdLabel.setStyle("-fx-alignment: center; -fx-padding: 10;");
                    Rectangle userBox = new Rectangle(EMPLOYEE_WIDTH, WEEK_WIDTH);
                    userBox.setFill(currentRow % 2 == 0 ? Color.web("#d5f0e8") : Color.WHITE);
                    StackPane userIdContainer = new StackPane();
                    userIdContainer.getChildren().addAll(userBox, userIdLabel);
                    userIdContainer.setStyle("-fx-border-color: gray; -fx-border-width: 1;");
                    gridPane.add(userIdContainer, 0, currentRow);
                    currentRow++;
                }

                int row = userIdToRow.get(userId);
                Node existingNode = getNodeByRowColumnIndex(row, weekNumber, gridPane);
                if (existingNode instanceof StackPane cellContainer) {
                    Label existingLabel = (Label) cellContainer.getChildren().get(1);
                    existingLabel.setText(hoursWorked + " Timer");
                } else {
                    Label hoursLabel = new Label(hoursWorked + " Timer");
                    StackPane cellContainer = new StackPane();
                    Rectangle cell = new Rectangle(WEEK_WIDTH, WEEK_WIDTH);
                    cell.setFill(hoursWorked >= 48 ? Color.web("#ffb0b6") : (row % 2 == 0 ? Color.web("#d5f0e8") : Color.WHITE));
                    cellContainer.getChildren().addAll(cell, hoursLabel);
                    cellContainer.setStyle("-fx-border-color: gray; -fx-border-width: 1;");
                    gridPane.add(cellContainer, weekNumber, row);
                }
            } else {
                System.err.println("Invalid data for user: " + userId);
            }
        }

        // Cell colour til alternate rows with empty cell
        for (Integer userId : userIdToRow.keySet()) {
            int row = userIdToRow.get(userId);
            for (int col = 1; col <= NUM_WEEKS; col++) {
                if (getNodeByRowColumnIndex(row, col, gridPane) == null) {
                    StackPane cellContainer = new StackPane();
                    Rectangle cell = new Rectangle(WEEK_WIDTH, WEEK_WIDTH);
                    cell.setFill(row % 2 == 0 ? Color.web("#d5f0e8") : Color.WHITE);
                    cellContainer.getChildren().add(cell);
                    cellContainer.setStyle("-fx-border-color: gray; -fx-border-width: 1;");
                    gridPane.add(cellContainer, col, row);
                }
            }
        }

        Platform.runLater(this::focusCurrentWeekDefault);
    }

    // Converts string to hours
    private double convertToDecimalHours(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        return hours + (minutes / 60.0) + (seconds / 3600.0);
    }

    // Gray borders to GridPane cells
    private void applyBordersToCells() {
        gridPane.getChildren().forEach(node -> {
            if (node instanceof StackPane stackPane) {
                stackPane.setStyle("-fx-border-color: gray; -fx-border-width: 1;");
            }
        });
    }

    // Gets the week of the year
    private int getWeekOfYear(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date.get(weekFields.weekOfWeekBasedYear());
    }

    // Scrolls to the week number from the clicked calendar
    private void scrollToWeek(LocalDate date) {
        int weekNumber = getWeekOfYear(date);
        Node node = getNodeByRowColumnIndex(0, weekNumber, gridPane);
        if (node != null) {
            scrollPane.layout();
            double x = node.getBoundsInParent().getMinX();
            scrollPane.setHvalue((x / gridPane.getWidth()) * 1.05);
        }
    }

    // Gets the node located at the specified row and column in the gridPane
    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer columnIndex = GridPane.getColumnIndex(node);
            if (rowIndex != null && columnIndex != null && rowIndex == row && columnIndex == column) {
                return node;
            }
        }
        return null;
    }

    // Scrolls to column of the current week when loading
    private void focusCurrentWeekDefault() {
        scrollToWeek(LocalDate.now());
    }

    private void handleBackButton() throws IOException {
        Stage stage = (Stage) BackButton.getScene().getWindow();
        stageLoader.loadStage("/com.p3.menu/MenuPage.fxml", stage);
    }

    private void handleDateClick(LocalDate date) {
        int selectedYear = date.getYear();
        if (selectedYear != currentMonth.getYear()) {
            WeeklyOverviewService.loadWeeklyTimelogs(selectedYear);
            loadGridPane(selectedYear);
            currentMonth = YearMonth.of(selectedYear, date.getMonth());
        }
        scrollToWeek(date);
    }

    private void handleLogOut() throws IOException {
        Session.clearSession();
        Stage stage = (Stage) logOutButton.getScene().getWindow();
        stageLoader.loadStage("/com.p3.login/LoginPage.fxml", stage);
    }

    private void handleDailyOverview() throws IOException {
        Stage stage = (Stage) dailyOverviewButton.getScene().getWindow();
        stageLoader.loadStage("/com.p3.managerDaily/ManagerDaily.fxml", stage);
    }

    private void handleWeeklyOverview() {
        weeklyOverviewButton.getStyleClass().add("managerSelectedBox");
    }

    private void handleEditEmployees() throws IOException {
        Stage stage = (Stage) editEmployeesButton.getScene().getWindow();
        stageLoader.loadStage("/com.p3.administration/EditUserPage.fxml", stage);
    }
    private void handleExportData() {
        Stage stage = (Stage) exportDataButton.getScene().getWindow();
        ModalUtil.ModalResult<ExportModalController> modalResult = ModalUtil.showModal("/com.p3.global/ExportModal.fxml", stage, "Export Data");
        if(modalResult != null){
            Stage modalStage = modalResult.getStage();
            modalStage.showAndWait();
        }
    }
}