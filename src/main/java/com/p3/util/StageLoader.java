package com.p3.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StageLoader {

    // Call method with fxml file and stage to load new stage, can be used from within all other controllers on event actions
    // - Stage kan bare fåes ud fra et vilkårligt element fra cur scene
    public void loadStage(String fxmlPath, Stage stage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            double width = stage.getWidth();
            double height = stage.getHeight();

            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
