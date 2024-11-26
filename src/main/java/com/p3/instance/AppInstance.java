package com.p3.instance;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;
import java.time.LocalDateTime;

public class AppInstance {
    private static Stage primaryStage;
    private static LocalDateTime serverStartTime;

    public AppInstance(Stage stage) {
        this.primaryStage = stage;
    }

    public void startApp() throws IOException {
        fetchAndStoreServerTime();
        loadLoginScene();
    }

    private void fetchAndStoreServerTime() {
        AppInstanceDAO dao = new AppInstanceDAO();
        serverStartTime = dao.fetchServerTime();
    }

    public static LocalDateTime getServerStartTime() {
        return serverStartTime;
    }

    public void loadLoginScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppInstance.class.getResource("/com.p3.login/LoginPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        primaryStage.setTitle("Time Registration System");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(AppInstance.class.getResourceAsStream("/icons/favicon.png"))));
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}