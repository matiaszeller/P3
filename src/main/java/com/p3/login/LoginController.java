package com.p3.login;

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
import javafx.util.Duration;
import javafx.scene.control.ProgressBar;
import javafx.geometry.Insets;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import com.p3.session.Session;

public class LoginController {
    @FXML
    private Label errorText;
    @FXML
    private TextField usernameField;
    @FXML
    private Button loginButton;

    private String managerUsername;
    private String employeeUsername;
    private final LoginService loginService = new LoginService();

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> {
            try {
                handleLogin();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        usernameField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> {
                    try {
                        handleLogin();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void handleLogin(){ // TODO ærligt måske bare overvej at lave én method til login DAO så vi ikke laver 4 forskellige kald til db
        String username = usernameField.getText();
        String role = loginService.validateUser(username);

        if (role == null) {
            errorText.setVisible(true);
        } else {
            int userId = loginService.getUserId(username);
            String fullName = loginService.getUserFullName(username);

            Session.setCurrentUserRole(role);
            Session.setCurrentUserId(userId);
            Session.setCurrentUserFullName(fullName);

            if ("manager".equalsIgnoreCase(role)) {
                showManagerModal(username);
            } else if ("employee".equalsIgnoreCase(role)) {
                boolean clockedIn = loginService.getClockedInStatus(username);

                if (clockedIn) {
                    loadMenuPage();
                } else {
                    showEmployeeModal(username);
                }
            }
        }
    }

    private void loadMenuPage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.p3.menu/MenuPage.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            // This is for getting the stage sizes so it doesn't mess with alignment and centering
            double width = stage.getWidth();
            double height = stage.getHeight();

            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);

            stage.setWidth(width);
            stage.setHeight(height);
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

        Label instructionLabel = new Label("Indtast Manager Kode:");
        instructionLabel.getStyleClass().add("modalText");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Adgangskode");

        Button submitButton = new Button("Bekræft");
        Button cancelButton = new Button("Annuller");
        Label modalErrorLabel = new Label("Forkert kodeord. Prøv igen.");
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
            int userId = loginService.getUserId(managerUsername);
            String fullName = loginService.getUserFullName(managerUsername);

            Session.setCurrentUserId(userId);
            Session.setCurrentUserFullName(fullName);

            modalStage.close();
            loadMenuPage();
        } else {
            modalErrorLabel.setVisible(true);
        }
    }

    private void showEmployeeModal(String username) {
        this.employeeUsername = username;

        int userId = Session.getCurrentUserId();

        loginService.postCheckInEvent(userId);

        loginService.setClockedInStatus(username, true);

        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Notification");

        modalStage.setResizable(false);

        Button menu = new Button("Gå til menu");
        Button logout = new Button("Log ud");
        Label textField = new Label("Din vagt er startet.\nGå til menu eller log ud automatisk.\n\nAutomatisk logud:");
        textField.getStyleClass().add("modalText");
        textField.setWrapText(true);
        textField.setAlignment(Pos.CENTER);
        textField.setMaxWidth(Double.MAX_VALUE);
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);

        menu.getStyleClass().add("confirmButton");
        logout.getStyleClass().add("defaultButton");

        HBox hbox = new HBox(10, textField, menu, logout);
        hbox.setPadding(new Insets(10));
        hbox.setPrefSize(300, 100);
        VBox vbox = new VBox(15,textField,progressBar,hbox);
        vbox.setPadding(new Insets(20));

        Scene modalScene = new Scene(vbox);
        modalScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        modalStage.setScene(modalScene);
        modalStage.show();
        Timeline progressAnimation = new Timeline(
                new KeyFrame(Duration.seconds(5), new KeyValue(progressBar.progressProperty(), 1))
        );

        PauseTransition timer = new PauseTransition(Duration.seconds(5));
        timer.setOnFinished(event -> LogoutAndClose(modalStage));
        //todo J: This might inadvertently create a memory leak, but i dont care right now.
        menu.setOnAction(event -> { progressAnimation.stop(); timer.stop(); MenuAndClose(modalStage); });
        logout.setOnAction(event -> {LogoutAndClose(modalStage);});
        progressAnimation.play();
        timer.play();

    }

    private void LogoutAndClose(Stage modalStage) {
        modalStage.close();
        usernameField.clear();
        Session.clearSession();
    }

    private void MenuAndClose(Stage modalStage) {
        modalStage.close();
        loadMenuPage();
    }
}