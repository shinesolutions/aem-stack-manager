package com.shinesolutions.aemstackmanager.handler;

import com.shinesolutions.aemstackmanager.model.TaskMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.TextMessage;
import java.io.File;
import java.util.Map;
import java.util.Scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SqsMessageHandlerTest {

    @Mock
    private Map<String, TaskHandler> taskHandlerMappings;

    @InjectMocks
    private SqsMessageHandler sqsMessageHandler;

    private TextMessage testMessage;

    private TaskHandler mockTaskHandler;

    @Before
    public void setup() throws Exception {
        testMessage = createMessageFromFile("/sample-sqs-message-body-2.json");

        mockTaskHandler = mock(TaskHandler.class);

        when(taskHandlerMappings.get("promote-author")).thenReturn(mockTaskHandler);
    }

    @Test
    public void testSuccess() {
        ArgumentCaptor<TaskMessage> taskMessageCaptor = ArgumentCaptor.forClass(TaskMessage.class);

        when(mockTaskHandler.handleTask(any(TaskMessage.class))).thenReturn(true);

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockTaskHandler, times(1)).handleTask(taskMessageCaptor.capture());

        TaskMessage taskMessage = taskMessageCaptor.getValue();

        assertThat(result, equalTo(true));
        assertThat(taskMessage.getTask(), equalTo("promote-author"));
    }

    @Test
    public void testBadMessageBody() throws Exception {
        when(testMessage.getText()).thenReturn("An invalid string");

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockTaskHandler, never()).handleTask(any(TaskMessage.class));

        assertThat(result, equalTo(false));
    }

    @Test
    public void testNoTaskHandlerFound() {
        when(taskHandlerMappings.get("promote-author")).thenReturn(null);

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockTaskHandler, never()).handleTask(any(TaskMessage.class));

        assertThat(result, equalTo(false));
    }

    @Test
    public void testTaskHandlerError() {
        doThrow(new RuntimeException("Test exception")).when(mockTaskHandler).handleTask(any(TaskMessage.class));

        boolean result = sqsMessageHandler.handleMessage(testMessage);

        verify(mockTaskHandler, times(1)).handleTask(any(TaskMessage.class));

        assertThat(result, equalTo(false));
    }

    @SuppressWarnings("resource")
    private TextMessage createMessageFromFile(String fileName) throws Exception {
        // Read in file containing test example of message body
        File sampleFile = new File(getClass().getResource(fileName).getFile());
        String sampleFileContent = new Scanner(sampleFile).useDelimiter("\\Z").next();

        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(sampleFileContent);
        return textMessage;
    }
}
