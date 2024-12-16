package com.p3.exportModal;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ExportModalController {

    public Label modalMainLabel;
    public HBox datepickerContainer;
    public DatePicker startDatePicker;
    public DatePicker endDatePicker;
    public HBox buttonContainer;
    public Button confirmButton;
    public Button cancelButton;
    public VBox modalMainContainer;
    public Label datapickerErrorLabel;

    private Stage stage;


    ExportModalService exportModalService = new ExportModalService();

    @FXML
    public void initialize() {
        // Takes focus away from the datapicker and sets to parent box + gets stage
        Platform.runLater(() -> {
            modalMainContainer.requestFocus();
            stage = (Stage) modalMainContainer.getScene().getWindow();
        });

        generateModal();

        // Removes focus from datapickers if background is pressed - making it easier to see the data inputted
        modalMainContainer.setOnMouseClicked(event -> {
            modalMainContainer.requestFocus();
        });

        confirmButton.setOnAction(event -> {generateCSV();});
        cancelButton.setOnAction(event -> {stage.close();});
    }

    public void generateModal() {
        startDatePicker.setPromptText("Vælg Start Dato");
        endDatePicker.setPromptText("Vælg Slut Dato");
    }

    // Fetch downloadable csv file based on LocalDates from datepickers
    private void generateCSV() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Gem CSV-fil");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            var saveFile = fileChooser.showSaveDialog(stage);
            if (saveFile != null) {
                String savePath = saveFile.getAbsolutePath();
                exportModalService.getTimelogsCSV(startDate, endDate, savePath);
                stage.close();
            }
        } else {
            datapickerErrorLabel.setText("Ugyldige datoer valgt!");
            datapickerErrorLabel.setVisible(true);
        }
    }
}
