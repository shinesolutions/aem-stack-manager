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

        assertThat(taskMessage.getProgress(), equalTo(root.path("Progress").asInt()));
        assertThat(taskMessage.getAccountId(), equalTo(root.path("AccountId").asText()));
        assertThat(taskMessage.getDescription(), equalTo(root.path("Description").asText()));
        assertThat(taskMessage.getRequestId(), equalTo(root.path("RequestId").asText()));
        assertThat(taskMessage.getEndTime(), equalTo(root.path("EndTime").asText()));
        assertThat(taskMessage.getAutoScalingGroupARN(), equalTo(root.path("AutoScalingGroupARN").asText()));

        assertThat(taskMessage.getActivityId(), equalTo(root.path("ActivityId").asText()));
        assertThat(taskMessage.getStartTime(), equalTo(root.path("StartTime").asText()));
        assertThat(taskMessage.getService(), equalTo(root.path("Service").asText()));

        assertThat(taskMessage.getTime(), equalTo(root.path("Time").asText()));
        assertThat(taskMessage.getEC2InstanceId(), equalTo(root.path("EC2InstanceId").asText()));
        assertThat(taskMessage.getStatusCode(), equalTo(root.path("StatusCode").asText()));

        JsonNode details = root.path("Details");
        assertThat(taskMessage.getDetails().getSubnetID(), equalTo(details.path("Subnet ID").asText()));
        assertThat(taskMessage.getDetails().getAvailabilityZone(), equalTo(details.path("Availability Zone").asText()));

        assertThat(taskMessage.getStatusMessage(), equalTo(root.path("StatusMessage").asText()));
        assertThat(taskMessage.getAutoScalingGroupName(), equalTo(root.path("AutoScalingGroupName").asText()));
        assertThat(taskMessage.getCause(), equalTo(root.path("Cause").asText()));
        assertThat(taskMessage.getTask(), equalTo(root.path("Task").asText()));
    }

    @Test(expected=JsonParseException.class)
    public void testExtractTaskMessageParseFail() throws Exception {
        MessageExtractor.extractTaskMessage("Invalid string");
    }

}
