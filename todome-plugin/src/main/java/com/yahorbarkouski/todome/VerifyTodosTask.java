package com.yahorbarkouski.todome;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyTodosTask extends DefaultTask {

    // applies to both /* */ and // comments
    public static final Pattern todoPattern =
            Pattern.compile("/\\*.*?TODO.*?\\*/|//.*?TODO.*($|\n)", Pattern.DOTALL);

    public VerifyTodosTask() {
        setDescription("Verifies all TODO comments have a due date.");
        setGroup("Verification");
    }

    @TaskAction
    public void verifyTodos() {
        File srcDir = getProject().file("src");
        checkDirForTodos(srcDir);
    }

    private void checkDirForTodos(File dir) {
        dir.listFiles(file -> {
            if (file.isFile() && containsJvmExtension(file)) {
                checkFileForTodos(file);
            }
            return false;
        });
    }

    private boolean containsJvmExtension(File file) {
        String fileName = file.getName();
        List<String> extensions = Arrays.asList(".java", ".kt", ".kts", ".groovy");
        return extensions.stream().anyMatch(fileName::endsWith);
    }

    private void checkFileForTodos(File file) {
        String fileContent;
        try {
            fileContent = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file " + file.getName(), e);
        }

        Matcher matcher = todoPattern.matcher(fileContent);
        while (matcher.find()) {
            String todoComment = matcher.group();
            if (!todoComment.contains("due to")) {
                throw new IllegalStateException(
                        "Found a TODO in " + file.getName() + " without a due date:\n" + todoComment
                );
            }
        }
    }
}