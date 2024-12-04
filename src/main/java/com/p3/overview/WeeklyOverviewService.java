package com.p3.overview;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.*;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class WeeklyOverviewService {

    private static final WeeklyOverviewDAO dao = new WeeklyOverviewDAO();
    private static List<Map<String, Object>> weeklyTimelogs = new ArrayList<>();

    // Load for year
    public static void loadWeeklyTimelogs(int year) {
        weeklyTimelogs = dao.getAllWeeklyLogs();

        // Filter logs by the selected year
        weeklyTimelogs = weeklyTimelogs.stream()
                .filter(log -> {
                    LocalDate weekStart = LocalDate.parse((String) log.get("weekStart"));
                    return weekStart.getYear() == year;
                })
                .toList();
    }

    public static List<Map<String, Object>> getWeeklyTimelogs() {
        return weeklyTimelogs;
    }

    //Loads WeeklyOverview
    public static void loadWeeklyOverview(Stage stage) {
        try {
            URL fxmlLocation = WeeklyOverviewController.class.getResource("/com.p3.overview/Weeklyoverview.fxml");
            if (fxmlLocation == null) {
                throw new IOException("FXML file not found at specified location");
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getBounds().getWidth();
            double screenHeight = screen.getBounds().getHeight();

            stage.setWidth(screenWidth);
            stage.setHeight(screenHeight);
            stage.setScene(scene);
            stage.show();
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Loads Loginpage when "Log Out" button is pressed
    public static void loadLoginPage(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(WeeklyOverviewService.class.getResource("/com.p3.login/LoginPage.fxml"));
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
    public static void loadMenuPage(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(WeeklyOverviewService.class.getResource("/com.p3.menu/MenuPage.fxml"));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(fxmlLoader.load(), width, height);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}