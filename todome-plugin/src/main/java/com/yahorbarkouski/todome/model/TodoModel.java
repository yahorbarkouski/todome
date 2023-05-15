package com.yahorbarkouski.todome.model;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

public class TodoModel {
    private final String assignee;
    @Nullable
    private final LocalDate dueDate;
    private final String filePath;
    private final int lineNumber;
    private final int columnNumber;

    public TodoModel(
            String assignee,
            @Nullable LocalDate dueDate,
            String filePath,
            int lineNumber,
            int columnNumber
    ) {
        this.assignee = assignee;
        this.dueDate = dueDate;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public String getAssignee() {
        return assignee;
    }

    public @Nullable LocalDate getDueDate() {
        return dueDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}