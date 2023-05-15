package com.yahorbarkouski.todome.task;

import com.yahorbarkouski.todome.extension.ToDoMeExtension;
import com.yahorbarkouski.todome.model.TodoModel;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractTodoTask extends DefaultTask {

    protected static final Logger logger = Logging.getLogger(AbstractTodoTask.class);
    public static final Pattern todoPattern = Pattern.compile(
            "(//.?TODO[^\\n]*|/\\*.*?TODO[^*]*\\*/)",
            Pattern.DOTALL
    );
    protected static final List<String> JVM_EXTENSIONS = Arrays.asList(".java", ".kt", ".kts", ".groovy");

    protected ToDoMeExtension extension;

    protected AbstractTodoTask() {
        extension = getProject().getExtensions().findByType(ToDoMeExtension.class);
    }

    // formats model output, according to specific task needs
    abstract String formatted(TodoModel todo);

    // filters model chooser, according to specific task needs
    abstract Predicate<TodoModel> filtered();

    // reports filtered todos output, according to specific task needs
    abstract void report(List<TodoModel> todos);

    /**
     * Extracts all TodoModel objects from all files within a given directory that pass the provided filter condition.
     *
     * @param dir the directory to search for TodoModel objects
     * @param filter the condition that the TodoModel objects must satisfy to be included in the list
     * @return a list of all TodoModel objects found within the directory that satisfy the provided filter condition
     */
    protected List<TodoModel> extractTodos(File dir, Predicate<TodoModel> filter) {
        List<TodoModel> todos = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) return todos;

        for (File file : files) {
            if (file.isDirectory()) {
                todos.addAll(extractTodos(file, filter));
            } else if (isJvmFile(file)) {
                todos.addAll(extractTodosFromFileByFilter(file, filter));
            }
        }
        return todos;
    }

    /**
     * Check if the given file is a JVM file based on its extension.
     *
     * @param file The file to check.
     * @return true if the file has a JVM extension, false otherwise.
     */
    protected boolean isJvmFile(File file) {
        return JVM_EXTENSIONS.stream().anyMatch(file.getName()::endsWith);
    }

    /**
     * Extracts all todo items from a given file that match the provided filter.
     *
     * @param file The file to extract todo items from.
     * @param filter The predicate filter used to match specific todo items.
     * @return A list of TodoModel objects that match the provided filter.
     */
    protected List<TodoModel> extractTodosFromFileByFilter(File file, Predicate<TodoModel> filter) {
        List<TodoModel> todos = new ArrayList<>();

        String fileContent = readFileContent(file);
        Matcher matcher = todoPattern.matcher(fileContent);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(extension.getDateFormat());

        while (matcher.find()) {
            String todoComment = matcher.group();
            String assignee = extractAssignee(todoComment);
            LocalDate dueDate = extractDueDate(todoComment, formatter, extension.getDueDatePrefixes());

            TodoModel todo = new TodoModel(
                    assignee,
                    dueDate,
                    file.getPath(),
                    calculateLineNumber(fileContent, matcher.start()),
                    calculateColumnNumber(fileContent, matcher.start())
            );

            if (filter.test(todo)) {
                todos.add(todo);
            }
        }

        return todos;
    }

    /**
     * Reads the content of the specified file.
     *
     * @param file the file to read
     * @return the content of the file as a String
     * @throws IllegalStateException if an error occurs while reading the file
     */
    protected String readFileContent(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file " + file.getName(), e);
        }
    }

    /**
     * Extracts the name of the todo assignee from the given todo comment.
     * If the todo comment does not contain an assignee, returns "unassigned".
     *
     * @param todoComment the todo comment from which to extract the assignee
     * @return the name of the assignee, or "unassigned" if no assignee was found
     */
    protected String extractAssignee(String todoComment) {
        Matcher assigneeMatcher = Pattern.compile("@\\w+").matcher(todoComment);
        return assigneeMatcher.find() ? assigneeMatcher.group().substring(1) : "unassigned";
    }

    /**
     * Extracts the due date from a todo comment based on given prefixes and a date formatter
     *
     * @param todoComment the string containing the todo comment
     * @param dateFormatter the date formatter to use when parsing the date string from the comment
     * @param dueDatePrefixes the prefixes to search for in the todo comment
     * @return LocalDate representing the due date extracted from the comment, or null if no valid date was found
     */
    protected LocalDate extractDueDate(
            String todoComment,
            DateTimeFormatter dateFormatter,
            List<String> dueDatePrefixes
    ) {
        return dueDatePrefixes.stream()
                .filter(todoComment::contains)
                .findFirst()
                .map(prefix -> {
                    int dueToCommentStartIndex = todoComment.indexOf(prefix) + prefix.length();
                    String dateString = todoComment.substring(
                            dueToCommentStartIndex,
                            Math.min(
                                    todoComment.length(),
                                    dueToCommentStartIndex + extension.getDateFormat().length() + 1
                            )
                    ).trim();
                    try {
                        return LocalDate.parse(dateString, dateFormatter);
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                }).orElse(null);
    }

    /**
     * Calculates the line number of the specified character position in a given string.
     * @param content the string to calculate the line number from
     * @param pos the character position in the string to calculate the line number for
     * @return the line number at the specified character position
     */
    protected int calculateLineNumber(String content, int pos) {
        return content.substring(0, pos).split("\n").length;
    }

    /**
     * Calculates the column number based on the content string and the current position.
     *
     * @param content the string content to calculate the column number from
     * @param pos the current position to calculate the column number from
     * @return the column number
     */
    protected int calculateColumnNumber(String content, int pos) {
        String[] lines = content.substring(0, pos).split("\n");
        return lines[lines.length - 1].length() + 1;
    }
}
