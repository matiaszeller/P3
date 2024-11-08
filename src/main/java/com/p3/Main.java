package com.p3;

import com.p3.instance.AppInstance;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

import java.net.URL;
import java.util.Objects;


public class Main extends Application {
    private AppInstance appInstance;

    @Override
    public void start(Stage stage) throws IOException {
        appInstance = new AppInstance(stage);
        appInstance.startApp();

    }

    public static void main(String[] args) {
        launch();
    }
}
