package com.p3;

import com.p3.instance.AppInstance;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    private AppInstance appInstance;

    @Override
    public void start(Stage stage) throws IOException {
        appInstance = new AppInstance(stage);
        appInstance.startApp();
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
    }

    public static void main(String[] args) {
        launch();
    }
}
