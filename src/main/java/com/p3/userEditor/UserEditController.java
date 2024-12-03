package com.p3.userEditor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


import static com.p3.login.LoginService.hashPassword;

public class UserEditController {

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    private TextField nameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField secondPasswordField;

    @FXML
    private Button tilbage;

    @FXML
    Button submitBtn;

    @FXML
    Button newUserBtn;

   @FXML
   private ChoiceBox<String> roleChoiceBox;

   @FXML
   private Label PassText;

   @FXML Label secPassText;

    private final UserEditService infoService = new UserEditService();
    private List<User> users;
    private User selectedUser;

    public void initialize() {

        users = infoService.getAllUsers();
        ObservableList<String> userNames = FXCollections.observableArrayList();

        for (User user : users) {
            userNames.add(user.getFullName());
        }

        choiceBox.setItems(userNames);

        ObservableList<String> roles = FXCollections.observableArrayList("manager", "medarbejder", "deaktiver");
        roleChoiceBox.setItems(roles);

        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateUserDetails(newValue);
            }
        });

        submitBtn.setOnAction(event -> saveChanges());
        tilbage.setOnAction(event -> goBack());
        newUserBtn.setOnAction(event -> newUser());
    }

    private void updateUserDetails(String fullName) {

        for (User user : users) {
            if (user.getFullName().equals(fullName)) {
                if (Objects.equals(user.getRole(), "employee")){
                    passwordField.visibleProperty().set(false);
                    secondPasswordField.visibleProperty().set(false);
                    PassText.visibleProperty().set(false);
                    secPassText.visibleProperty().set(false);
                }
                else{passwordField.visibleProperty().set(true);
                    secondPasswordField.visibleProperty().set(true);
                    PassText.visibleProperty().set(true);
                    secPassText.visibleProperty().set(true);}
                selectedUser = user;
                break;
            }
        }


        if (selectedUser != null) {
            nameField.setText(selectedUser.getUsername());
            passwordField.setText(selectedUser.getPassword());
            roleChoiceBox.getSelectionModel().select(selectedUser.getRole());
            lastNameField.setText(selectedUser.getFullName());

        }
    }

    private void saveChanges() {
        if (selectedUser != null) {

            selectedUser.setUsername(nameField.getText());
            selectedUser.setFullName(lastNameField.getText());
            if(passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                String hashedPassword;
                    if (Objects.equals(passwordField.getText(), secondPasswordField.getText())) {
                        hashedPassword = passwordField.getText();
                        hashPassword(hashedPassword);
                        selectedUser.setPassword(hashedPassword);
                    }
            }
            String selectedRole = roleChoiceBox.getSelectionModel().getSelectedItem();

            switch (selectedRole) {
                case "deaktiver":
                    selectedUser.setRole("deaktiverede");
                    break;
                case "medarbejder":
                    selectedUser.setRole("employee");
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + selectedRole);
            }

            boolean success = infoService.updateUser(selectedUser);

            if (success) {
                System.out.println("Changes saved successfully!");
            } else {
                System.err.println("Failed to save changes!");
            }
        }
    }

    private void goBack() {
        loadMenuPage();
    }
    private void newUser() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Tilføj ny bruger");


        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label instructionLabel = new Label("Indtast brugeroplysninger:");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Fuldt navn");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Brugernavn");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Adgangskode");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Bekræft adgangskode");


        ChoiceBox<String> roleChoiceBox = new ChoiceBox<>();
        roleChoiceBox.setItems(FXCollections.observableArrayList("manager", "employee"));
        roleChoiceBox.setValue("employee");


        passwordField.setVisible(false);
        confirmPasswordField.setVisible(false);

        Label passwordLabel = new Label("Adgangskode:");
        Label confirmPasswordLabel = new Label("Bekræft adgangskode:");
        passwordLabel.setVisible(false);
        confirmPasswordLabel.setVisible(false);


        roleChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isEmployee = newValue.equals("employee");
            passwordField.setVisible(!isEmployee);
            confirmPasswordField.setVisible(!isEmployee);
            passwordLabel.setVisible(!isEmployee);
            confirmPasswordLabel.setVisible(!isEmployee);
        });

        Button createButton = new Button("Opret Bruger");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");


        layout.getChildren().addAll(
                instructionLabel,
                fullNameField,
                usernameField,
                roleChoiceBox,
                passwordLabel,
                passwordField,
                confirmPasswordLabel,
                confirmPasswordField,
                createButton,
                errorLabel
        );

            createButton.setOnAction(event -> {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String role = roleChoiceBox.getValue();

            if (fullName.isEmpty()) {
                errorLabel.setText("Fuldt navn skal udfyldes.");
                return;
            }

            if (username.isEmpty()) {
                errorLabel.setText("Brugernavn skal udfyldes.");
                return;
            }

            String password = null;
            if (!role.equals("employee")) {
                password = passwordField.getText();
                String confirmPassword = confirmPasswordField.getText();

                if (password.isEmpty() || confirmPassword.isEmpty()) {
                    errorLabel.setText("Adgangskodefelterne skal udfyldes.");
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    errorLabel.setText("Adgangskoderne stemmer ikke overens.");
                    return;
                }
            }


            User newUser = new User(0, username, fullName, false, false, false, password, role);

            boolean success = infoService.createUser(newUser);

            if (success) {

                choiceBox.getItems().add(newUser.getFullName());
                modalStage.close();
                initialize();
            } else {
                errorLabel.setText("Kunne ikke oprette bruger. Prøv igen.");
            }
        });

        Scene scene = new Scene(layout);
        modalStage.setScene(scene);
        modalStage.setResizable(false);
        modalStage.showAndWait();
    }

    //todo for some reason, i cant call the method in the login controller, so i've implemented it again. needs to be fixed
    private void loadMenuPage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.p3.menu/MenuPage.fxml"));
            Stage stage = (Stage) tilbage.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}