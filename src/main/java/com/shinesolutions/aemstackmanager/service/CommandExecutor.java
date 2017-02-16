package com.shinesolutions.aemstackmanager.service;

import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

        LogOutputStream outputStream = new LogOutputStream() {
            @Override
            protected void processLine(String line, int level) {
                logger.debug(line);
            }
        };

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

            logger.debug("Command execution complete. exitValue: " + exitValue);

        } catch (ExecuteException e){

            logger.error("Error executing command: " + command + ", check logs for errors", e);

            //remove from queue
            return true;

        } catch (IOException | InterruptedException e) {

            logger.error("Error executing command: " + command + ", check logs for errors", e);

            //leave in queue for retry.
            return false;
        }

        return true;

    }

}
