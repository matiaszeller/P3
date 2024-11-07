package com.p3.menu;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class MenuService {

    public static boolean showEndShiftConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText("Er du sikker på at du vile afslutte din vagt?");

        ButtonType confirmButton = new ButtonType("Bekræft");
        ButtonType cancelButton = new ButtonType("Annuller");
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        alert.getDialogPane().getStylesheets().add(MenuService.class.getResource("/style.css").toExternalForm());
        alert.getDialogPane().lookupButton(confirmButton).getStyleClass().add("confirmButton");
        alert.getDialogPane().lookupButton(cancelButton).getStyleClass().add("defaultButton");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }

}
