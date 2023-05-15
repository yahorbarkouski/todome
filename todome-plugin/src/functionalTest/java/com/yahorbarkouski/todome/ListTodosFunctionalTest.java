package com.yahorbarkouski.todome;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ListTodosFunctionalTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private File srcDir;

    @Before
    public void setup() throws IOException {
        File settingsFile = testProjectDir.newFile("settings.gradle.kts");
        File buildFile = testProjectDir.newFile("build.gradle.kts");
        srcDir = testProjectDir.newFolder("src");
        writeString(settingsFile, "");
        writeString(
                buildFile,
                """
                        plugins {
                            id("com.yahorbarkouski.todome")
                        }
                      """
        );
    }

    @Test
    public void canRunTask() {
        BuildResult result = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("listTodos")
                .withProjectDir(testProjectDir.getRoot())
                .build();

        assertEquals("SUCCESS", Objects.requireNonNull(result.task(":listTodos")).getOutcome().name());
    }

    @Test
    public void taskSucceededWithDueDateInTodo() throws IOException {
        File srcFile = new File(srcDir, "Test.java");
        writeString(
                srcFile,
                """
                    // TODO: should be solved by @yahor due to 10.10.2023
                    // TODO: should not be solved by @damna, date is not specified"
                """
        );

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("listTodos")
                .withPluginClasspath()
                .build();

        assertEquals("SUCCESS", Objects.requireNonNull(result.task(":listTodos")).getOutcome().name());
        assertTrue(result.getOutput().contains("yahor"));
        assertTrue(result.getOutput().contains("due date of 2023-10-10"));
        assertFalse(result.getOutput().contains("damna"));
    }

    @Test
    public void listTodosGetAssignedTodos() throws IOException {
        File srcFile = new File(srcDir, "Test.java");
        writeString(
                srcFile,
                """
                    // TODO: should be solved by @yahor due to 10.10.2023
                    // TODO: should be solved by @damna due to 10.11.2023
                """
        );

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("listTodos", "-Passignee=yahor")
                .withPluginClasspath()
                .build();

        assertEquals("SUCCESS", Objects.requireNonNull(result.task(":listTodos")).getOutcome().name());
        assertTrue(result.getOutput().contains("yahor"));
        assertTrue(result.getOutput().contains("due date of 2023-10-10"));
        assertFalse(result.getOutput().contains("damna"));
    }

    @Test
    public void listTodosSortsTodosBySort() throws IOException {
        File srcFile = new File(srcDir, "Test.java");
        writeString(
                srcFile,
                """
                    // TODO: should be solved by @yahor due to 10.10.2023
                    // TODO: should be solved by @yahor due to 10.09.2023
                    // TODO: should be solved by @yahor due to 10.12.2023
                """
        );

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("listTodos", "-Psort=asc")
                .withPluginClasspath()
                .build();

        assertEquals("SUCCESS", Objects.requireNonNull(result.task(":listTodos")).getOutcome().name());

        String buildOutput = result.getOutput();
        List<String> todoLines = Arrays.stream(buildOutput.split("\n"))
                .filter(line -> line.contains("yahor"))
                .toList();

        assertTrue(todoLines.get(0).contains("2023-09-10"));
        assertTrue(todoLines.get(1).contains("2023-10-10"));
        assertTrue(todoLines.get(2).contains("2023-12-10"));

        // desc order, by default
        result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("listTodos")
                .withPluginClasspath()
                .build();

        buildOutput = result.getOutput();
        todoLines = Arrays.stream(buildOutput.split("\n"))
                .filter(line -> line.contains("yahor"))
                .toList();

        assertTrue(todoLines.get(0).contains("2023-12-10"));
        assertTrue(todoLines.get(1).contains("2023-10-10"));
        assertTrue(todoLines.get(2).contains("2023-09-10"));
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.append(string);
        }
    }
}
