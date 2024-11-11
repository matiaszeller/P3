package com.p3.login;

import com.p3.instance.AppInstance;
import com.p3.login.LoginService;
import com.p3.menu.MenuService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;
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
    private String employeeUsername;
    private final LoginService loginService = new LoginService();

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> handleLogin());
    }

    private void handleLogin() {
        String role = loginService.validateUser(usernameField.getText());

        if (role == null) {
            errorText.setVisible(true);
        } else {
            if ("manager".equalsIgnoreCase(role)) {
                showManagerModal(usernameField.getText());
            } else if ("employee".equalsIgnoreCase(role)) {
                showEmployeeModal(usernameField.getText());
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

    private void handleSubmit(Stage modalStage, String password, Label modalErrorLabel) {
        if (loginService.validateManager(managerUsername, password)) {
            modalStage.close();
            loadMenuPage();
        } else {
            modalErrorLabel.setVisible(true);
        }
    }

    private void showEmployeeModal(String username) {
        MenuService menuService = new MenuService();
        this.employeeUsername = username;

        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Notification");

        modalStage.setResizable(false);

        Button menuButton = new Button("Gå til Menu");
        Button logoutButton = new Button("Annuller");
        Label textField = new Label("Din vagt er startet.\nGå til menu eller log ud automatisk.");
        textField.setWrapText(true);
        textField.setAlignment(Pos.CENTER);
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.getStyleClass().add("modalText");

        menuButton.getStyleClass().add("confirmButton");
        logoutButton.getStyleClass().add("defaultButton");

        menuButton.setOnAction(event -> {
            modalStage.close();
            loadMenuPage();
        });
        logoutButton.setOnAction(event -> {
            modalStage.close();
            MenuService.loadLoginPage(AppInstance.getPrimaryStage());
        });

        VBox vbox = new VBox(15, textField, new HBox(10, menuButton, logoutButton));
        vbox.setPadding(new Insets(20));

        Scene modalScene = new Scene(vbox);

        modalScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }
}