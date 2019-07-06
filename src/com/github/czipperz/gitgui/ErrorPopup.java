package com.github.czipperz.gitgui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorPopup extends Stage {
    public ErrorPopup(String message) {
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Error: " + message);
        setErrorScene(message);
    }

    private void setErrorScene(String message) {
        VBox layout = new VBox();
        layout.setSpacing(10);
        layout.setAlignment(Pos.CENTER);

        layout.getChildren().add(new Label("Error: " + message));
        layout.getChildren().add(buildCloseButton());

        setScene(new Scene(layout, 400, 200));
    }

    private Button buildCloseButton() {
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> close());
        return closeButton;
    }
}
