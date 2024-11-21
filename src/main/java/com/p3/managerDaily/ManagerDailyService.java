package com.p3.managerDaily;

import com.p3.menu.MenuService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Map;
import java.io.IOException;
import java.util.List;
import java.time.LocalDate;

public class ManagerDailyService {

    ManagerDailyDAO dao = new ManagerDailyDAO();

    public ManagerDailyService() {
        // Constructor or method to process timelogs
        List<Map<String, Object>> timelogs = dao.getTimelogsForDate(LocalDate.of(2024, 11, 20));

        // testing..
        for (Map<String, Object> timelog : timelogs) {
            System.out.println("User ID: " + timelog.get("userId"));
            System.out.println("Shift Date: " + timelog.get("shiftDate"));
            System.out.println("Event Type: " + timelog.get("eventType"));
        }
    }

    public static void loadLoginPage(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MenuService.class.getResource("/com.p3.login/LoginPage.fxml"));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene loginScene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(loginScene);

            stage.setTitle("Time Registration System");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
