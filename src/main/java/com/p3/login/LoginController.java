package com.p3.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.IOException;
import javafx.geometry.Insets;

public class LoginController {
    @FXML
    private Label errorText;

    @FXML
    private TextField usernameField;

    @FXML
    private Button loginButton;

    @FXML
    private VBox managerModal;

    private String managerUsername;
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
                showManagerModal(usernameField.getText());
            } else if ("employee".equalsIgnoreCase(employmentRole)) {
                loadMenuPage();
            }
        }
    }

    private void loadMenuPage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.p3.menu/MenuPage.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        // !!!!! Lige nu er koden til 'brain' som er manager: admin. 
    private void showManagerModal(String username) {
        this.managerUsername = username;

        // Create modal elements
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Manager Login");

        // Disable window resizing
        modalStage.setResizable(false);

        Label instructionLabel = new Label("Enter Manager Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button submitButton = new Button("Submit");
        Button cancelButton = new Button("Cancel");
        Label modalErrorLabel = new Label("Invalid password. Try again.");
        modalErrorLabel.setStyle("-fx-text-fill: red;");
        modalErrorLabel.setVisible(false);

        VBox vbox = new VBox(10, instructionLabel, passwordField, new HBox(10, submitButton, cancelButton), modalErrorLabel);
        vbox.setPadding(new Insets(10));

        submitButton.setOnAction(event -> handleSubmit(modalStage, passwordField.getText(), modalErrorLabel));
        cancelButton.setOnAction(event -> modalStage.close());

        Scene modalScene = new Scene(vbox);


        modalScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Add style classes to nodes
        submitButton.getStyleClass().add("confirmButton");
        cancelButton.getStyleClass().add("defaultButton");
        modalErrorLabel.getStyleClass().add("errorText");
        passwordField.getStyleClass().add("usernameField");

        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }


    @FXML
    private void handleCancel() {
        managerModal.setVisible(false);
    }

    private void handleSubmit(Stage modalStage, String password, Label modalErrorLabel) {
        if (loginService.validateManager(managerUsername, password)) {
            modalStage.close();
            loadMenuPage();
        } else {
            modalErrorLabel.setVisible(true);
        }
    }
}