package com.p3.history;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HistoryController {

    @FXML
    private HBox gridPaneContainer;

    @FXML
    private Button historyButton;

    @FXML
    private Button returnButton;

    @FXML
    private Label totalHoursLabel;

    private final HistoryService historyService = new HistoryService();

    public static void showHistoryPage(Stage stage) {
        try {
            URL fxmlLocation = HistoryController.class.getResource("/com.p3.historyFxml/HistoryPage.fxml");
            if (fxmlLocation == null) {
                System.out.println("FXML file not found at the specified path.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);

            // Get the current stage's width and height for flexibility
            double width = stage.getWidth();
            double height = stage.getHeight();

            // Create the scene with the current stage dimensions
            Scene scene = new Scene(fxmlLoader.load(), width, height);

            // Set the scene and make the stage fullscreen for flexibility
            stage.setScene(scene);
            stage.setFullScreen(true); // Change to true if you want fullscreen behavior
            stage.setResizable(true); // Allow resizing
            // Show the stage
            stage.show();

            // Initialize the controller and load dynamic content
            HistoryController controller = fxmlLoader.getController();
            controller.loadWeeklyGridPane();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void initialize() {
        historyButton.setOnAction(event -> loadWeeklyGridPane());
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

    void loadWeeklyGridPane() {
        GridPane gridPane = new GridPane();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        int userId = 1; // Replace this with the actual user ID

        int startHour = 7;
        int endHour = calculateEndHour(weekStart, weekEnd, userId);

        if (endHour <= startHour) {
            System.err.println("Invalid hours range: startHour=" + startHour + ", endHour=" + endHour);
            return;
        }

        for (int day = 0; day < 7; day++) {
            LocalDate date = weekStart.plusDays(day);

            // Define a formatter for Danish day names
            DateTimeFormatter danishFormatter = DateTimeFormatter.ofPattern("EEEE", new Locale("da", "DK"));

// Format the day name in Danish and capitalize the first letter
            String dayNameInDanish = date.format(danishFormatter);
            dayNameInDanish = dayNameInDanish.substring(0, 1).toUpperCase() + dayNameInDanish.substring(1);

// Add the day label to the first column
            Label dayLabel = new Label(date + " (" + dayNameInDanish + ")");
            dayLabel.setMinWidth(150); // Adjust for better visibility
            dayLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            gridPane.add(dayLabel, 0, day + 1);


            // Create a StackPane to mimic a dialog
            StackPane commentBox = new StackPane();
            commentBox.setPrefSize(200, 70); // Adjust size as needed
            commentBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-background-color: white;");

            // Add a TextArea for the comment
            TextArea commentArea = new TextArea();
            commentArea.setPromptText("Skriv din kommentar her...");
            commentArea.setWrapText(true);
            commentArea.setPrefSize(180, 70);
            commentArea.setStyle("-fx-background-color: white; -fx-border-color: transparent;");

            // Add the "Tilføj kommentar" button
            Button commentButton = new Button("+");
            commentButton.setPrefSize(30, 30); // Adjust size for the button
            commentButton.setStyle("-fx-background-radius: 15; -fx-border-color: lightgray; -fx-border-radius: 15;");
            commentButton.setOnAction(event -> {
                System.out.println("Comment button clicked for " + date);
                // Add your logic here to save comments if needed
            });

            // Align the button inside the StackPane
            StackPane.setAlignment(commentButton, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(commentButton, new Insets(5)); // Add spacing for the button

            // Add TextArea and button to the StackPane
            commentBox.getChildren().addAll(commentArea, commentButton);

            // Add the StackPane to the grid
            gridPane.add(commentBox, endHour - startHour + 2, day + 1);

            // Add hour cells to the grid
            for (int hour = startHour; hour < endHour; hour++) {
                int columnIndex = hour - startHour + 1;

                if (columnIndex < 0) {
                    System.err.println("Invalid columnIndex: " + columnIndex + " for hour: " + hour);
                    continue;
                }

                // Create a cell rectangle
                Rectangle cell = new Rectangle(70, 70);
                cell.setFill(Color.WHITE);
                cell.setStroke(Color.GRAY);

                // Add time label to the cell
                Label timeLabel = new Label(String.format("%02d:00", hour));
                timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: black;");

                // Add the cell and label to a StackPane
                StackPane cellContainer = new StackPane();
                cellContainer.getChildren().addAll(cell, timeLabel);
                StackPane.setAlignment(timeLabel, Pos.TOP_LEFT);
                StackPane.setMargin(timeLabel, new Insets(2, 0, 0, 2));

                // Add the StackPane to the GridPane
                gridPane.add(cellContainer, columnIndex, day + 1);
            }
        }

        // Apply weekly event colors
        applyWeeklyEventColors(gridPane, weekStart, weekEnd, userId);

        // Add the GridPane to the container
        gridPaneContainer.getChildren().clear();
        gridPaneContainer.getChildren().add(gridPane);
    }


    public void applyWeeklyEventColors(GridPane gridPane, LocalDate weekStart, LocalDate weekEnd, int userId) {
        // Retrieve timelog events from the database using getWeeklyTimelogEvents
        Map<LocalDate, Map<String, LocalDateTime>> timelogEvents = historyService.getWeeklyTimelogEvents(weekStart, weekEnd, userId);
        int totalMinutesWorked = 0;
        Map<LocalDate, Integer> dailyWorkMinutes = new HashMap<>();

        Color customGreen = Color.web("#20C997");
        Color customRed = Color.web("#EE7572");
        Color customOrange = Color.web("#F9D58B");
        // Iterate through the events for each day
        for (Map.Entry<LocalDate, Map<String, LocalDateTime>> entry : timelogEvents.entrySet()) {
            LocalDate date = entry.getKey();
            Map<String, LocalDateTime> events = entry.getValue();

            // Define variables for each event
            LocalDateTime checkInTime = events.get("check_in");
            LocalDateTime breakStartTime = events.get("break_start");
            LocalDateTime breakEndTime = events.get("break_end");
            LocalDateTime checkOutTime = events.get("check_out");

            int row = date.getDayOfWeek().getValue() % 8;

            // Track the last event time to determine the next step in coloring
            LocalDateTime lastEventTime = null;

            // Handle "check_in" event
            if (checkInTime != null) {
                lastEventTime = checkInTime;
            }

            // Handle "break_start" and "break_end" events
            if (breakStartTime != null && breakEndTime != null) {
                // Color cells green from last event to break_start
                if (lastEventTime != null && lastEventTime.isBefore(breakStartTime)) {
                    colorCells(gridPane, row, lastEventTime, breakStartTime, customGreen);
                    int workedMinutes = (int) Duration.between(lastEventTime, breakStartTime).toMinutes();
                    dailyWorkMinutes.put(date, dailyWorkMinutes.getOrDefault(date, 0) + workedMinutes);
                }

                // Color cells orange from break_start to break_end
                colorCells(gridPane, row, breakStartTime, breakEndTime, customOrange);
                lastEventTime = breakEndTime;
            }

            // Handle the case where there is no break_end, or we continue after the break
            if (breakEndTime != null && checkOutTime != null) {
                // Color cells green from break_end to check_out
                colorCells(gridPane, row, breakEndTime, checkOutTime, customGreen);
                int workedMinutes = (int) Duration.between(breakEndTime, checkOutTime).toMinutes();
                dailyWorkMinutes.put(date, dailyWorkMinutes.getOrDefault(date, 0) + workedMinutes);
                lastEventTime = checkOutTime;
            }

            // Handle "check_out" event if there's no break
            if (lastEventTime != null && checkOutTime != null && lastEventTime.isBefore(checkOutTime)) {
                colorCells(gridPane, row, lastEventTime, checkOutTime, customGreen);
                int workedMinutes = (int) Duration.between(lastEventTime, checkOutTime).toMinutes();
                dailyWorkMinutes.put(date, dailyWorkMinutes.getOrDefault(date, 0) + workedMinutes);
                lastEventTime = checkOutTime;
            }

            // Handle missing "check_out" - mark red from the last known event to end of day
            if (lastEventTime != null && checkOutTime == null) {
                LocalDateTime endOfDay = lastEventTime.toLocalDate().atTime(23, 59);
                colorCells(gridPane, row, lastEventTime, endOfDay, customRed);
                int missingMinutes = (int) Duration.between(lastEventTime, endOfDay).toMinutes();
                dailyWorkMinutes.put(date, dailyWorkMinutes.getOrDefault(date, 0) + missingMinutes);
            }
        }

        // Calculate total hours worked
        totalMinutesWorked = dailyWorkMinutes.values().stream().mapToInt(Integer::intValue).sum();
        int totalHours = totalMinutesWorked / 60;
        int remainingMinutes = totalMinutesWorked % 60;
        totalHoursLabel.setText(String.format("Total Ugentlig Arbejdstid: \n%d:%02d Timer", totalHours, remainingMinutes));
    }





    private void colorCells(GridPane gridPane, int row, LocalDateTime start, LocalDateTime end, Color color) {
        int startHour = start.getHour();
        int endHour = end.getHour();
        int startMinute = start.getMinute();
        int endMinute = end.getMinute();

        for (int hour = startHour; hour <= endHour; hour++) {
            int col = hour - 7 + 1;  // Assuming 7 AM is the start hour for the grid

            // Find the cell in the grid for the given row and column
            StackPane cellContainer = getNodeByRowColumnIndex(row, col, gridPane);
            if (cellContainer != null) {
                Rectangle baseCell = null;
                for (javafx.scene.Node node : cellContainer.getChildren()) {
                    if (node instanceof Rectangle && !((Rectangle) node).getFill().equals(color)) {
                        baseCell = (Rectangle) node;
                        break;
                    }
                }

                if (baseCell != null) {
                    // Calculate the fractional width of the overlay based on start and end times
                    double startFraction = (hour == startHour) ? startMinute / 60.0 : 0;
                    double endFraction = (hour == endHour) ? endMinute / 60.0 : 1;
                    double fillWidth = baseCell.getWidth() * (endFraction - startFraction);

                    // Create an overlay rectangle for the time segment to be colored
                    Rectangle overlayRect = new Rectangle(fillWidth, baseCell.getHeight());
                    overlayRect.setFill(color);

                    // Position the overlay within the cell
                    if (hour == startHour) {
                        overlayRect.setTranslateX(baseCell.getWidth() * startFraction - baseCell.getWidth() / 2 + fillWidth / 2);
                    } else {
                        overlayRect.setTranslateX(-baseCell.getWidth() / 2 + fillWidth / 2);
                    }

                    // Remove overlays with the same color after collecting them
                    javafx.scene.Node[] toRemove = cellContainer.getChildren().stream()
                            .filter(node -> node instanceof Rectangle && ((Rectangle) node).getFill().equals(color))
                            .toArray(javafx.scene.Node[]::new);
                    for (javafx.scene.Node node : toRemove) {
                        cellContainer.getChildren().remove(node);
                    }

                    // Add the new overlay to the cell
                    cellContainer.getChildren().add(overlayRect);

                    // Ensure the time label stays on top of the overlays
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

    public int calculateEndHour(LocalDate weekStart, LocalDate weekEnd, int userId) {
        int defaultEndHour = 24; // Default if API fails
        try {
            String urlString = String.format("http://localhost:8080/api/timelog/calculateMaxEndHour?userId=%d&weekStart=%s&weekEnd=%s",
                    userId,
                    weekStart.format(DateTimeFormatter.ISO_DATE),
                    weekEnd.format(DateTimeFormatter.ISO_DATE));
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line);
                }

                // Parse the JSON response to extract the maxHour
                JSONObject jsonResponse = new JSONObject(output.toString());
                return jsonResponse.getInt("maxHour");
            } else {
                System.err.println("Failed to get the max hour from server. HTTP code: " + conn.getResponseCode());
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.err.println("No valid response. Defaulting endHour to " + defaultEndHour);
        return defaultEndHour;
    }

//    private void openCommentDialog(LocalDate date) {
//        Dialog<Void> dialog = new Dialog<>();
//        dialog.setTitle("Add Comment for " + date);
//        dialog.setHeaderText("Enter your comment for " + date + ":");
//
//        TextArea textArea = new TextArea();
//        textArea.setWrapText(true);
//
//        // Load existing comment from the server
//        String existingComment = historyService.getCommentFromServer(date, 1); // Replace `1` with the actual user ID
//        if (existingComment != null) {
//            textArea.setText(existingComment);
//        }
//
//        dialog.getDialogPane().setContent(textArea);
//        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
//
//        dialog.setResultConverter(dialogButton -> {
//            if (dialogButton == ButtonType.OK) {
//                String newComment = textArea.getText().trim();
//                if (!newComment.isEmpty() || existingComment != null) {
//                    boolean success = historyService.saveCommentToServer(date, newComment, 1); // Replace `1` with the actual user ID
//                    if (!success) {
//                        showErrorDialog("Failed to save comment. Please try again.");
//                    }
//                }
//            }
//            return null;
//        });
//
//        dialog.showAndWait();
//    }
//
//    private void showErrorDialog(String message) {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setTitle("Error");
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }

}
