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
import java.time.LocalDate;
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

    private final MenuDAO menuDAO = new MenuDAO();  // TODO DAO skal ikke kunne tilgåes fra controller
    private final MenuService menuService = new MenuService();

    @FXML
    public void initialize() {
        endShiftButton.setOnAction(event -> handleEndShift());
        logOutButton.setOnAction(event -> handleLogOut());
        breakButton.setOnAction(event -> handleBreakButton());

        startClock();
        loadDailyEvents();
        initializeWelcomeText();
        initializeBreakButton();
        getMissedCheckout();
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

            boolean onBreak = menuService.getOnBreakStatus(userId);
            if (onBreak) {
                menuService.postBreakEndEvent(userId);
                menuService.setOnBreakStatus(userId, false);
            }

            menuService.postCheckOutEvent(userId);
            menuService.putClockedInStatusById(userId, false);

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
        boolean onBreak = menuService.getOnBreakStatus(userId);

        if (onBreak) {
            menuService.postBreakEndEvent(userId);
            menuService.setOnBreakStatus(userId, false);

            breakButton.getStyleClass().add("breakButton");
            breakButton.getStyleClass().remove("onBreakButton");
            breakButton.setText("Start Pause");
        } else {
            menuService.postBreakStartEvent(userId);
            menuService.setOnBreakStatus(userId, true);

            breakButton.getStyleClass().add("onBreakButton");
            breakButton.setText("Afslut Pause");
        }

        loadDailyEvents();
    }

    private void loadDailyEvents() {
        int userId = Session.getCurrentUserId();
        List<Event> events = menuService.getTodaysEventsForUser(userId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        notificationBox.getChildren().clear();

        for (Event event : events) {
            String formattedTime = event.getEventTime().format(formatter);
            String eventType = event.getEventType();
            String eventDisplay;
            String styleClass;

            // Combine formatting and styling logic
            switch (eventType) {
                case "check_in":
                    eventDisplay = "Check ind";
                    styleClass = "checkInNotification";
                    break;
                case "check_out":
                    eventDisplay = "Check ud";
                    styleClass = "checkOutNotification";
                    break;
                case "break_start":
                    eventDisplay = "Pause start";
                    styleClass = "breakStartNotification";
                    break;
                case "break_end":
                    eventDisplay = "Pause slut";
                    styleClass = "breakEndNotification";
                    break;
                default:
                    eventDisplay = eventType;
                    styleClass = "defaultNotification";
                    break;
            }

            Label eventLabel = new Label(eventDisplay + ": " + formattedTime);
            eventLabel.getStyleClass().add("eventLabel");
            eventLabel.getStyleClass().add(styleClass);

            notificationBox.getChildren().add(eventLabel);
        }
    }

    private void initializeWelcomeText() {
        String fullName = Session.getCurrentUserFullName();
        welcomeText.setText("Velkommen " + fullName);
    }

    private void initializeBreakButton() {
        int userId = Session.getCurrentUserId();
        boolean onBreak = menuService.getOnBreakStatus(userId);     // TODO Overvej om det skal være sessiondata?

        if (onBreak) {
            breakButton.getStyleClass().add("onBreakButton");
            breakButton.setText("Afslut Pause");
        } else {
            breakButton.getStyleClass().add("breakButton");
            breakButton.getStyleClass().remove("onBreakButton");
            breakButton.setText("Start Pause");
        }
    }

    private void getMissedCheckout() {
        int userId = Session.getCurrentUserId();
        MenuDAO.Event lastCheckOutEvent = menuService.getLastCheckOutEvent(userId);

        if (lastCheckOutEvent != null) {
            LocalTime key = LocalTime.of(23, 59, 0); // key = 23:59:00
            LocalTime lastCheckOutTime = lastCheckOutEvent.getEventTime().toLocalTime();

            if (lastCheckOutTime.equals(key)) {
                LocalDate missedShiftDate = lastCheckOutEvent.getEventTime().toLocalDate();

                boolean noteExists = menuService.checkIfNoteExists(userId, missedShiftDate);

                if (!noteExists) {
                    showMissedCheckoutModal(missedShiftDate);
                }
            }
        } else {
            System.out.println("No last check-out event found for user.");
        }
    }

    private void showMissedCheckoutModal(LocalDate missedShiftDate) {
        MenuService.showMissedCheckoutModal(missedShiftDate, note -> {
            int userId = Session.getCurrentUserId();
            menuService.postMissedCheckoutNote(userId, note, missedShiftDate);
        });
    }
}