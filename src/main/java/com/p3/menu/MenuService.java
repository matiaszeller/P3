package com.p3.menu;

import com.p3.event.Event;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class MenuService {
    MenuDAO menuDao = new MenuDAO();

    public static boolean showEndShiftConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(Objects.requireNonNull(MenuService.class.getResourceAsStream("/icons/favicon.png"))));
        alert.setTitle("");
        alert.setHeaderText(null);
        Label contentLabel = new Label("Er du sikker på at du vil afslutte din vagt?");
        contentLabel.getStyleClass().add("modalText");
        alert.getDialogPane().setContent(contentLabel);


        ButtonType confirmButton = new ButtonType("Bekræft");
        ButtonType cancelButton = new ButtonType("Annuller");
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(MenuService.class.getResource("/style.css")).toExternalForm());
        alert.getDialogPane().lookupButton(confirmButton).getStyleClass().add("confirmButton");
        alert.getDialogPane().lookupButton(cancelButton).getStyleClass().add("defaultButton");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }

    public boolean getOnBreakStatus(int userId){
        String jsonResponse = menuDao.getOnBreakStatus(userId);
        org.json.JSONObject jsonObject = new org.json.JSONObject(jsonResponse);
        return jsonObject.getBoolean("on_break");
    }

    public void setOnBreakStatus(int userId, boolean onBreak){
        menuDao.setOnBreakStatus(userId, onBreak);
    }

    public void postBreakStartEvent(int userId){
        menuDao.postBreakStartEvent(userId);
    }

    public void postBreakEndEvent(int userId){
        menuDao.postBreakEndEvent(userId);
    }

    public List<Event> getTodaysEventsForUser(int userId){
        LocalDate today = LocalDate.now();
        JSONArray timelogs = new JSONArray(menuDao.getTodaysEventsForUser(userId, today));

        List<Event> events = new ArrayList<>();

        for(int i = 0; i < timelogs.length(); i++){     // For each object in array, we take the eventTime and eventType and create a new event object, adding it to the return list
            JSONObject timelog = timelogs.getJSONObject(i);
            LocalDateTime eventTime = LocalDateTime.parse(timelog.getString("event_time"));
            String eventType = timelog.getString("event_type");
            Event event = new Event(eventTime, eventType);

            events.add(event);
        }

        return events;
    }

    public void postCheckOutEvent(int userId){
        menuDao.postCheckOutEvent(userId);
    }

    public void putClockedInStatusById(int userId, boolean status){
        menuDao.putClockedInStatusById(userId, status);
    }

    public Event getLastCheckOutEvent(int userId) {
        String jsonResponse = menuDao.getLastCheckOutEvent(userId);

        if (jsonResponse == null || jsonResponse.isEmpty() || jsonResponse.equals("null")) {
            return null;
        }

        JSONObject jsonObject = new JSONObject(jsonResponse);
        LocalDateTime eventTime = LocalDateTime.parse(jsonObject.getString("event_time"));
        String eventType = jsonObject.getString("event_type");
        Event event = new Event(eventTime, eventType);

        return event;
    }

    public void postMissedCheckoutNote(int userId, String note, LocalDate missedShiftDate) {
        menuDao.postMissedCheckoutNote(userId, note, missedShiftDate);
    }

    public static void showMissedCheckoutModal(LocalDate date, Consumer<String> onSubmit) {
        Dialog<String> dialog = new Dialog<>();
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(Objects.requireNonNull(MenuService.class.getResourceAsStream("/icons/favicon.png"))));

        dialog.setTitle("Glemt vagtafslutning");
        dialog.setHeaderText("Du glemte at afslutte din vagt d. " + date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        ButtonType submitButtonType = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Ignorer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(submitButtonType).getStyleClass().add("confirmButton");
        dialog.getDialogPane().lookupButton(cancelButtonType).getStyleClass().add("defaultButton");

        TextArea noteTextArea = new TextArea();
        noteTextArea.setPromptText("Skriv din note her");

        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Skriv venligst tidspunktet du gik:"), noteTextArea);
        dialog.getDialogPane().setContent(content);

        Node submitButton = dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.setDisable(true);

        noteTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            submitButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(MenuService.class.getResource("/style.css")).toExternalForm());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return noteTextArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(note -> {
            onSubmit.accept(note);
        });
    }

    public boolean checkIfNoteExists(int userId, LocalDate missedShiftDate) {
        String response = menuDao.noteExistsForDate(userId, missedShiftDate);
        return Boolean.parseBoolean(response);
    }
}
