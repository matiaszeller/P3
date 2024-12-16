package com.p3.login;

import com.p3.util.StageLoader;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import java.io.IOException;
import java.util.Objects;
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
    private Label deactivatedText;
    @FXML
    private TextField usernameField;
    @FXML
    private Button loginButton;

    private String managerUsername;
    private String employeeUsername;
    private final LoginService loginService = new LoginService();
    private final StageLoader stageLoader = new StageLoader();

    @FXML
    private void initialize() {
        Session.clearSession();

        loginButton.setOnAction(event -> {
            try {
                String username = usernameField.getText();
                String apiKey = loginService.getApiKey(username);
                Session.setApiKey(apiKey);
                handleLogin();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        usernameField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> {
                    try {
                        String username = usernameField.getText();
                        String apiKey = loginService.getApiKey(username);
                        Session.setApiKey(apiKey);
                        handleLogin();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void handleLogin() throws Exception {
        String username = usernameField.getText();
        String role = loginService.setUserRole(username);

        if (role == null) {
            errorText.setVisible(true);
        } else if (role.equals("deaktiverede")){
            deactivatedText.setVisible(true);
        }
        {int userId = loginService.getUserId(username);
            String fullName = loginService.getUserFullName(username);

            Session.setCurrentUserRole(role);
            Session.setCurrentUserId(userId);
            Session.setCurrentUserFullName(fullName);
            Session.setRole(role);

            if ("manager".equalsIgnoreCase(role)) {
                showManagerModal(username);
            } else if ("employee".equalsIgnoreCase(role)) {
                boolean clockedIn = loginService.getClockedInStatus(username);

                if (clockedIn) {
                    stageLoader.loadStage("/com.p3.menu/MenuPage.fxml",(Stage) loginButton.getScene().getWindow());
                } else {
                    showEmployeeModal(username);
                }
            }
        }
    }

    private void showManagerModal(String username) {
        this.managerUsername = username;

        // Create modal elements
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("");
        modalStage.getIcons().add(new Image(Objects.requireNonNull(LoginController.class.getResourceAsStream("/icons/favicon.png"))));

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

        submitButton.setOnAction(event -> {
            try {
                handleSubmit(modalStage, passwordField.getText(), modalErrorLabel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    handleSubmit(modalStage, passwordField.getText(), modalErrorLabel);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
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

    private void handleSubmit(Stage modalStage, String password, Label modalErrorLabel) throws IOException {
        if (loginService.validateManager(managerUsername, password)) {
            int userId = loginService.getUserId(managerUsername);
            String fullName = loginService.getUserFullName(managerUsername);
            String username = usernameField.getText();

            Session.setCurrentUserId(userId);
            Session.setCurrentUserFullName(fullName);

            modalStage.close();
            boolean clockedIn = loginService.getClockedInStatus(username);

            if (clockedIn) {
                stageLoader.loadStage("/com.p3.menu/MenuPage.fxml",(Stage) loginButton.getScene().getWindow());
            } else {
                showEmployeeModal(username);
            }
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
        menu.setOnAction(event -> { progressAnimation.stop(); timer.stop();
            try {
                MenuAndClose(modalStage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        logout.setOnAction(event -> {LogoutAndClose(modalStage);});
        progressAnimation.play();
        timer.play();

    }

    private void LogoutAndClose(Stage modalStage) {
        modalStage.close();
        usernameField.clear();
        Session.clearSession();
    }

    private void MenuAndClose(Stage modalStage) throws Exception{
        modalStage.close();
        stageLoader.loadStage("/com.p3.menu/MenuPage.fxml",(Stage) loginButton.getScene().getWindow());
    }
}