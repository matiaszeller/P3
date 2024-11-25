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
    private TextField nameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button tilbage;

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


        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateUserDetails(newValue);
            }
        });


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
        }
    }

    private void saveChanges() {
        if (selectedUser != null) {

            selectedUser.setUsername(nameField.getText());
            selectedUser.setPassword(passwordField.getText());


            boolean success = infoService.updateUser(selectedUser);

            if (success) {
                System.out.println("Changes saved successfully!");
            } else {
                System.err.println("Failed to save changes!");
            }
        }
    }

    private void goBack() {

        System.out.println("Navigating back...");
    }
}