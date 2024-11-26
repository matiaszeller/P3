package com.p3.employeeHistory;

import com.p3.menu.MenuService;
import com.p3.session.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;


public class EmployeeHistoryController {

    @FXML
    private Button logOutButton;
    @FXML
    private Button goBackButton;
    @FXML
    private VBox contentContainer;
    @FXML
    private Button prevWeekButton;
    @FXML
    private Button nextWeekButton;

    private final EmployeeHistoryService employeeHistoryService = new EmployeeHistoryService();
    private LocalDate date; // On load is .now, afterwards is used as working date

    @FXML
    private void initialize(){
        logOutButton.setOnAction(event -> handleLogOut());
        goBackButton.setOnAction(event -> loadMenuStage());
        prevWeekButton.setOnAction(event -> fetchPrevWeekHistory());
        nextWeekButton.setOnAction(event -> fetchNextWeekHistory());

        date = LocalDate.now();


        handleWeekTimelogs(date);    // On first load, go from todays date, and fetch current weeks timelogs TODO make sessiontokens?
    }

    private void handleLogOut(){   // Copied from menuController, maybe global method instead?
        Session.clearSession();

        Stage stage = (Stage) logOutButton.getScene().getWindow();
        MenuService.loadLoginPage(stage);   // Shouldn't be on service in any way, but make a global stageloader class
    }

    private void loadMenuStage(){  // Copied from loginController - Like before, maybe make a global class for stageloaders?
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.p3.menu/MenuPage.fxml"));
            Stage stage = (Stage) logOutButton.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleWeekTimelogs(LocalDate localDate){
        JSONArray jsonArray = new JSONArray(employeeHistoryService.getWeekTimelogs(localDate, Session.getCurrentUserId()));
        contentContainer.getChildren().removeIf(node -> node instanceof VBox);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray dayTimelogs = jsonArray.getJSONArray(i);

            if (dayTimelogs.length() == 0) {
                VBox emptyBox = new VBox();
                emptyBox.getStyleClass().add("emptyShiftBox");
                Label noShiftLabel = new Label("Ingen vagt");
                emptyBox.getChildren().add(noShiftLabel);
                VBox.setVgrow(emptyBox, Priority.ALWAYS);
                contentContainer.getChildren().add(emptyBox);
            } else {
                VBox filledBox = new VBox();
                filledBox.getStyleClass().add("filledShiftBox");

                for (int j = 0; j < dayTimelogs.length(); j++) {
                    JSONObject timelog = dayTimelogs.getJSONObject(j);
                    String eventType = timelog.getString("event_type");
                    String eventTime = timelog.getString("event_time");

                    Label timelogLabel = new Label("Type: " + eventType + ", Time: " + eventTime);
                    filledBox.getChildren().add(timelogLabel);
                }
                filledBox.setVgrow(filledBox, Priority.ALWAYS);
                contentContainer.getChildren().add(filledBox);
            }
        }
    }


    //TODO also change weeknumber label
    private void fetchNextWeekHistory(){
        date = date.plusWeeks(1);
        handleWeekTimelogs(date);
    }

    private void fetchPrevWeekHistory(){
        date = date.minusWeeks(1);
        handleWeekTimelogs(date);
    }
}
