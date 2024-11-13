package com.p3.menu;

import com.p3.menu.MenuDAO.Event;
import com.p3.session.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    public void initialize() {
        endShiftButton.setOnAction(event -> handleEndShift());
        logOutButton.setOnAction(event -> handleLogOut());
        breakButton.setOnAction(event -> handleBreakButton());

        startClock();
        loadTodaysEvents();
        setWelcomeText();
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
            System.out.println("Shift ended at " + java.time.LocalTime.now());

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

    }

    private void loadTodaysEvents() {
        int userId = Session.getCurrentUserId();
        MenuDAO menuDAO = new MenuDAO();
        List<Event> events = menuDAO.getTodaysEventsForUser(userId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Event event : events) {
            String formattedTime = event.getEventTime().format(formatter);
            String eventType = event.getEventType();

            Label eventLabel = new Label(eventType + " klokken " + formattedTime); //TODO lave enumerator til formatering af eventTypes
            eventLabel.getStyleClass().add("eventLabel"); //TODO

            notificationBox.getChildren().add(eventLabel);
        }
    }

    private void setWelcomeText() {
        String fullName = Session.getCurrentUserFullName();
        welcomeText.setText("Velkommen " + fullName);
    }
}