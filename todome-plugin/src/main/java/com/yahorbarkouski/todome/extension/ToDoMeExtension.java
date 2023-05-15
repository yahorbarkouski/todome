package com.yahorbarkouski.todome.extension;

import java.util.List;

@SuppressWarnings("unused")
public class ToDoMeExtension {
    private List<String> dueDatePrefixes = List.of("due to");
    private String dateFormat = "dd.MM.yyyy";
    private String mentionSymbol = "@";

    public List<String> getDueDatePrefixes() { return this.dueDatePrefixes; }
    public void setDueDatePrefixes(List<String> dueDatePrefixes) { this.dueDatePrefixes = dueDatePrefixes; }

    public String getDateFormat() { return this.dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }

    public String getMentionSymbol() { return this.mentionSymbol; }
    public void setMentionSymbol(String mentionSymbol) { this.mentionSymbol = mentionSymbol; }
}
