package com.shinesolutions.aemstackmanager.service;

import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class CommandExecutor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${command.shell}")
    private String shell;

    @Value("${command.timeout}")
    private long timeout;

    public boolean execute(String command){

        logger.debug("Executing command: " + command);

        CommandLine commandLine = new CommandLine(shell);

        commandLine.addArguments(new String[] {
                "-c",
                command
        },false);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        Executor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        executor.setExitValue(1);
        executor.setWatchdog(watchdog);

        try {

            executor.execute(commandLine, resultHandler);

            // some time later the result handler callback was invoked so we
            // can safely request the exit value
            resultHandler.waitFor();
            int exitValue = resultHandler.getExitValue();

            if (exitValue == 0){
                logger.debug("Command execution successful. output: " + outputStream.toString());
            } else {
                throw resultHandler.getException();
            }

        } catch (ExecuteException e){

            logger.error("Error executing command: " + command + ", output: " + outputStream.toString(), e);

            //remove from queue
            return true;

        } catch (IOException | InterruptedException e) {

            logger.error("Error executing command: " + command + ", output: " + outputStream.toString(), e);

            //leave in queue for retry.
            return false;
        }

        return true;

    }

}
