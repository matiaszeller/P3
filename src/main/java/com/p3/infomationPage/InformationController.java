package com.p3.infomationPage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.List;

public class InformationController {

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button tilbage; // Back button

    private final informationService infoService = new informationService();
    private List<User> users; // List to store all users
    private User selectedUser; // Currently selected user

    public void initialize() {
        // Fetch all users and populate ChoiceBox
        users = infoService.getAllUsers();
        ObservableList<String> userNames = FXCollections.observableArrayList();

        for (User user : users) {
            userNames.add(user.getFullName()); // Corrected method name
        }

        choiceBox.setItems(userNames);

        // Add listener to handle selection changes
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateUserDetails(newValue);
            }
        });

        // Add action to Back button
        tilbage.setOnAction(event -> goBack());
    }

    private void updateUserDetails(String fullName) {
        // Find the selected user by full name
        for (User user : users) {
            if (user.getFullName().equals(fullName)) { // Corrected method name
                selectedUser = user;
                break;
            }
        }

        // Populate the fields with the selected user's details
        if (selectedUser != null) {
            usernameField.setText(selectedUser.getUsername());
            passwordField.setText(selectedUser.getPassword());
        }
    }

    private void saveChanges() {
        if (selectedUser != null) {
            // Update the selected user with the new values
            selectedUser.setUsername(usernameField.getText());
            selectedUser.setPassword(passwordField.getText());

            // Submit the updated user to the backend
            boolean success = infoService.updateUser(selectedUser);

            if (success) {
                System.out.println("Changes saved successfully!");
            } else {
                System.err.println("Failed to save changes!");
            }
        }
    }

    private void goBack() {
        // Logic to navigate back to the previous screen
        System.out.println("Navigating back...");
    }
}