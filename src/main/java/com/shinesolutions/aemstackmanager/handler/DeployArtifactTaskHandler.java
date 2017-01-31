package com.shinesolutions.aemstackmanager.handler;

import com.shinesolutions.aemstackmanager.model.TaskMessage;
import org.springframework.stereotype.Component;

@Component
public class DeployArtifactTaskHandler implements TaskHandler {

    public boolean handleTask(TaskMessage message) {
        // Do stuff
        return false;
    }

}
