package com.p3.menu;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Objects;
import java.util.Optional;

public class MenuService {

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
    public static void loadAdminPage(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MenuService.class.getResource("/com.p3.administration/Administration.fxml"));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
