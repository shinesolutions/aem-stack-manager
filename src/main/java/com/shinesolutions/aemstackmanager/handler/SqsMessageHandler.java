package com.shinesolutions.aemstackmanager.handler;

import com.shinesolutions.aemstackmanager.model.TaskMessage;
import com.shinesolutions.aemstackmanager.util.MessageExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Map;

/*
 * Invokes the correct action based on the message type
 */
@Component
public class SqsMessageHandler implements MessageHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private Map<String, TaskHandler> taskHandlerMappings;

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

            // Get class mapping for message type:
            TaskHandler taskHandler = taskHandlerMappings.get(task);

            if (taskHandler == null) {
                logger.error("No task handler found for message: " + task);
                deleteMessage = true;
            } else {
                try {
                    logger.debug("Handling task: " + task);
                    deleteMessage = taskHandler.handleTask(taskMessage);

                } catch (Exception e) {
                    logger.error("Failed to handle task for message: " + task, e);
                }
            }
        }

        return deleteMessage;
    }

}
