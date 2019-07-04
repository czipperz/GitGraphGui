package com.github.czipperz.gitgui;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {
    private Timer updateTimer;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Git Gui");

        Scene scene = new Scene(buildScrollPane(), 600, 600);
        scene.getStylesheets().add("style.css");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private ScrollPane buildScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(graphPane(scrollPane));
        return scrollPane;
    }

    private Node graphPane(ScrollPane scrollPane) {
        StackPane pane = new StackPane();
        ShellInteraction shellInteraction = new ShellInteraction(getDirectory());
        Runnable updater = new GraphPaneUpdater(scrollPane, pane, shellInteraction);

        updateTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                updater.run();
            }
        };
        updateTimer.scheduleAtFixedRate(timerTask, 0, 1000);

        return pane;
    }

    private String getDirectory() {
        List<String> params = getParameters().getRaw();
        if (params.size() >= 1) {
            return params.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        updateTimer.cancel();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
