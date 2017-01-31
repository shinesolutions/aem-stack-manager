package com.shinesolutions.aemstackmanager.handler;

import org.springframework.stereotype.Component;

import com.shinesolutions.aemstackmanager.model.TaskMessage;

@Component
public class PromoteAuthorTaskHandler implements TaskHandler {
    

    public boolean handleTask(TaskMessage message) {
        //Do stuff
        return false;
    }

}
