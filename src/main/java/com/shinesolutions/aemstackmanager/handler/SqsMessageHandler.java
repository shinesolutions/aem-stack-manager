package com.shinesolutions.aemstackmanager.handler;

import com.shinesolutions.aemstackmanager.model.EventMessage;
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
    private Map<String, EventHandler> eventTypeHandlerMappings;

    public boolean handleMessage(Message message) {

        boolean handleSuccess = false;
        EventMessage eventMessage = null;

        try {
            String messageBody = ((TextMessage)message).getText();
            logger.debug("Raw message body: " + messageBody);
            eventMessage = MessageExtractor.extractEventMessage(messageBody);
        } catch (Exception e) {
            logger.error("Error when reading message body, event will not be handled", e);
        }

        if (eventMessage != null) {
            String eventType = eventMessage.getEvent();

            // Get class mapping for message type:
            EventHandler eventHandler = eventTypeHandlerMappings.get(eventType);

            if (eventHandler == null) {
                logger.error("No event handler found for message type: " + eventType);
            } else {
                try {
                    logger.debug("Handling event: " + eventType);
                    handleSuccess = eventHandler.handleEvent(eventMessage);

                } catch (Exception e) {
                    logger.error("Failed to handle event for message type: " + eventType, e);
                }
            }
        }

        return handleSuccess;
    }

}
