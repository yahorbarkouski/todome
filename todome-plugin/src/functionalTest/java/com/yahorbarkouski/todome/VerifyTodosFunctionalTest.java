package com.yahorbarkouski.todome;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class VerifyTodosFunctionalTest {

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
                .withArguments("verifyTodos")
                .withProjectDir(testProjectDir.getRoot())
                .build();

        assertEquals("SUCCESS", Objects.requireNonNull(result.task(":verifyTodos")).getOutcome().name());
    }

    @Test
    public void taskFailsWithoutDueDateInTodoUnassigned() throws IOException {
        File srcFile = new File(srcDir, "Test.java");
        writeString(srcFile, "// TODO: This unassigned comment does not contain a due date");

        try {
            GradleRunner.create()
                    .withProjectDir(testProjectDir.getRoot())
                    .withArguments("verifyTodos")
                    .withPluginClasspath()
                    .build();

            fail("Expected build to fail due to TODO without due date");
        } catch (UnexpectedBuildFailure e) {
            String errorMessage = e.getMessage();
            assertTrue(errorMessage.contains("unassigned"));
            assertTrue(errorMessage.contains("TODO in " + srcFile.getCanonicalPath() + ":1:1"));
        }
    }

    @Test
    public void taskFailsWithoutDueDateInTodoAssigned() throws IOException {
        File srcFile = new File(srcDir, "Test.java");
        writeString(srcFile, "// TODO: This assigned to @yahor comment does not contain a due date");

        try {
            GradleRunner.create()
                    .withProjectDir(testProjectDir.getRoot())
                    .withArguments("verifyTodos")
                    .withPluginClasspath()
                    .build();

            fail("Expected build to fail due to TODO without due date");
        } catch (UnexpectedBuildFailure e) {
            String errorMessage = e.getMessage();
            assertTrue(errorMessage.contains("yahor"));
            assertTrue(errorMessage.contains("TODO in " + srcFile.getCanonicalPath() + ":1:1"));
        }
    }

    @Test
    public void taskSucceededWithDueDateInTodo() throws IOException {
        File srcFile = new File(srcDir, "Test.java");
        writeString(srcFile, "// TODO: this comment contain due to 10.10.2023, text");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("verifyTodos")
                .withPluginClasspath()
                .build();

        assertEquals("SUCCESS", Objects.requireNonNull(result.task(":verifyTodos")).getOutcome().name());
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
