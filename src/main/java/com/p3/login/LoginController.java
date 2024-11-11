package com.p3.login;

import com.p3.instance.AppInstance;
import com.p3.menu.MenuController;
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
import javafx.util.Duration;
import javafx.scene.control.ProgressBar;
import javafx.geometry.Insets;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;


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
        RoleHolder roleHolder = new RoleHolder();
        boolean isValid = loginService.validateUser(usernameField.getText(), roleHolder);

        if (!isValid) {
            errorText.setVisible(true);
        } else {
            String employmentRole = roleHolder.getEmploymentRole();
            if ("manager".equalsIgnoreCase(employmentRole)) {
                showManagerModal(usernameField.getText());
            } else if ("employee".equalsIgnoreCase(employmentRole)) {

                showEmployeeModal(usernameField.getText());

                //loadMenuPage();
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


    private void showEmployeeModal(String username) {
        MenuService menuService = new MenuService();
        this.employeeUsername = username;

        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Notifikation");


        modalStage.setResizable(false);

        Button menu = new Button("Gå til menu.");
        Button logout = new Button("Log ud.");
        Label textField = new Label("Din tid er blevet registrede.\n  Log ud eller gå til menu. \n \n      Automatisk logud:");
        textField.setWrapText(true);
        textField.setAlignment(Pos.CENTER); // Center align the text within the Label
        textField.setMaxWidth(Double.MAX_VALUE); // Set max width to make centering effective
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
        MenuService.loadLoginPage(AppInstance.getPrimaryStage());
    }

    private void MenuAndClose(Stage modalStage) {

        modalStage.close();
        loadMenuPage();

    }
}

