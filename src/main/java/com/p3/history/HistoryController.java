package com.p3.history;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

public class HistoryController {

    @FXML
    private AnchorPane contentPane;

    @FXML
    private Button historyButton;

    @FXML
    private Button returnButton;

    @FXML
    private Label totalHoursLabel;

    private final String url = "jdbc:mysql://localhost:3306/database";
    private final String user = "root";
    private final String password = "Westcoast33!";

    public static void showHistoryPage(Stage stage) {
        try {
            URL fxmlLocation = HistoryController.class.getResource("/com.p3.historyFxml/HistoryPage.fxml");
            if (fxmlLocation == null) {
                System.out.println("FXML file not found at the specified path.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
            stage.setScene(scene);
            stage.show();

            HistoryController controller = fxmlLoader.getController();
            if (controller.checkDatabaseConnection()) {
                controller.loadWeeklyGridPane();
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        historyButton.setOnAction(event -> {
            if (checkDatabaseConnection()) {
                loadWeeklyGridPane();
            } else {
                System.out.println("Failed to connect to the database.");
            }
        });

        returnButton.setOnAction(event -> loadMenuPage());
    }

    private void loadMenuPage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.p3.menu/MenuPage.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1536, 960);
            Stage stage = (Stage) returnButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkDatabaseConnection() {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            return connection != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadWeeklyGridPane() {
        GridPane gridPane = new GridPane();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        int startHour = 7;
        int endHour = calculateEndHour(weekStart);

        for (int day = 0; day < 7; day++) {
            LocalDate date = weekStart.plusDays(day);

            Label dayLabel = new Label(date + " (" + date.getDayOfWeek() + ")");
            dayLabel.setMinWidth(80);
            gridPane.add(dayLabel, 0, day + 1);

            Button commentButton = new Button("Tilføj kommentar");
            commentButton.setOnAction(e -> openCommentDialog(date));
            gridPane.add(commentButton, endHour - startHour + 2, day + 1);

            for (int hour = startHour; hour < endHour; hour++) {
                StackPane cellContainer = new StackPane();
                Rectangle cell = new Rectangle(50, 50);
                cell.setFill(Color.WHITE);
                cell.setStroke(Color.GRAY);

                Label timeLabel = new Label(String.format("%02d:00", hour));
                timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: black;");

                cellContainer.getChildren().addAll(cell, timeLabel);
                StackPane.setAlignment(timeLabel, Pos.TOP_LEFT);
                StackPane.setMargin(timeLabel, new Insets(2, 0, 0, 2));
                gridPane.add(cellContainer, hour - startHour + 1, day + 1);
            }
        }

        applyWeeklyEventColors(gridPane, weekStart);
        contentPane.getChildren().clear();
        contentPane.getChildren().add(gridPane);
    }

    private void applyWeeklyEventColors(GridPane gridPane, LocalDate weekStart) {
        String query = "SELECT shift_date, event_time, event_type FROM timelog WHERE shift_date BETWEEN ? AND ? ORDER BY shift_date, event_time";

        Map<LocalDate, LocalDateTime> checkInMap = new HashMap<>();
        Map<LocalDate, Integer> dailyWorkMinutes = new HashMap<>();

        int totalMinutesWorked = 0;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(weekStart));
            stmt.setDate(2, Date.valueOf(weekStart.plusDays(6)));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDate shiftDate = rs.getDate("shift_date").toLocalDate();
                LocalDateTime eventTime = rs.getTimestamp("event_time").toLocalDateTime();
                String eventType = rs.getString("event_type");
                int row = shiftDate.getDayOfWeek().getValue();

                switch (eventType) {
                    case "check_in":
                        checkInMap.put(shiftDate, eventTime);
                        break;

                    case "check_out":
                        LocalDateTime checkInTime = checkInMap.get(shiftDate);
                        if (checkInTime != null) {
                            // Calculate minutes worked for this interval
                            int workedMinutes = (int) Duration.between(checkInTime, eventTime).toMinutes();
                            dailyWorkMinutes.put(shiftDate, dailyWorkMinutes.getOrDefault(shiftDate, 0) + workedMinutes);
                            totalMinutesWorked += workedMinutes;

                            // Color the cells for this interval
                            colorCells(gridPane, row, checkInTime, eventTime, Color.GREEN);
                            checkInMap.remove(shiftDate);
                        }
                        break;

                    case "break_start":
                        LocalDateTime activeCheckInTime = checkInMap.get(shiftDate);
                        if (activeCheckInTime != null) {
                            // Color work period before the break
                            colorCells(gridPane, row, activeCheckInTime, eventTime, Color.GREEN);

                            // Calculate minutes worked up to the break
                            int workedMinutes = (int) Duration.between(activeCheckInTime, eventTime).toMinutes();
                            dailyWorkMinutes.put(shiftDate, dailyWorkMinutes.getOrDefault(shiftDate, 0) + workedMinutes);
                            totalMinutesWorked += workedMinutes;

                            checkInMap.remove(shiftDate);
                        }
                        checkInMap.put(shiftDate, eventTime);
                        break;

                    case "break_end":
                        LocalDateTime breakStartTime = checkInMap.get(shiftDate);
                        if (breakStartTime != null) {
                            // Color the break period
                            colorCells(gridPane, row, breakStartTime, eventTime, Color.ORANGE);

                            // Update the map with the time after the break
                            checkInMap.put(shiftDate, eventTime);
                        }
                        break;
                }
            }

            // Handle missing check-outs by coloring red to the end of the day
            for (Map.Entry<LocalDate, LocalDateTime> entry : checkInMap.entrySet()) {
                LocalDate shiftDate = entry.getKey();
                LocalDateTime checkInTime = entry.getValue();
                int row = shiftDate.getDayOfWeek().getValue();

                LocalDateTime endOfDay = checkInTime.toLocalDate().atTime(23, 59);
                int missingMinutes = (int) Duration.between(checkInTime, endOfDay).toMinutes();
                dailyWorkMinutes.put(shiftDate, dailyWorkMinutes.getOrDefault(shiftDate, 0) + missingMinutes);
                totalMinutesWorked += missingMinutes;

                colorCells(gridPane, row, checkInTime, endOfDay, Color.RED);
            }

            // Display total weekly hours
            int totalHours = totalMinutesWorked / 60;
            int remainingMinutes = totalMinutesWorked % 60;
            totalHoursLabel.setText(String.format("Samlet timer denne uge: %d:%02d", totalHours, remainingMinutes));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private int calculateEndHour(LocalDate weekStart) {
        String query = "SELECT MAX(HOUR(event_time)) AS max_hour FROM timelog WHERE shift_date BETWEEN ? AND ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(weekStart));
            stmt.setDate(2, Date.valueOf(weekStart.plusDays(6)));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("max_hour");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 24;
    }

    private void colorCells(GridPane gridPane, int row, LocalDateTime start, LocalDateTime end, Color color) {
        int startHour = start.getHour();
        int endHour = end.getHour();
        int startMinute = start.getMinute();
        int endMinute = end.getMinute();

        for (int hour = startHour; hour <= endHour; hour++) {
            int col = hour - 7 + 1;
            StackPane cellContainer = getNodeByRowColumnIndex(row, col, gridPane);

            if (cellContainer != null) {
                // Calculate the fraction of the hour that the event covers
                double startFraction = (hour == startHour) ? startMinute / 60.0 : 0;
                double endFraction = (hour == endHour) ? endMinute / 60.0 : 1;

                // Adjust the width and position of the overlay rectangle
                double cellWidth = 50; // Assuming each cell is 50px wide
                double overlayWidth = cellWidth * (endFraction - startFraction);
                Rectangle overlayRect = new Rectangle(overlayWidth, 50); // 50px height for each cell
                overlayRect.setFill(color);
                overlayRect.setTranslateX(cellWidth * (startFraction - 0.5) + overlayWidth / 2);

                // Remove conflicting rectangles of the same color
                cellContainer.getChildren().removeIf(node -> node instanceof Rectangle && ((Rectangle) node).getFill().equals(color));

                // Add the overlay rectangle to the cell
                cellContainer.getChildren().add(overlayRect);

                // Ensure the time label remains on top
                cellContainer.getChildren().stream()
                        .filter(node -> node instanceof Label)
                        .findFirst()
                        .ifPresent(label -> {
                            cellContainer.getChildren().remove(label);
                            cellContainer.getChildren().add(label);
                        });
            }
        }
    }



    private StackPane getNodeByRowColumnIndex(int row, int column, GridPane gridPane) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(node);
            Integer nodeColumn = GridPane.getColumnIndex(node);

            if (nodeRow != null && nodeColumn != null && nodeRow == row && nodeColumn == column && node instanceof StackPane) {
                return (StackPane) node;
            }
        }
        return null;
    }

    private void openCommentDialog(LocalDate date) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Comment for " + date);
        dialog.setHeaderText("Enter your comment for " + date + ":");

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);

        // Retrieve existing comment from database
        String existingComment = getCommentFromDatabase(date);
        if (existingComment != null) {
            textArea.setText(existingComment);
        }

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                saveCommentToDatabase(date, textArea.getText());
            }
            return null;
        });

        dialog.showAndWait();
    }

    private String getCommentFromDatabase(LocalDate date) {
        String query = "SELECT comment FROM comments WHERE user_id = ? AND shift_date = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, 1); // Replace 1 with actual user ID if needed
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("comment");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveCommentToDatabase(LocalDate date, String comment) {
        String query = "INSERT INTO comments (user_id, shift_date, comment) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE comment = VALUES(comment)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, 1); // Replace 1 with actual user ID if needed
            stmt.setDate(2, Date.valueOf(date));
            stmt.setString(3, comment);
            stmt.executeUpdate();
            System.out.println("Comment saved for " + date);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
