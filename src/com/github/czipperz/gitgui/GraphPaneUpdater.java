package com.github.czipperz.gitgui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class GraphPaneUpdater implements Runnable {
    private ScrollPane scrollPane;
    private Pane parent;
    private ShellInteraction shellInteraction;

    public GraphPaneUpdater(ScrollPane scrollPane, Pane parent, ShellInteraction shellInteraction) {
        this.scrollPane = scrollPane;
        this.parent = parent;
        this.shellInteraction = shellInteraction;
    }

    @Override
    public void run() {
        Pane linesPane = new VBox();

        for (String line : shellInteraction.getGraphLines()) {
            linesPane.getChildren().add(processLine(line));
        }

        Platform.runLater(() -> {
            parent.getChildren().clear();
            parent.getChildren().add(linesPane);

            double vvalue = scrollPane.getVvalue();
            scrollPane.layout();
            scrollPane.setVvalue(vvalue);
        });
    }

    private Node processLine(String line) {
        Pane linePane = new HBox();

        if (line.contains("*")) {
            int index = 0;

            int end = line.indexOf('[', index);
            String prefix = line.substring(0, end);
            index = end;

            end = line.indexOf(']', index);
            String refs = line.substring(index + 1, end);
            index = end + 2;

            String suffix = line.substring(index);

            addPrefix(linePane, prefix);
            if (!refs.isEmpty()) {
                addRefs(linePane, refs);
            }
            addLabel(linePane, suffix);
        } else {
            addLabel(linePane, line);
        }

        return linePane;
    }

    private void addPrefix(Pane linePane, String prefix) {
        int start = findPrefixHashStart(prefix);
        int end = start + 7;
        addLabel(linePane, prefix.substring(0, start));
        addRefButton(linePane, prefix.substring(start, end));
        addLabel(linePane, prefix.substring(end));
    }

    private int findPrefixHashStart(String prefix) {
        for (int i = 0; i < prefix.length(); ++i) {
            char c = prefix.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F') {
                return i;
            }
        }

        throw new RuntimeException("Couldn't find hash in " + prefix);
    }

    private void addLabel(Pane linePane, String prefix) {
        linePane.getChildren().add(new Label(prefix));
    }

    private void addRefs(Pane linePane, String refsTotal) {
        String[] refs = refsTotal.split(", ");
        addLabel(linePane, "(");
        for (int i = 0; i < refs.length; i++) {
            if (i != 0) {
                addLabel(linePane, ", ");
            }

            String refString = refs[i];
            if (refString.equals("HEAD")) {
                addLabel(linePane, "HEAD");
            } else {
                for (String prefix : new String[]{"HEAD -> ", "tag: "}) {
                    if (refString.startsWith(prefix)) {
                        addLabel(linePane, prefix);
                        refString = refString.substring(prefix.length());
                    }
                }

                if (i + 1 < refs.length && refString.equals("origin/" + refs[i + 1])) {
                    addRefButtonWithText(linePane, refString, "origin");
                    addLabel(linePane, "/");
                    ++i;
                    refString = refs[i];
                }

                addRefButton(linePane, refString);
            }
        }
        addLabel(linePane, ") ");
    }

    private void addRefButton(Pane linePane, String ref) {
        addRefButtonWithText(linePane, ref, ref);
    }

    private void addRefButtonWithText(Pane linePane, String ref, String text) {
        Button button = new Button(text);
        button.setPadding(Insets.EMPTY);
        button.setOnMousePressed(event -> refButtonClicked(ref, event));
        linePane.getChildren().add(button);
    }

    private void refButtonClicked(String ref, MouseEvent event) {
        System.out.println("Click " + ref);
        if (event.getButton() == MouseButton.PRIMARY) {
            System.out.println("Checkout " + ref);
            shellInteraction.checkout(ref);
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            System.out.println("Copy " + ref);
            ClipboardContent content = new ClipboardContent();
            content.putString(ref);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }
}
