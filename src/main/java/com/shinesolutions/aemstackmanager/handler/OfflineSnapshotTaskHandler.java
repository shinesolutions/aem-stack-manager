package com.shinesolutions.aemstackmanager.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemstackmanager.model.TaskMessage;
import com.shinesolutions.aemstackmanager.service.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OfflineSnapshotTaskHandler implements TaskHandler {

    private static final String AUTHOR_PRIMARY = "author-primary";
    private static final String AUTHOR_STANDBY = "author-standby";
    private static final String PUBLISH = "publish";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${command.stackComponents}")
    private String stackComponentsCommand;

    @Value("${command.stopAem}")
    private String stopAemCommand;

    @Value("${command.offlineCompaction}")
    private String offlineCompactionCommand;

    @Value("${command.offlineSnapshot}")
    private String offlineSnapshotCommand;

    @Value("${command.startAem}")
    private String startAemCommand;

    @Resource
    private CommandExecutor commandExecutor;

    @Value("${offlineSnapshot.minimumPublishInstances:2}")
    private int minimumPublishInstances;


    //return true - remove from queue, return false leave on the queue
    public boolean handleTask(TaskMessage message) {

        logger.debug("Handling Task: " + message);

        String stackPrefix = message.getStackPrefix();

        try {

            Map<String, String> stack = getStack(stackPrefix);

            if (isHealthy(stack)) {

                //execute offline snapshot
                executeOfflineSnapshot(stackPrefix, stack);

                //successful remove from queue
                return true;

            } else {

                logger.error("Error Stack : " + stackPrefix + " is not healthy can not perform offline snapshot, check logs for errors");

                //remove from queue
                return true;
            }


        } catch (IOException | InterruptedException e) {

            logger.error("Error performing offline snapshot, check logs for errors", e);

            //remove from queue, we can not re-run if there is an interruption midway through the offline snapshot
            return true;

        }


    }

    private void executeOfflineSnapshot(String stackPrefix, Map<String, String> stack) throws IOException, InterruptedException {

        String authorPrimaryIdentity = null;
        String authorStandbyIdentity = null;
        String publishIdentity = null;

        for (String identity : stack.keySet()) {

            if (AUTHOR_PRIMARY.equals(stack.get(identity))) {
                authorPrimaryIdentity = identity;
            } else if (AUTHOR_STANDBY.equals(stack.get(identity))) {
                authorStandbyIdentity = identity;
            } else if (PUBLISH.equals(stack.get(identity))) {
                publishIdentity = identity;
            }

            if (authorPrimaryIdentity != null && authorStandbyIdentity != null && publishIdentity != null) {
                break;
            }

        }


        //stop author-standby
        commandExecutor.execute(stopAemCommand.replaceAll("\\{identity}", authorStandbyIdentity));

        //stop author-primary
        commandExecutor.execute(stopAemCommand.replaceAll("\\{identity}", authorPrimaryIdentity));

        //run offline-compaction on both author-primary and author-standby
        commandExecutor.execute(offlineCompactionCommand.replaceAll("\\{stack_prefix}", stackPrefix));

        //take ebs snapshot of author-primary
        commandExecutor.execute(offlineSnapshotCommand.replaceAll("\\{identity}", authorPrimaryIdentity));

        //start aem on author-primary
        commandExecutor.execute(startAemCommand.replaceAll("\\{identity}", authorPrimaryIdentity));

        //start aem on author-standby
        commandExecutor.execute(startAemCommand.replaceAll("\\{identity}", authorStandbyIdentity));

        //stop aem on publish instance
        commandExecutor.execute(stopAemCommand.replaceAll("\\{identity}", publishIdentity));

        //take ebs snapshot of publish instance
        commandExecutor.execute(offlineSnapshotCommand.replaceAll("\\{identity}", publishIdentity));

        //start aem on publish instance
        commandExecutor.execute(startAemCommand.replaceAll("\\{identity}", publishIdentity));

    }

    private Map<String, String> getStack(String stackPrefix) throws IOException, InterruptedException {

        Map<String, String> stack = new HashMap<>();

        //get stack components
        String builtCommand = stackComponentsCommand.replaceAll("\\{stack_prefix}", stackPrefix);
        String stackComponents = commandExecutor.executeReturnOutput(builtCommand);

        ObjectMapper mapper = new ObjectMapper();

        JsonNode components = mapper.readTree("{\"objects\" : " + stackComponents + "}").get("objects");

        for (JsonNode component : components) {

            String sender = component.get("sender").asText();
            String componentValue = component.get("data").get("value").asText();

            stack.put(sender, componentValue);
        }

        return stack;

    }


    private boolean isHealthy(Map<String, String> stack) {

        //check stack as 1 author-primary, 1 author-standby and the minimum publish instances

        int authorPrimaryCount = 0;
        int authorStandbyCount = 0;
        int publishCount = 0;

        for (String instance : stack.keySet()) {

            String component = stack.get(instance);

            if (AUTHOR_PRIMARY.equals(component)) {

                authorPrimaryCount++;

            }

            if (AUTHOR_STANDBY.equals(component)) {

                authorStandbyCount++;

            }

            if (PUBLISH.equals(component)) {

                publishCount++;

            }

        }

        if (authorPrimaryCount == 1 && authorStandbyCount == 1 && publishCount >= minimumPublishInstances) {
            return true;
        }

        if (authorPrimaryCount != 1) {
            logger.error("Found " + authorPrimaryCount + " author-primary instances. Unhealthy stack.");
        }

        if (authorStandbyCount != 1) {
            logger.error("Found " + authorPrimaryCount + " author-standby instances. Unhealthy stack.");
        }

        if (publishCount < minimumPublishInstances) {
            logger.error("Found " + publishCount + " publish instances. Unhealthy stack.");
        }


        return false;


    }

}
