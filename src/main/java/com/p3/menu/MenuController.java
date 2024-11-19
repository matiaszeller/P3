package com.p3.menu;

import com.p3.menu.MenuDAO.Event;
import com.p3.session.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MenuController {

    @FXML
    private Button endShiftButton;
    @FXML
    private Button logOutButton;
    @FXML
    private Button breakButton;
    @FXML
    private Label clock;
    @FXML
    private Label welcomeText;
    @FXML
    private VBox notificationBox;
    @FXML
    private Button managerButton;

    private final MenuDAO menuDAO = new MenuDAO();

    @FXML
    public void initialize() {
        endShiftButton.setOnAction(event -> handleEndShift());
        logOutButton.setOnAction(event -> handleLogOut());
        breakButton.setOnAction(event -> handleBreakButton());
        managerButton.setOnAction(event -> handleOnPressManager());
        startClock();
        loadDailyEvents();
        initializeWelcomeText();
        initializeBreakButton();
    }

    private void startClock() {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> updateClock());
        Timeline clockTimeline = new Timeline(keyFrame);
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    private void updateClock() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = currentTime.format(formatter);

        clock.setText(formattedTime);
    }

    private void handleEndShift() {
        boolean confirmed = MenuService.showEndShiftConfirmation();
        if (confirmed) {
            int userId = Session.getCurrentUserId();
            LocalDateTime currentTime = LocalDateTime.now();

            menuDAO.insertCheckOutEvent(userId, currentTime);
            menuDAO.setClockedInStatusById(userId, false);
            menuDAO.setOnBreakStatus(userId, false);

            Session.clearSession();

            Stage stage = (Stage) endShiftButton.getScene().getWindow();
            MenuService.loadLoginPage(stage);
        }
    }

    private void handleLogOut() {
        Session.clearSession();

        Stage stage = (Stage) logOutButton.getScene().getWindow();
        MenuService.loadLoginPage(stage);
    }

    private void handleBreakButton() {
        int userId = Session.getCurrentUserId();
        boolean onBreak = menuDAO.getOnBreakStatus(userId);
        LocalDateTime currentTime = LocalDateTime.now();

        if (onBreak) {
            menuDAO.insertBreakEndEvent(userId, currentTime);
            menuDAO.setOnBreakStatus(userId, false);

            breakButton.getStyleClass().add("breakButton");
            breakButton.getStyleClass().remove("onBreakButton");
            breakButton.setText("Start Pause");
        } else {
            menuDAO.insertBreakStartEvent(userId, currentTime);
            menuDAO.setOnBreakStatus(userId, true);

            breakButton.getStyleClass().add("onBreakButton");
            breakButton.setText("Afslut Pause");
        }

        loadDailyEvents();
    }

    private void loadDailyEvents() {
        int userId = Session.getCurrentUserId();
        List<Event> events = menuDAO.getTodaysEventsForUser(userId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        notificationBox.getChildren().clear();

        for (Event event : events) {
            String formattedTime = event.getEventTime().format(formatter);
            String eventType = event.getEventType();

            String eventDisplay = formatEventType(eventType);

            Label eventLabel = new Label(eventDisplay + " klokken " + formattedTime);
            eventLabel.getStyleClass().add("eventLabel");

            notificationBox.getChildren().add(eventLabel);
        }
    }

    private String formatEventType(String eventType) {
        switch (eventType) {
            case "check_in":
                return "Check-ind";
            case "check_out":
                return "Check-ud";
            case "break_start":
                return "Pause start";
            case "break_end":
                return "Pause slut";
            default:
                return eventType;
        }
    }

    private void initializeWelcomeText() {
        String fullName = Session.getCurrentUserFullName();
        welcomeText.setText("Velkommen " + fullName);
    }

    private void initializeBreakButton() {
        int userId = Session.getCurrentUserId();
        boolean onBreak = menuDAO.getOnBreakStatus(userId);

        if (onBreak) {
            breakButton.getStyleClass().add("onBreakButton");
            breakButton.setText("Afslut Pause");
        } else {
            breakButton.getStyleClass().add("breakButton");
            breakButton.getStyleClass().remove("onBreakButton");
            breakButton.setText("Start Pause");
        }
    }
    private void loadManagerPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("com/p3/managerDaily/ManagerDaily.fxml"));
            Stage stage = (Stage) managerButton.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(loader.load(), width, height);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleOnPressManager(){
        loadManagerPage(); //xD
    }
}