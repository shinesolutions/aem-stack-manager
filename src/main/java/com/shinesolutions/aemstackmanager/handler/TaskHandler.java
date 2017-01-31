package com.shinesolutions.aemstackmanager.handler;

import com.shinesolutions.aemstackmanager.model.TaskMessage;

public interface TaskHandler {

    boolean handleTask(TaskMessage message);

}
