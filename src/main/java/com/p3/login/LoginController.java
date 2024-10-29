package com.p3.login;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private Label errorText;

    @FXML
    private TextField usernameField;

    @FXML
    private Button loginButton;

    @FXML
    private void initialize() {
        // Event handler for the Login button
        loginButton.setOnAction(event -> validateUser());
    }

    private void validateUser() {
        if (usernameField.getText().isEmpty()) {
            errorText.setVisible(true);
        }
    }
}