package com.shinesolutions.aemstackmanager.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemstackmanager.model.TaskMessage;
import org.junit.Test;

import java.io.File;
import java.util.Scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MessageExtractorTest {


    @Test
    @SuppressWarnings("resource")
    public void testExtractTaskMessageSuccess() throws Exception {

        File sampleFileMessageOnly = new File(getClass().getResource("/sample-sqs-message-body-1.json").getFile());
        File sampleFileFull = new File(getClass().getResource("/sample-sqs-message-body-2.json").getFile());
        String sampleFileContent = new Scanner(sampleFileFull).useDelimiter("\\Z").next();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sampleFileMessageOnly);

        TaskMessage taskMessage = MessageExtractor.extractTaskMessage(sampleFileContent);

        assertThat(taskMessage.getTask(), equalTo(root.path("task").asText()));
        assertThat(taskMessage.getStackPrefix(), equalTo(root.path("stack_prefix").asText()));

        JsonNode details = root.path("details");
        assertThat(taskMessage.getDetails().getDescriptorFile(), equalTo(details.path("descriptor_file").asText()));
        assertThat(taskMessage.getDetails().getComponent(), equalTo(details.path("component").asText()));
        assertThat(taskMessage.getDetails().getArtifact(), equalTo(details.path("artifact").asText()));

    }

    @Test(expected=JsonParseException.class)
    public void testExtractTaskMessageParseFail() throws Exception {
        MessageExtractor.extractTaskMessage("Invalid string");
    }

}
