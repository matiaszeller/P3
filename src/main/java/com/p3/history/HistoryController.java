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
            if (connection != null) {
                System.out.println("Connected to the database successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Database connection error:");
            e.printStackTrace();
        }
        return false;
    }


    void loadWeeklyGridPane() {
        GridPane gridPane = new GridPane();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)); // Start of week
        int startHour = 7;
        int endHour = calculateEndHour(weekStart); // Ensure this returns a valid value

        // Validate that endHour is greater than startHour
        if (endHour <= startHour) {
            System.err.println("Invalid hours range: startHour=" + startHour + ", endHour=" + endHour);
            return;
        }

        // Rows for each day
        for (int day = 0; day < 7; day++) {
            LocalDate date = weekStart.plusDays(day);

            // Add day label (e.g., "2024-11-21 (THURSDAY)")
            Label dayLabel = new Label(date + " (" + date.getDayOfWeek() + ")");
            dayLabel.setMinWidth(80);
            gridPane.add(dayLabel, 0, day + 1); // Column 0, Row day + 1

            // Add comment button
            Button commentButton = new Button("Tilføj kommentar");
            commentButton.setOnAction(e -> openCommentDialog(date));
            gridPane.add(commentButton, endHour - startHour + 2, day + 1); // Place after hour columns

            // Generate columns for hours
            for (int hour = startHour; hour < endHour; hour++) {
                int columnIndex = hour - startHour + 1;

                // Validate columnIndex to prevent adding at invalid positions
                if (columnIndex < 0) {
                    System.err.println("Invalid columnIndex: " + columnIndex + " for hour: " + hour);
                    continue; // Skip invalid cells
                }

                // Create a white cell with a border
                Rectangle cell = new Rectangle(50, 50);
                cell.setFill(Color.WHITE);
                cell.setStroke(Color.GRAY);

                // Add time label to the cell (e.g., "07:00")
                Label timeLabel = new Label(String.format("%02d:00", hour));
                timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: black;");

                // Create a StackPane to hold the cell and time label
                StackPane cellContainer = new StackPane();
                cellContainer.getChildren().addAll(cell, timeLabel);
                StackPane.setAlignment(timeLabel, Pos.TOP_LEFT);
                StackPane.setMargin(timeLabel, new Insets(2, 0, 0, 2));

                // Add the cell container to the grid
                gridPane.add(cellContainer, columnIndex, day + 1);
            }
        }

        // Apply colors to the grid based on weekly events
        applyWeeklyEventColors(gridPane, weekStart);

        // Clear and update the content pane with the grid
        contentPane.getChildren().clear();
        contentPane.getChildren().add(gridPane);
    }






    public void applyWeeklyEventColors(GridPane gridPane, LocalDate weekStart) {
        String query = "SELECT user_id, shift_date, event_time, event_type FROM timelog " +
                "WHERE shift_date BETWEEN ? AND ? ORDER BY shift_date, event_time";

        int totalMinutesWorked = 0;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            LocalDate weekEnd = weekStart.plusDays(6);
            stmt.setDate(1, Date.valueOf(weekStart));
            stmt.setDate(2, Date.valueOf(weekEnd));
            ResultSet rs = stmt.executeQuery();

            Map<LocalDate, LocalDateTime> checkInMap = new HashMap<>();
            Map<LocalDate, LocalDateTime> breakStartMap = new HashMap<>();
            Map<LocalDate, Integer> dailyWorkMinutes = new HashMap<>();

            while (rs.next()) {
                LocalDate shiftDate = rs.getDate("shift_date").toLocalDate();
                LocalDateTime eventTime = rs.getTimestamp("event_time").toLocalDateTime();
                String eventType = rs.getString("event_type");

                int row = shiftDate.getDayOfWeek().getValue() % 8; // Consistent row mapping

                switch (eventType) {
                    case "check_in":
                        checkInMap.put(shiftDate, eventTime);
                        break;

                    case "break_start":
                        LocalDateTime checkInTime = checkInMap.get(shiftDate);
                        if (checkInTime != null) {
                            colorCells(gridPane, row, checkInTime, eventTime, Color.GREEN);
                            int workedMinutes = (int) Duration.between(checkInTime, eventTime).toMinutes();
                            dailyWorkMinutes.put(shiftDate, dailyWorkMinutes.getOrDefault(shiftDate, 0) + workedMinutes);
                            checkInMap.remove(shiftDate);
                        }
                        breakStartMap.put(shiftDate, eventTime);
                        break;

                    case "break_end":
                        LocalDateTime breakStartTime = breakStartMap.get(shiftDate);
                        if (breakStartTime != null) {
                            colorCells(gridPane, row, breakStartTime, eventTime, Color.ORANGE);
                            breakStartMap.remove(shiftDate);
                        }
                        checkInMap.put(shiftDate, eventTime);
                        break;

                    case "check_out":
                        LocalDateTime activeCheckInTime = checkInMap.get(shiftDate);
                        if (activeCheckInTime != null) {
                            colorCells(gridPane, row, activeCheckInTime, eventTime, Color.GREEN);
                            int workedMinutes = (int) Duration.between(activeCheckInTime, eventTime).toMinutes();
                            dailyWorkMinutes.put(shiftDate, dailyWorkMinutes.getOrDefault(shiftDate, 0) + workedMinutes);
                            checkInMap.remove(shiftDate);
                        }
                        break;
                }
            }

            // Handle incomplete check-ins (mark cells red)
            System.out.println("Check-In Map before processing missing checkouts: " + checkInMap);
            for (Map.Entry<LocalDate, LocalDateTime> entry : checkInMap.entrySet()) {
                LocalDate shiftDate = entry.getKey();
                LocalDateTime checkInTime = entry.getValue();
                int row = shiftDate.getDayOfWeek().getValue() % 8;

                LocalDateTime endOfDay = checkInTime.toLocalDate().atTime(23, 59);
                int missingMinutes = (int) Duration.between(checkInTime, endOfDay).toMinutes();
                dailyWorkMinutes.put(shiftDate, dailyWorkMinutes.getOrDefault(shiftDate, 0) + missingMinutes);
                totalMinutesWorked += missingMinutes;

                colorCells(gridPane, row, checkInTime, endOfDay, Color.RED);
            }

            // Update the total hours worked label
            for (int minutes : dailyWorkMinutes.values()) {
                totalMinutesWorked += minutes;
            }
            int totalHours = totalMinutesWorked / 60;
            int remainingMinutes = totalMinutesWorked % 60;
            totalHoursLabel.setText(String.format("Samlet timer denne uge: %d:%02d", totalHours, remainingMinutes));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }







    private int calculateEndHour(LocalDate weekStart) {
        String query = "SELECT MAX(HOUR(event_time)) AS max_hour FROM timelog WHERE shift_date BETWEEN ? AND ?";
        int defaultEndHour = 24;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            LocalDate weekEnd = weekStart.plusDays(6);
            stmt.setDate(1, Date.valueOf(weekStart));
            stmt.setDate(2, Date.valueOf(weekEnd));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int maxHour = rs.getInt("max_hour");
                if (!rs.wasNull()) {
                    return maxHour;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.err.println("No events found for the week. Defaulting endHour to 24.");
        return defaultEndHour;
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
                Rectangle baseCell = cellContainer.getChildren().isEmpty() ? null : (Rectangle) cellContainer.getChildren().get(0);

                if (baseCell != null) {
                    double startFraction = (hour == startHour) ? startMinute / 60.0 : 0;
                    double endFraction = (hour == endHour) ? endMinute / 60.0 : 1;
                    double fillWidth = baseCell.getWidth() * (endFraction - startFraction);

                    // Create the overlay rectangle
                    Rectangle overlayRect = new Rectangle(fillWidth, baseCell.getHeight());
                    overlayRect.setFill(color);
                    overlayRect.setTranslateX(baseCell.getWidth() * startFraction - baseCell.getWidth() / 2 + fillWidth / 2);

                    // Remove any existing overlay of the same color
                    cellContainer.getChildren().removeIf(node -> node instanceof Rectangle && ((Rectangle) node).getFill().equals(color));
                    cellContainer.getChildren().add(overlayRect);

                    // Re-add the time label to ensure it stays on top
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
                String newComment = textArea.getText().trim();
                if (!newComment.isEmpty() || existingComment != null) {
                    saveCommentToDatabase(date, newComment);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private String getCommentFromDatabase(LocalDate date) {
        String query = "SELECT written_note FROM note WHERE writer_id = ? AND note_date = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, 1); // Replace with the actual user ID if needed
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("written_note");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveCommentToDatabase(LocalDate date, String comment) {
        String deleteQuery = "DELETE FROM note WHERE writer_id = ? AND note_date = ?";
        String insertQuery = "INSERT INTO note (note_date, writer_id, recipient_id, full_name, written_note) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            if (comment == null || comment.isEmpty()) {
                // If the comment is empty, delete the existing record
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, 1); // Replace with the actual writer ID if needed
                    stmt.setDate(2, Date.valueOf(date));
                    int rowsDeleted = stmt.executeUpdate();
                    if (rowsDeleted > 0) {
                        System.out.println("Comment deleted for " + date);
                    } else {
                        System.out.println("No comment found to delete for " + date);
                    }
                }
            } else {
                // Insert or update the comment
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                     PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    // Delete existing comment first
                    deleteStmt.setInt(1, 1);
                    deleteStmt.setDate(2, Date.valueOf(date));
                    deleteStmt.executeUpdate();

                    // Insert new comment
                    insertStmt.setDate(1, Date.valueOf(date));
                    insertStmt.setInt(2, 1); // Replace with the actual writer ID if needed
                    insertStmt.setInt(3, 1); // Replace with the actual recipient ID if needed
                    insertStmt.setString(4, "Writer Full Name"); // Replace with the writer's full name
                    insertStmt.setString(5, comment);
                    insertStmt.executeUpdate();
                    System.out.println("Comment saved for " + date);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
