package com.yahorbarkouski.todome.task;

import com.yahorbarkouski.todome.model.TodoModel;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Predicate;

public class ListTodosTask extends AbstractTodoTask {

    private final String assigneeFilter = getProject().hasProperty("assignee")
            ? (String) getProject().property("assignee")
            : null;
    private final boolean dueDateAscending = getProject().hasProperty("sort") &&
            Objects.equals(getProject().property("sort"), "asc");

    private final boolean overdue = getProject().hasProperty("overdue") &&
            Objects.equals(getProject().property("overdue"), "true");

    /**
     * Extracts a list of todos from the project's root directory and filters them according to a defined predicate.
     * Then reports the valid todos by putting them in a HashMap based on their due date and generating a report.
     */
    @TaskAction
    public void listTodos() {
        File srcDir = getProject().getRootDir();
        report(extractTodos(srcDir, filtered()));
    }

    /**
     * Reports any filtered todos.
     * If no valid todos are present, nothing happens.
     * The todos are sorted by due date in ascending order,
     * unless {@code dueDateAscending} is set to false,
     * in which case they are sorted in descending order.
     * Each TodoModel is logged as a lifecycle event using logger.
     *
     * @see TodoModel
     */
    @Override
    public void report(List<TodoModel> todos) {
        Map<String, TodoModel> sortedValidTodos = new TreeMap<>();
        todos.forEach(todo -> sortedValidTodos.put(Objects.requireNonNull(todo.getDueDate()).toString(), todo));

        if (todos.isEmpty()) return;

        List<TodoModel> sortedTodos = new ArrayList<>(sortedValidTodos.values());

        if (!dueDateAscending) {
            Collections.reverse(sortedTodos);
        }

        if (overdue) {
            sortedTodos.removeIf(todo -> Objects.requireNonNull(todo.getDueDate()).isAfter(LocalDate.now()));
        }

        for (TodoModel todo : sortedTodos) {
            logger.lifecycle(formatted(todo));
        }

        if (overdue && !sortedTodos.isEmpty()) {
            throw new IllegalStateException("Found TODOs with due dates in the past. TIME TO SOLVE IT!");
        }
    }

    /**
     * Returns a formatted string representation of the given TodoModel object, including its assignee, file path, line
     * number, column number, and due date. If the assignee is "unassigned", the assignee name will be displayed in red,
     * otherwise it will be displayed in blue.
     *
     * @param todo the TodoModel object to format
     * @return a formatted string representation of the given TodoModel object
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
                        " with a due date of " + todo.getDueDate() + ".\u001B[0m";
    }


    /**
     * Returns a predicate that filters TodoModel objects based on due date and assignee.
     * Only TodoModel objects with a non-null due date and the same assignee as the filter
     * (if a filter is set) will be included.
     *
     * @return the predicate for filtering TodoModel objects
     */
    @Override
    public Predicate<TodoModel> filtered() {
        return todo -> todo.getDueDate() != null &&
                (this.assigneeFilter == null || todo.getAssignee().equals(this.assigneeFilter));
    }
}
