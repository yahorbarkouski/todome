package com.yahorbarkouski.todome;

import com.yahorbarkouski.todome.extension.ToDoMeExtension;
import com.yahorbarkouski.todome.task.ListTodosTask;
import com.yahorbarkouski.todome.task.VerifyTodosTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

@SuppressWarnings("unused")
public class ToDoMePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("todome", ToDoMeExtension.class);
        project.getTasks().create("verifyTodos", VerifyTodosTask.class);
        project.getTasks().create("listTodos", ListTodosTask.class);
    }
}
