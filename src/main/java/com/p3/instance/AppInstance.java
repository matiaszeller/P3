package com.p3.instance;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class AppInstance {
    private final Stage stage;

    public AppInstance(Stage stage) {
        this.stage = stage;
    }

    public void startApp() throws IOException {
        loadLoginScene();
    }

    public void loadLoginScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppInstance.class.getResource("/com.p3.login/LoginPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Time Registration System");
        stage.getIcons().add(new Image(Objects.requireNonNull(AppInstance.class.getResourceAsStream("/icons/favicon.png"))));
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
