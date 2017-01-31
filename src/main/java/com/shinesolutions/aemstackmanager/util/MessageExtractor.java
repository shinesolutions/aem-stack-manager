package com.shinesolutions.aemstackmanager.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemstackmanager.model.EventMessage;

import java.io.IOException;

public class MessageExtractor {

    public static EventMessage extractEventMessage(String sqsMessageBody)
            throws JsonParseException, JsonMappingException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sqsMessageBody);

        String messageNode = root.path("Message").asText();

        // Body contains \" instead of just ". Need to replace before attempting
        // to map to object
        String preparedBody = messageNode.replace("\\\"", "\"");

        ObjectMapper eventMapper = new ObjectMapper();

        return eventMapper.readValue(preparedBody, EventMessage.class);

    }
}
