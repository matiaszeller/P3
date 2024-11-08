package com.p3.menu;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MenuController {

    @FXML
    private Button endShiftButton;
    @FXML
    private Button logOutButton;
    @FXML
    private Button breakButton;
    @FXML
    private Label clock;  // The new label for the clock

    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        endShiftButton.setOnAction(event -> handleEndShift());
        logOutButton.setOnAction(event -> handleLogOut());
        breakButton.setOnAction(event -> handleStartBreak());

        startClock();
    }

    private void startClock() {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> updateClock());
        clockTimeline = new Timeline(keyFrame);
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
            Stage stage = (Stage) endShiftButton.getScene().getWindow();
            MenuService.loadLoginPage(stage);
        }
    }

    private void handleLogOut() {
        Stage stage = (Stage) logOutButton.getScene().getWindow();
        MenuService.loadLoginPage(stage);
    }

    private void handleStartBreak() {

    }

}