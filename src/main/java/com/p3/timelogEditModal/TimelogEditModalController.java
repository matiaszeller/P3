package com.p3.timelogEditModal;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class TimelogEditModalController {

    private LocalDate date;
    private JSONArray timelogs;
    private JSONArray returnArray = new JSONArray();
    private final List<Map<String, ComboBox<Integer>>> timePickerMap = new ArrayList<>();

    TimelogEditModalService service = new TimelogEditModalService();

    @FXML
    private Label modalMainLabel;
    @FXML
    private Button modalConfirmButton;
    @FXML
    private Button modalCancelButton;
    @FXML
    private VBox topConfigurationContainer;



    @FXML
    public void initialize() {
        setActionHandlers();
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


    public void generateModal(LocalDate date, JSONArray timelogs) {
        setDate(date);
        setTimelogs(timelogs);

        Map<String, String> eventTypeMap = Map.of(
                "check_in", "vagt start",
                "check_out", "vagt slut",
                "break_start", "pause start",
                "break_end", "pause slut"
        );

        Set<String> requiredEvents = new HashSet<>(eventTypeMap.keySet());
        Set<String> existingEvents = new HashSet<>();
        for (int i = 0; i < timelogs.length(); i++) {
            existingEvents.add(timelogs.getJSONObject(i).getString("event_type"));
        }

        for (String eventType : requiredEvents) {
            if (!existingEvents.contains(eventType)) {
                JSONObject missingEvent = new JSONObject();
                missingEvent.put("event_type", eventType);
                missingEvent.put("event_time", date.atTime(0, 0).toString()); // Default time: 00:00
                missingEvent.put("user_id", timelogs.getJSONObject(0).getInt("user_id"));
                missingEvent.put("shift_date", timelogs.getJSONObject(0).getString("shift_date"));
                missingEvent.put("edited_time", LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0));
                timelogs.put(missingEvent);
            }
        }

        modalMainLabel.setText("Ændrer Registreret tid for d. " + date.toString());

        for (int i = 0; i < timelogs.length(); i++) {
            JSONObject timelog = timelogs.getJSONObject(i);
            LocalDateTime eventTime = LocalDateTime.parse(timelog.getString("event_time"));

            ComboBox<Integer> hourPicker = new ComboBox<>();
            ComboBox<Integer> minutePicker = new ComboBox<>();

            Label eventLabel = new Label(String.format("Original %s %02d:%02d",
                    eventTypeMap.get(timelogs.getJSONObject(i).getString("event_type")),
                    eventTime.getHour(),
                    eventTime.getMinute())
            );
            eventLabel.setStyle("-fx-font-weight: bold");

            populateTimePickers(hourPicker, minutePicker);
            hourPicker.setValue(eventTime.getHour());
            minutePicker.setValue(eventTime.getMinute());

            Map<String, ComboBox<Integer>> pickers = new HashMap<>();
            pickers.put("hour", hourPicker);
            pickers.put("minute", minutePicker);
            timePickerMap.add(pickers);

            VBox eventBox = new VBox(eventLabel);
            eventBox.getStyleClass().add("timelogModalConfigurationContainers");

            HBox timePickerBox = new HBox(hourPicker, minutePicker);
            eventBox.getChildren().add(timePickerBox);
            timePickerBox.setStyle("-fx-alignment: center;");

            topConfigurationContainer.getChildren().add(eventBox);
        }
    }

    private void populateTimePickers(ComboBox<Integer> hourPicker, ComboBox<Integer> minutePicker) {
        for (int i = 0; i < 24; i++) {
            hourPicker.getItems().add(i);
        }
        for (int i = 0; i < 59; i ++) {
            minutePicker.getItems().add(i);
        }
    }

    private void updateReturnArray() {
        for (int i = 0; i < timelogs.length(); i++) {
            JSONObject timelog = timelogs.getJSONObject(i);

            Map<String, ComboBox<Integer>> pickers = timePickerMap.get(i);
            int newHour = pickers.get("hour").getValue();
            int newMinute = pickers.get("minute").getValue();

            LocalDateTime newTime = LocalDateTime.parse(timelog.getString("event_time"))
                    .withHour(newHour)
                    .withMinute(newMinute);

            timelog.put("edited_time", timelog.getString("event_time"));
            timelog.put("event_time", newTime.toString());

            returnArray.put(timelog);
        }
        System.out.println(returnArray);
    }

    private void setDate(LocalDate date) {
        this.date = date;
    }

    private void setTimelogs(JSONArray timelogs) {
        this.timelogs = timelogs;
    }
}
