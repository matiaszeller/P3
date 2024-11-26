package com.p3.infomationPage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


import static com.p3.login.LoginService.hashPassword;

public class InformationController {

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

    @FXML Button CancelBtn;

    @FXML Button submitBtn;

   @FXML
   private ChoiceBox<String> roleChoiceBox;

    private final informationService infoService = new informationService();
    private List<User> users;
    private User selectedUser;

    public void initialize() {

        users = infoService.getAllUsers();
        ObservableList<String> userNames = FXCollections.observableArrayList();

        for (User user : users) {
            userNames.add(user.getFullName());
        }

        choiceBox.setItems(userNames);

        ObservableList<String> roles = FXCollections.observableArrayList("manager", "medarbejder");
        roleChoiceBox.setItems(roles);

        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateUserDetails(newValue);
            }
        });

        submitBtn.setOnAction(event -> saveChanges());
        tilbage.setOnAction(event -> goBack());
    }

    private void updateUserDetails(String fullName) {

        for (User user : users) {
            if (user.getFullName().equals(fullName)) {
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
            if (!passwordField.getText().isEmpty()) {
                String hashedPassword;
                if (Objects.equals(passwordField.getText(), secondPasswordField.getText())) {
                    hashedPassword = passwordField.getText();
                    hashPassword(hashedPassword);
                    selectedUser.setPassword(hashedPassword);
                }
            }
            selectedUser.setRole(roleChoiceBox.getSelectionModel().getSelectedItem());
            selectedUser.setFullName(lastNameField.getText());


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