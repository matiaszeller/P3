package com.p3.timelogEditModal;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimelogEditModalController {

    private LocalDate date;
    private JSONArray timelogs;
    private JSONArray returnArray = new JSONArray();

    TimelogEditModalService service = new TimelogEditModalService();

    @FXML
    private Label shiftStartLabel;
    @FXML
    private Label shiftEndLabel;
    @FXML
    private Label breakStartLabel;
    @FXML
    private Label breakEndLabel;
    @FXML
    private Label modalMainLabel;
    @FXML
    private Label shiftStartHeader;
    @FXML
    private Label shiftEndHeader;
    @FXML
    private Label breakStartHeader;
    @FXML
    private Label breakEndHeader;
    @FXML
    private Button modalConfirmButton;
    @FXML
    private Button modalCancelButton;
    @FXML
    private ComboBox<Integer> shiftStartHourPicker, shiftStartMinutePicker, shiftEndHourPicker, shiftEndMinutePicker;
    @FXML
    private ComboBox<Integer> breakStartHourPicker, breakStartMinutePicker, breakEndHourPicker, breakEndMinutePicker;



    @FXML
    public void initialize() {
        setActionHandlers();

        populateTimePickers(shiftStartHourPicker, shiftStartMinutePicker);
        populateTimePickers(shiftEndHourPicker, shiftEndMinutePicker);
        populateTimePickers(breakStartHourPicker, breakStartMinutePicker);
        populateTimePickers(breakEndHourPicker, breakEndMinutePicker);
    }

    private void setActionHandlers() {
        modalConfirmButton.setOnAction(event -> {
            updateReturnArray();
            service.postUpdatedTimelogs(returnArray);
            Stage stage = (Stage) modalConfirmButton.getScene().getWindow();
            stage.close();
        });
        modalCancelButton.setOnAction(event -> {
            Stage stage = (Stage) modalCancelButton.getScene().getWindow();
            stage.close();
        });


    }

    //TODO FIX DET HER DET VIRKELIGT LORT SKREVET MEN HAVDE IKKE NOK TID

    public void generateModal(LocalDate date, JSONArray timelogs) {
        setDate(date);
        setTimelogs(timelogs);

        modalMainLabel.setText("Ændrer Registreret tid for d. " + date.toString());


        LocalDateTime shiftStart = LocalDateTime.parse(timelogs.getJSONObject(0).getString("event_time"));
        shiftStartHourPicker.setValue(shiftStart.getHour());
        shiftStartMinutePicker.setValue(shiftStart.getMinute());

        LocalDateTime shiftEnd = LocalDateTime.parse(timelogs.getJSONObject(3).getString("event_time"));
        shiftEndHourPicker.setValue(shiftEnd.getHour());
        shiftEndMinutePicker.setValue(shiftEnd.getMinute());

        LocalDateTime breakStart = LocalDateTime.parse(timelogs.getJSONObject(1).getString("event_time"));
        breakStartHourPicker.setValue(breakStart.getHour());
        breakStartMinutePicker.setValue(breakStart.getMinute());

        LocalDateTime breakEnd = LocalDateTime.parse(timelogs.getJSONObject(2).getString("event_time"));
        breakEndHourPicker.setValue(breakEnd.getHour());
        breakEndMinutePicker.setValue(breakEnd.getMinute());

    }

    private void populateTimePickers(ComboBox<Integer> hourPicker, ComboBox<Integer> minutePicker) {
        for (int i = 0; i < 24; i++) {
            hourPicker.getItems().add(i);
        }
        // only up to 58 cuz otherwise it fucks everthing up :D:D:D:D:D:D:D
        for (int i = 0; i < 59; i ++) {
            minutePicker.getItems().add(i);
        }
    }

    private void updateReturnArray(){
        for(int i = 0; i < timelogs.length(); i++){
            JSONObject timelog = timelogs.getJSONObject(i);
            LocalDateTime newTime = LocalDateTime.parse(timelog.getString("event_time"));

            switch(i){
                case 0:
                    newTime = newTime.withHour(shiftStartHourPicker.getValue()).withMinute(shiftStartMinutePicker.getValue());
                    break;
                case 1:
                    newTime = newTime.withHour(breakStartHourPicker.getValue()).withMinute(breakStartMinutePicker.getValue());
                    break;
                case 2:
                    newTime = newTime.withHour(breakEndHourPicker.getValue()).withMinute(breakEndMinutePicker.getValue());
                    break;
                case 3:
                    newTime = newTime.withHour(shiftEndHourPicker.getValue()).withMinute(shiftEndMinutePicker.getValue());
                    break;
            }
            // TODO fix og check på at datepicker tider ikke er før og efter hinanden
            timelog.put("edited_time", timelog.getString("event_time"));
            timelog.put("event_time", newTime.toString());
            returnArray.put(timelog);
        }
    }

    private void setDate(LocalDate date) {
        this.date = date;
    }

    private void setTimelogs(JSONArray timelogs) {
        this.timelogs = timelogs;
    }
}
