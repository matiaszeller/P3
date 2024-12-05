package com.p3.util;

import com.p3.instance.AppInstance;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

// Global modal loader, should be able to be used for modal on both employeehistory and managerview
public class ModalUtil {
    public static <T> ModalResult<T> showModal(String fxmlPath, Stage ownerStage, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ModalUtil.class.getResource(fxmlPath));
            Parent root = fxmlLoader.load();

            Stage modalStage = new Stage();
            modalStage.setTitle(title);
            modalStage.setScene(new Scene(root));

            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.initOwner(ownerStage);

            modalStage.setMinWidth(600);
            modalStage.setMinHeight(600);
            modalStage.setMaxWidth(600);
            modalStage.setMaxHeight(600);

            modalStage.getIcons().add(new Image(Objects.requireNonNull(AppInstance.class.getResourceAsStream("/icons/favicon.png"))));

            // returns both stage and controller, so stage can be loaded with content from parent controller page
            return new ModalResult<>(fxmlLoader.getController(), modalStage);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class ModalResult<T> {
        private final T controller;
        private final Stage Stage;

        public ModalResult(T controller, Stage Stage) {
            this.controller = controller;
            this.Stage = Stage;
        }

        public T getController() {
            return controller;
        }

        public Stage getStage() {
            return Stage;
        }
    }
}
