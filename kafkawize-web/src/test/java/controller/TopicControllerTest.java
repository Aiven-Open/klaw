package controller;

import com.kafkamgt.uiapi.controller.TopicController;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.service.TopicControllerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TopicControllerTest {

    @Mock
    private TopicControllerService topicControllerService;

    private TopicController topicController;

    @Before
    public void setUp(){
        topicController = new TopicController(topicControllerService);
    }

    @Test
    public void createTopicTest(){
        TopicRequest topicRequest = new TopicRequest();
        ResponseEntity<String> response = topicController.createTopics(topicRequest);
        assertEquals(HttpStatus.OK.value(),response.getStatusCodeValue());
    }
}
