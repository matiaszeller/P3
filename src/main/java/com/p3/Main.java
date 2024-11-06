package com.p3;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class Main extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = null;
        try{
            URL fxmlLocation = Main.class.getResource("/com.p3.login/LoginPage.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            System.out.println("FXML Location: " + fxmlLocation); // Print the URL to confirm the path

            scene = new Scene(fxmlLoader.load(), 1920, 1040);
         } catch (IOException e) {
            e.printStackTrace();
         }

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