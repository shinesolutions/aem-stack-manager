package com.shinesolutions.aemstackmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Represents the message but for a standard AEM Stack Manager message
 */
public class TaskMessage {

    @JsonProperty("task")
    private String task;

    @JsonProperty("stack_prefix")
    private String stackPrefix;

    @JsonProperty("details")
    private Details details;

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getStackPrefix() {
        return stackPrefix;
    }

    public void setStackPrefix(String stackPrefix) {
        this.stackPrefix = stackPrefix;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

}
