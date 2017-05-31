package com.shinesolutions.aemstackmanager.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemstackmanager.model.TaskMessage;
import com.shinesolutions.aemstackmanager.service.CommandExecutor;
import org.apache.commons.exec.ExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OfflineCompactionSnapshotTaskHandler implements TaskHandler {

    private static final String AUTHOR_PRIMARY = "author-primary";
    private static final String AUTHOR_STANDBY = "author-standby";
    private static final String PUBLISH = "publish";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${command.stackComponents}")
    private String stackComponentsCommand;

    @Value("${command.stopAem}")
    private String stopAemCommand;

    @Value("${command.offlineCompactionForAuthor}")
    private String offlineCompactionForAuthorCommand;

    @Value("${command.offlineCompactionByIdentity}")
    private String offlineCompactionByIdentityCommand;

    @Value("${command.offlineSnapshot}")
    private String offlineSnapshotCommand;

    @Value("${command.startAem}")
    private String startAemCommand;

    @Value("${command.enterStandbyByComponent}")
    private String enterStandbyByComponentCommand;

    @Value("${command.exitStandbyByComponent}")
    private String exitStandbyByComponentCommand;

    @Value("${command.enterStandbyByIdentity}")
    private String enterStandbyByIdentityCommand;

    @Value("${command.exitStandbyByIdentity}")
    private String exitStandbyByIdentityCommand;

    @Value("${command.ec2Metadata}")
    private String ec2MetadataCommand;

    @Value("${command.pairedInstance}")
    private String pairedInstanceCommand;

    @Value("${command.waitUntilReady}")
    private String waitUntilReadyCommand;

    @Value("${command.checkOakRunProcess}")
    private String checkOakRunProcessCommand;

    @Value("${command.checkCrxQuickstartProcess}")
    private String checkCrxQuickstartProcessCommand;

    @Value("${command.checkSnapshotProcess}")
    private String checkSnapshotProcessCommand;

    @Value("${aemStop.sleepSeconds}")
    private int aemStopSleepSeconds;

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
                executeOfflineCompactionSnapshot(stackPrefix, stack);

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

    private void executeOfflineCompactionSnapshot(String stackPrefix, Map<String, String> stack) throws IOException, InterruptedException {

        String authorPrimaryIdentity = null;
        String authorStandbyIdentity = null;
        String publishIdentity = null;
        String publishDispatcherIdentity = null;

        for (String identity : stack.keySet()) {

            if (AUTHOR_PRIMARY.equals(stack.get(identity))) {
                authorPrimaryIdentity = identity;
            } else if (AUTHOR_STANDBY.equals(stack.get(identity))) {
                authorStandbyIdentity = identity;
            } else if (PUBLISH.equals(stack.get(identity))) {
                publishIdentity = identity;
                publishDispatcherIdentity = findPairedPublishDispatcher(publishIdentity);
            }

            if (authorPrimaryIdentity != null && authorStandbyIdentity != null && publishIdentity != null) {
                break;
            }

        }

        executeOfflineCompactionSnapshotForAuthor(stackPrefix, authorPrimaryIdentity, authorStandbyIdentity);

        executeOfflineCompactionSnapshotForPublish(publishIdentity, publishDispatcherIdentity);

        executeOfflineCompactionForRemainingPublish(stack, publishIdentity);

    }

    private void executeOfflineCompactionForRemainingPublish(Map<String, String> stack, String compactedPublishIdentity) throws IOException, InterruptedException {

        String publishDispatcherIdentity;

        //wait for compactedPublishInstance to be healthy
        commandExecutor.execute(waitUntilReadyCommand.replaceAll("\\{identity}", compactedPublishIdentity));

        //cycle through remaining publish instances and compact.
        for(String remainingPublishIdentity: getRemainingPublishIdentities(stack, compactedPublishIdentity)){

            publishDispatcherIdentity = findPairedPublishDispatcher(remainingPublishIdentity);

            //move the selected publish-dispatcher into standby mode
            commandExecutor.execute(enterStandbyByIdentityCommand.replaceAll("\\{identity}", publishDispatcherIdentity));

            //stop aem on publish instance
            commandExecutor.execute(stopAemCommand.replaceAll("\\{identity}", remainingPublishIdentity));

            //Wait for the crx-quickstart process to no longer exist before starting compaction.
            checkProcessEnded(checkCrxQuickstartProcessCommand, 24, 5, remainingPublishIdentity);

            //sleep after stop aem
            Thread.sleep(aemStopSleepSeconds * 1000);

            //run offline-compaction on publish instance
            commandExecutor.execute(offlineCompactionByIdentityCommand.replaceAll("\\{identity}", remainingPublishIdentity));

            //Wait for compaction processes to no longer exist before starting aem.
            checkProcessEnded(checkOakRunProcessCommand, 360, 10, remainingPublishIdentity);

            //start aem on publish instance
            commandExecutor.execute(startAemCommand.replaceAll("\\{identity}", remainingPublishIdentity));

            //move the selected publish-dispatcher out of standby mode
            commandExecutor.execute(exitStandbyByIdentityCommand.replaceAll("\\{identity}", publishDispatcherIdentity));

            //wait for remainingPublishIdentity to be healthy before continuing
            commandExecutor.execute(waitUntilReadyCommand.replaceAll("\\{identity}", remainingPublishIdentity));

        }

    }

    private void checkProcessEnded(String processCommand, int repeatCount, int sleepSeconds, String identity) throws IOException, InterruptedException {

        String command = processCommand.replaceAll("\\{identity}", identity);

        boolean processEnded = false;

        for(int i = 0; i < repeatCount; i++){

            logger.debug("Attempt " + (i + 1) + " of " + repeatCount + ". Check Process exists.");

            List<String> output = commandExecutor.executeReturnOutputAsList(command);

            for(int j = 0; j < output.size(); j++){

                String message = output.get(j);

                if(message.contains("Output:")){

                    j++;

                    String response = output.get(j);

                    if(response.trim().equals("0")){
                        processEnded = true;
                    }

                    break;

                }

            }

            if(processEnded){
                logger.debug("Process no longer exists. Continuing.");
                break;
            }


            logger.debug("Attempt " + (i + 1) + " of " + repeatCount + ". Process exists. Sleeping for " + sleepSeconds + " seconds");
            Thread.sleep(sleepSeconds * 1000);

        }

        if(!processEnded){
            throw new ExecuteException("Process has not ended", 1);
        }

    }

    private String[] getRemainingPublishIdentities(Map<String, String> stack, String publishIdentity) {

        List<String> remainingPublishIdentities = new ArrayList<>();

        for (String identity : stack.keySet()) {

            if (PUBLISH.equals(stack.get(identity)) && !identity.equals(publishIdentity)) {

                remainingPublishIdentities.add(identity);

            }

        }

        return remainingPublishIdentities.toArray(new String[remainingPublishIdentities.size()]);
    }


    private void executeOfflineCompactionSnapshotForPublish(String publishIdentity, String publishDispatcherIdentity) throws IOException, InterruptedException {

        //move the selected publish-dispatcher into standby mode
        commandExecutor.execute(enterStandbyByIdentityCommand.replaceAll("\\{identity}", publishDispatcherIdentity));

        //stop aem on publish instance
        commandExecutor.execute(stopAemCommand.replaceAll("\\{identity}", publishIdentity));

        //Wait for the crx-quickstart process to no longer exist before starting compaction.
        checkProcessEnded(checkCrxQuickstartProcessCommand, 24, 5, publishIdentity);

        //sleep after stop aem
        Thread.sleep(aemStopSleepSeconds * 1000);

        //run offline-compaction on publish instance
        commandExecutor.execute(offlineCompactionByIdentityCommand.replaceAll("\\{identity}", publishIdentity));

        //Wait for compaction processes to no longer exist before starting aem.
        checkProcessEnded(checkOakRunProcessCommand, 360, 10, publishIdentity);

        //take ebs snapshot of publish instance
        commandExecutor.execute(offlineSnapshotCommand.replaceAll("\\{identity}", publishIdentity));

        //Wait for the snapshot_backup process to no longer exist before starting aem.
        checkProcessEnded(checkSnapshotProcessCommand, 90, 10, publishIdentity);

        //start aem on publish instance
        commandExecutor.execute(startAemCommand.replaceAll("\\{identity}", publishIdentity));

        //move the selected publish-dispatcher out of standby mode
        commandExecutor.execute(exitStandbyByIdentityCommand.replaceAll("\\{identity}", publishDispatcherIdentity));
    }

    private void executeOfflineCompactionSnapshotForAuthor(String stackPrefix, String authorPrimaryIdentity, String authorStandbyIdentity) throws IOException, InterruptedException {

        //move all author-dispatcher instances into standby
        commandExecutor.execute(enterStandbyByComponentCommand.replaceAll("\\{stack_prefix}", stackPrefix).replaceAll("\\{component}", "author-dispatcher"));

        //stop author-standby
        commandExecutor.execute(stopAemCommand.replaceAll("\\{identity}", authorStandbyIdentity));

        //Wait for the crx-quickstart process to no longer exist before starting compaction.
        checkProcessEnded(checkCrxQuickstartProcessCommand, 24, 5, authorStandbyIdentity);

        //sleep after stop aem
        Thread.sleep(aemStopSleepSeconds * 1000);

        //stop author-primary
        commandExecutor.execute(stopAemCommand.replaceAll("\\{identity}", authorPrimaryIdentity));

        //Wait for the crx-quickstart process to no longer exist before starting compaction.
        checkProcessEnded(checkCrxQuickstartProcessCommand, 24, 5, authorPrimaryIdentity);

        //sleep after stop aem
        Thread.sleep(aemStopSleepSeconds * 1000);

        //run offline-compaction on both author-primary and author-standby
        commandExecutor.execute(offlineCompactionForAuthorCommand.replaceAll("\\{stack_prefix}", stackPrefix));

        //Wait for compaction processes to no longer exist before starting aem.
        checkProcessEnded(checkOakRunProcessCommand, 360, 10, authorPrimaryIdentity);

        //Wait for compaction processes to no longer exist before starting aem.
        checkProcessEnded(checkOakRunProcessCommand, 12, 10, authorStandbyIdentity);

        //take ebs snapshot of author-primary
        commandExecutor.execute(offlineSnapshotCommand.replaceAll("\\{identity}", authorPrimaryIdentity));

        //Wait for the snapshot_backup process to no longer exist before starting aem.
        checkProcessEnded(checkSnapshotProcessCommand, 90, 10, authorPrimaryIdentity);

        //start aem on author-primary
        commandExecutor.execute(startAemCommand.replaceAll("\\{identity}", authorPrimaryIdentity));

        //start aem on author-standby
        commandExecutor.execute(startAemCommand.replaceAll("\\{identity}", authorStandbyIdentity));

        //move all author-dispatcher instances out of standby
        commandExecutor.execute(exitStandbyByComponentCommand.replaceAll("\\{stack_prefix}", stackPrefix).replaceAll("\\{component}", "author-dispatcher"));

    }

    private String findPairedPublishDispatcher(String publishIdentity) throws IOException, InterruptedException {

        String publishEc2Metadata = commandExecutor.executeReturnOutput(ec2MetadataCommand.replaceAll("\\{identity}", publishIdentity));

        ObjectMapper mapper = new ObjectMapper();

        JsonNode ec2metaData = mapper.readTree("{\"objects\" : " + publishEc2Metadata + "}").get("objects");

        String publishInstanceId = ec2metaData.findValue("instance-id").asText();

        return commandExecutor.executeReturnOutput(pairedInstanceCommand.replaceAll("\\{instance_id}", publishInstanceId));

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
