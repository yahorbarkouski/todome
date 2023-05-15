package com.yahorbarkouski.todome.task;

import com.yahorbarkouski.todome.model.TodoModel;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

public class VerifyTodosTask extends AbstractTodoTask {

    /**
     * Verifies all the todos present in the project's root directory,
     * extracts all todos and generates a report of filtered todos.
     */
    @TaskAction
    public void verifyTodos() {
        File srcDir = getProject().getRootDir();
        report(extractTodos(srcDir, filtered()));
    }

    /**
     * Reports the list of todos. If the list is empty, returns immediately.
     * For each TodoModel in the list, formats it and logs it with the logger at the lifecycle level.
     * If any TodoModel in the list does not have a valid due date, throws an IllegalStateException
     * with a message indicating the log should be checked for more details.
     *
     * @param todos the list of TodoModels to report
     * @throws IllegalStateException if any TodoModel in the list does not have a valid due date
     */
    @Override
    public void report(List<TodoModel> todos) {
        if (todos.isEmpty()) return;

        for (TodoModel todo : todos) {
            logger.lifecycle(formatted(todo));
        }
        throw new IllegalStateException("Found TODOs without a valid due date. Please check the log for more details.");
    }

    /**
     * Returns a formatted string containing information about a TODO task.
     * The method checks if the task has an assigned person. If the person is 'unassigned',
     * it is displayed in red color, otherwise displayed in blue color. Along with the
     * assignee information, the method displays the file path, line number, column number
     * and message indicating the missing due date of the TODO.
     *
     * @param todo the task to be formatted
     * @return a formatted string containing the information about the task
     */
    @Override
    public String formatted(TodoModel todo) {
        String colorAssignee =
                todo.getAssignee().equals("unassigned")
                        ? "\u001B[31m" + todo.getAssignee() + "\u001B[0m"
                        : "\u001B[34m" + todo.getAssignee() + "\u001B[0m";
        return
                "\u001B[93mFound " +
                        colorAssignee +
                        " \u001B[93mTODO in " +
                        todo.getFilePath() + ":" + todo.getLineNumber() + ":" + todo.getColumnNumber() +
                        " without a valid due date.\u001B[0m";
    }

    @Override
    public Predicate<TodoModel> filtered() {
        return todo -> todo.getDueDate() == null;
    }
}


