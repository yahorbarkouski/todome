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
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class VerifyTodosExtensionFunctionalTest {

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
                                        
                        todome {
                            dueDatePrefixes = listOf("by date of")
                            dateFormat = "d MMM yyyy"
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
    public void taskSucceededWithCustomDueDateInTodo() throws IOException {
        File srcFile = new File(srcDir, "Test.java");
        writeString(srcFile, "// TODO: should be solved by date of 3 Dec 2011");

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
