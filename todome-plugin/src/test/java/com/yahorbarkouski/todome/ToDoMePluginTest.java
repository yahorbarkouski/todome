package com.yahorbarkouski.todome;

import com.yahorbarkouski.todome.task.AbstractTodoTask;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.Project;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ToDoMePluginTest {

    @Test
    public void pluginRegistersATask() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("com.yahorbarkouski.todome");

        assertNotNull(project.getTasks().findByName("verifyTodos"));
        assertNotNull(project.getTasks().findByName("listTodos"));
    }

    @Test
    public void todoPatternApplicable() {
        Pattern todoPattern = AbstractTodoTask.todoPattern;
        // Single line TODO comment with newline
        Matcher matcher1 = todoPattern.matcher("// TODO: fix this\n");
        assertTrue(matcher1.find());

        // Single line TODO comment without newline
        Matcher matcher2 = todoPattern.matcher("// TODO: fix this");
        assertTrue(matcher2.find());

        // Multi line TODO comment with newline
        Matcher matcher3 = todoPattern.matcher("/* TODO: fix this */\n");
        assertTrue(matcher3.find());

        // Multi line TODO comment without newline
        Matcher matcher4 = todoPattern.matcher("/* TODO: fix this */");
        assertTrue(matcher4.find());
    }
}
