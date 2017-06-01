package com.shinesolutions.aemstackmanager.service;

import com.amazon.sqs.javamessaging.SQSConnection;
import com.shinesolutions.aemstackmanager.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Listener for the SQS queue. Will add itself as a message listener upon startup
 * and then start the connection.
 * <p>
 * When a message is received, it will pass it to the @see MessageHandler
 */
@Component
public class MessageReceiver implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private SQSConnection connection;

    @Resource
    private MessageConsumer consumer;

    @Resource
    private MessageHandler messageHandler;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void onMessage(Message message) {

        try {

            if (message != null) {

                logger.info("Message received with id: " + message.getJMSMessageID());

                submitMessage(message);

            } else {
                logger.info("Null message received");
            }

        } catch (JMSException e) {
            logger.error("Error while receiving message", e);
        }
    }

    private void submitMessage(Message message) {

        executorService.submit(() -> {

            boolean removeMessageFromQueue = messageHandler.handleMessage(message);

            //Acknowledging the message with remove it from the queue
            if (removeMessageFromQueue) {

                try {
                    logger.info("Acknowledged message (removing from queue): " + message.getJMSMessageID());
                    message.acknowledge();
                } catch (JMSException e) {
                    logger.error("Error while receiving or acknowledging message", e);
                }

            }

        });

    }

    @PostConstruct
    public void initIt() throws Exception {
        logger.debug("Initialising message receiver, starting SQS connection");
        consumer.setMessageListener(this);
        connection.start();
    }

    @PreDestroy
    public void cleanUp() throws Exception {

        logger.debug("Destroying message receiver, stopping SQS connection");
        connection.stop();

        try {
            logger.debug("attempt to shutdown executor");
            executorService.shutdown();
            executorService.awaitTermination(60, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("tasks interrupted");
        } finally {
            if (!executorService.isTerminated()) {
                logger.error("cancel non-finished tasks");
            }
            executorService.shutdownNow();
            logger.debug("shutdown finished");
        }

    }

}
