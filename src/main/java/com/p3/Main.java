package com.p3;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com.p3.login/LoginPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Time Registration System");
        stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icons/favicon.png"))));


        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}