package com.p3.menu;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public static void loadLoginPage(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MenuService.class.getResource("/com.p3.login/LoginPage.fxml"));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene loginScene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(loginScene);

            stage.setTitle("Time Registration System");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public List<MenuDAO.Event> getTodaysEventsForUser(int userId){
        LocalDate today = LocalDate.now();

        JSONObject jsonResponse = new JSONObject(menuDao.getTodaysEventsForUser(userId, today));
        JSONArray timelogs = jsonResponse.getJSONArray("timelogs");

        List<MenuDAO.Event> events = new ArrayList<>();

        for(int i = 0; i < timelogs.length(); i++){     // For each object in array, we take the eventTime and eventType and create a new event object, adding it to the return list
            JSONObject timelog = timelogs.getJSONObject(i);
            LocalDateTime eventTime = LocalDateTime.parse(timelog.getString("event_time"));
            String eventType = timelog.getString("event_type");
            MenuDAO.Event event = new MenuDAO.Event(eventTime, eventType);

            events.add(event);
        }
        return events;
    }

    public void postCheckOutEvent(int user_id){
        menuDao.postCheckOutEvent(user_id);
    }

    public void putClockedInStatusById(int userId, boolean status){
        menuDao.putClockedInStatusById(userId, status);
    }
}
