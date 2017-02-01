package com.shinesolutions.aemstackmanager.handler;

import com.shinesolutions.aemstackmanager.model.TaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeployArtifactsTaskHandler implements TaskHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean handleTask(TaskMessage message) {

        logger.debug("Handling Task: " + message);

        // Do stuff
        return false;
    }

}
