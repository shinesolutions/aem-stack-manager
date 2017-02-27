package com.shinesolutions.aemstackmanager.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemstackmanager.model.TaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MessageExtractor {


    private static final Logger logger = LoggerFactory.getLogger(MessageExtractor.class);

    public static TaskMessage extractTaskMessage(String sqsMessageBody)
            throws JsonParseException, JsonMappingException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sqsMessageBody);

        String messageNode = root.path("Message").asText();

        logger.debug("Original Message: " + messageNode);

        // Body contains \" instead of just ". Need to replace before attempting
        // to map to object
        String preparedBody = messageNode.replace("\\\"", "\"").replace("'", "\"");

        logger.debug("Prepared Message: " + preparedBody);

        ObjectMapper taskMapper = new ObjectMapper();

        return taskMapper.readValue(preparedBody, TaskMessage.class);

    }
}
