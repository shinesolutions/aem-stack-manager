package com.shinesolutions.aemstackmanager.handler;

import com.shinesolutions.aemstackmanager.model.TaskMessage;
import com.shinesolutions.aemstackmanager.util.MessageExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Invokes the correct action based on the message type
 */
@Component
public class SqsMessageHandler implements MessageHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private Map<String, TaskHandler> taskHandlerMappings;

    private static final Map<String, String> RUNNING_TASKS = new HashMap<>();
    private static final ReentrantLock LOCK = new ReentrantLock();

    public boolean handleMessage(Message message) {

        boolean deleteMessage = false;
        TaskMessage taskMessage = null;

        try {
            String messageBody = ((TextMessage) message).getText();
            logger.debug("Raw message body: " + messageBody);
            taskMessage = MessageExtractor.extractTaskMessage(messageBody);
        } catch (Exception e) {
            logger.error("Error when reading message body, task will not be handled", e);
        }

        if (taskMessage != null) {

            String task = taskMessage.getTask();
            String stackPrefix = taskMessage.getStackPrefix();

            // Get class mapping for message type:
            TaskHandler taskHandler = taskHandlerMappings.get(task);

            if (taskHandler == null) {

                logger.error("No task handler found for message: " + task);
                deleteMessage = true;

            } else {

                lockStack(stackPrefix, task);

                try {

                    logger.debug("Handling task: " + task);
                    deleteMessage = taskHandler.handleTask(taskMessage);

                } catch (Exception e) {

                    logger.error("Failed to handle task for message: " + task, e);

                } finally {
                    unlockStack(stackPrefix);
                }

            }


        }

        return deleteMessage;
    }


    private void lockStack(String stackPrefix, String task) {

        boolean handleTask = false;

        int sleepTime = 5;

        while (!handleTask) {

            LOCK.lock();

            try {

                String runningTask = RUNNING_TASKS.get(stackPrefix);

                if (runningTask != null) {

                    logger.info("Stack: " + stackPrefix + " is already executing: " + runningTask + ". Sleeping for " + sleepTime + " seconds.");

                } else {

                    RUNNING_TASKS.put(stackPrefix, task);
                    handleTask = true;
                }

            } finally {
                LOCK.unlock();
            }


            if (!handleTask) {

                try {
                    Thread.sleep(sleepTime * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                sleepTime = sleepTime * 2;

            }

        }
    }


    private void unlockStack(String stackPrefix) {
        LOCK.lock();
        try {
            //task is completed. remove from map.
            RUNNING_TASKS.remove(stackPrefix);
        } finally {
            LOCK.unlock();
        }
    }

}
