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

import java.io.IOException;
import java.util.*;

public class GraphPaneUpdater implements Runnable {
    private ScrollPane scrollPane;
    private Pane parent;
    private ShellInteraction shellInteraction;
    private List<String> previousLines = new ArrayList<>();
    private boolean isDirty = false;

    public GraphPaneUpdater(ScrollPane scrollPane, Pane parent, ShellInteraction shellInteraction) {
        this.scrollPane = scrollPane;
        this.parent = parent;
        this.shellInteraction = shellInteraction;
    }

    @Override
    public void run() {
        List<String> graphLines = null;
        try {
            graphLines = shellInteraction.getGraphLines();
        } catch (IOException e) {
            System.err.println("Error: Cannot get graph lines: " + e.getMessage());
            System.exit(1);
        }

        boolean isDirty = shellInteraction.isDirty();
        if (graphLines.equals(previousLines) && this.isDirty == isDirty) {
            return;
        }
        this.isDirty = isDirty;

        previousLines = graphLines;

        Pane linesPane = new VBox();
        linesPane.getStyleClass().add("lines");

        for (String line : graphLines) {
            linesPane.getChildren().add(processLine(line));
        }

        Platform.runLater(() -> {
            parent.getChildren().clear();
            parent.getChildren().add(linesPane);

            double hvalue = scrollPane.getHvalue();
            double vvalue = scrollPane.getVvalue();
            scrollPane.layout();
            scrollPane.setHvalue(hvalue);
            scrollPane.setVvalue(vvalue);
        });
    }

    private Node processLine(String line) {
        Pane linePane = new HBox();
        linePane.getStyleClass().add("line");

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

    private void addLabel(Pane linePane, String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label");
        linePane.getChildren().add(label);
    }

    private void addHeadLabel(Pane linePane, String name) {
        Label label = new Label(name);
        label.setId("head");
        if (isDirty) {
            label.getStyleClass().add("dirty");
        } else {
            label.getStyleClass().add("clean");
        }
        label.setOnMousePressed(e -> copy(name));
        label.getStyleClass().add("label");
        linePane.getChildren().add(label);
    }

    private void addRefs(Pane linePane, String refs) {
        addLabel(linePane, "(");
        boolean first = true;
        for (Ref ref : parseRefs(refs)) {
            if (first) {
                first = false;
            } else {
                addLabel(linePane, ", ");
            }

            displayRef(linePane, ref);
        }
        addLabel(linePane, ") ");
    }

    private Collection<Ref> parseRefs(String refs) {
        Map<String, Ref> result = new TreeMap<>();
        for (String refName : refs.split(", ")) {
            Ref ref = new Ref(refName);

            if (ref.isFilteredOut()) {
                continue;
            }

            String remote = "origin/";

            // handle x, remote/x
            if (ref.name.startsWith(remote)) {
                Ref nonOriginRef = result.get(ref.name.substring(remote.length()));
                if (nonOriginRef != null) {
                    nonOriginRef.isTrackingOrigin = true;
                    continue;
                }
            }

            // handle remote/x, x
            if (result.containsKey(remote + ref.name)) {
                result.remove(remote + ref.name);
                ref.isTrackingOrigin = true;
            }

            result.put(ref.name, ref);
        }
        return result.values();
    }

    private void displayRef(Pane linePane, Ref ref) {
        if (ref.name.equals("HEAD")) {
            addHeadLabel(linePane, ref.name);
        } else {
            if (ref.isTag) {
                addLabel(linePane, "tag: ");
            }

            if (ref.isTrackingOrigin) {
                addRefButtonWithText(linePane, "origin/" + ref.name, "origin");
                addLabel(linePane, "/");
            }

            if (ref.isHead) {
                addHeadLabel(linePane, ref.name);
            } else {
                addRefButton(linePane, ref.name);
            }
        }
    }

    private void addRefButton(Pane linePane, String ref) {
        addRefButtonWithText(linePane, ref, ref);
    }

    private void addRefButtonWithText(Pane linePane, String ref, String text) {
        Button button = new Button(text);
        button.setMnemonicParsing(false);
        button.getStyleClass().add("ref");
        button.setPadding(Insets.EMPTY);
        button.setOnMousePressed(event -> refButtonClicked(ref, event));
        linePane.getChildren().add(button);
    }

    private void refButtonClicked(String ref, MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            checkout(ref);
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            copy(ref);
        }
    }

    private void checkout(String ref) {
        try {
            shellInteraction.checkout(ref);
        } catch (IOException e) {
            new ErrorPopup("Checkout '" + ref + "' failed").show();
        }
    }

    private void copy(String ref) {
        ClipboardContent content = new ClipboardContent();
        content.putString(ref);
        Clipboard.getSystemClipboard().setContent(content);
    }
}
