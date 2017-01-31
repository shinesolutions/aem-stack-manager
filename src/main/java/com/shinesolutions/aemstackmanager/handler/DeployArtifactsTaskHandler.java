package com.shinesolutions.aemstackmanager.handler;

import com.shinesolutions.aemstackmanager.model.TaskMessage;
import org.springframework.stereotype.Component;

@Component
public class DeployArtifactsTaskHandler implements TaskHandler {

    public boolean handleTask(TaskMessage message) {
        // Do stuff
        return false;
    }

}
