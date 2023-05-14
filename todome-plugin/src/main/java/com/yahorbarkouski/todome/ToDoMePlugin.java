package com.yahorbarkouski.todome;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

@SuppressWarnings("unused")
public class ToDoMePlugin implements Plugin<Project> {

    public void apply(Project project) {
        project.getTasks().create("verifyTodos", VerifyTodosTask.class);
    }
}
