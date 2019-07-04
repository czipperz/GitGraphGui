package com.github.czipperz.gitgui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ShellInteraction {
    private String directory;

    public ShellInteraction(String directory) {
        this.directory = directory;
    }

    public void checkout(String ref) {
        runGitCommand(out -> {}, err -> {}, "checkout", ref);
    }

    public List<String> getGraphLines() {
        List<String> lines = new ArrayList<>();
        runGitCommand(lines::add, System.err::println, "log", "--graph", "--format=%h - [%D] %s", "--abbrev-commit", "--all");
        return lines;
    }

    private void runGitCommand(Consumer<String> outConsumer, Consumer<String> errConsumer, String... commandArguments) {
        try {
            Process process = new ProcessBuilder().command(buildCommand(commandArguments)).start();

            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // we must manually read because Process's buffer fills up and it deadlocks
            while (true) {
                String line;
                if ((line = stdout.readLine()) != null) {
                    outConsumer.accept(line);
                } else if ((line = stderr.readLine()) != null) {
                    errConsumer.accept(line);
                } else {
                    break;
                }
            }

            if (process.waitFor() != 0) {
                throw new IOException("git failed with exit code " + process.exitValue());
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private String[] buildCommand(String[] commandArguments) {
        List<String> gitArguments = new ArrayList<>();
        gitArguments.add("git");
        gitArguments.add("--no-pager");
        if (directory != null) {
            gitArguments.add("-C");
            gitArguments.add(directory);
        }
        String[] command = new String[gitArguments.size() + commandArguments.length];
        for (int i = 0; i < gitArguments.size(); ++i) {
            command[i] = gitArguments.get(i);
        }
        for (int i = 0; i < commandArguments.length; ++i) {
            command[i + gitArguments.size()] = commandArguments[i];
        }
        return command;
    }
}
