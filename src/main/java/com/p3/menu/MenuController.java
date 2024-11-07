package com.p3.menu;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MenuController {

    @FXML
    private Button endShiftButton;

    @FXML
    public void initialize() {
        // Set up the action for the "Afslut Vagt" button
        endShiftButton.setOnAction(event -> handleEndShift());
    }

    private void handleEndShift() {
        boolean confirmed = MenuService.showEndShiftConfirmation();
        if (confirmed) {
            // Logic to end the shift, for example:
            System.out.println("Shift ended at " + java.time.LocalTime.now());
            // You could add more logic here as required, like saving shift data.
        } else {
            System.out.println("Shift end cancelled.");
        }
    }
}
