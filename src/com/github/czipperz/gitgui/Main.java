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
        String directory = getDirectory();
        primaryStage.setTitle("Git Graph Gui -- " + directory);

        Scene scene = new Scene(buildScrollPane(directory), 600, 600);
        scene.getStylesheets().add("style.css");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private ScrollPane buildScrollPane(String directory) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(graphPane(scrollPane, directory));
        return scrollPane;
    }

    private Node graphPane(ScrollPane scrollPane, String directory) {
        StackPane pane = new StackPane();
        ShellInteraction shellInteraction = new ShellInteraction(directory);
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
            return System.getProperty("user.dir");
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
