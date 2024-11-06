package com.p3.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {
    @FXML
    private Label errorText;

    @FXML
    private TextField usernameField;

    @FXML
    private Button loginButton;

    private final LoginService loginService = new LoginService();

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> handleLogin());
    }

    private void handleLogin() {
        RoleHolder roleHolder = new RoleHolder();
        boolean isValid = loginService.validateUser(usernameField.getText(), roleHolder);

        if (!isValid) {
            errorText.setVisible(true);
        } else {
            String employmentRole = roleHolder.getEmploymentRole();
            if ("manager".equalsIgnoreCase(employmentRole)) {
                loadManagerModal();
            } else if ("employee".equalsIgnoreCase(employmentRole)) {
                loadMenuPage();
            }
        }
    }

    private void loadMenuPage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.p3.menu/MenuPage.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1920, 1040);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadManagerModal() {

    }
}