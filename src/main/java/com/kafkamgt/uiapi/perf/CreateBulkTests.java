package com.kafkamgt.uiapi.perf;

import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.model.TopicInfo;
import com.kafkamgt.uiapi.model.TopicRequestModel;
import com.kafkamgt.uiapi.service.TopicControllerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class CreateBulkTests {

    @Autowired
    private TopicControllerService topicControllerService;



    class MyThread1 extends Thread {
        int k;
        public MyThread1(int i) {
            k = i;
        }

        @Override
        public void run() {
            //Your Code
            createTopicReqThread();
//            approveTopicReqThread(k);
//                getTopics();

        }

        private void getTopics(){
            try {
                List<List<TopicInfo>> s = topicControllerService.getTopics("1","1","",null,null,null);
                //System.out.println(k+"--"+s.size());
            } catch (Exception e) {
                log.error("Exception:", e);
            }
        }

        private void approveTopicReqThread(int k) {
            try {
                topicControllerService.approveTopicRequests(""+k);
            } catch (KafkawizeException e) {
                log.error("Exception:", e);
            }
        }

        private void createTopicReqThread() {
            TopicRequestModel topicRequest = new TopicRequestModel();

            Calendar calendar = Calendar.getInstance();
            calendar.getTimeInMillis();
            topicRequest.setTopicname("testtopic-"+calendar.getTimeInMillis()+""+k);
            topicRequest.setEnvironment("1");
            topicRequest.setTopicpartitions(1);
            topicRequest.setReplicationfactor("1");
            topicRequest.setRequestor("murali");
            topicRequest.setUsername("murali");
            topicRequest.setTeamname("Octopus");

            try {
                topicControllerService.createTopicsRequest(topicRequest);
            } catch (KafkawizeException e) {
                log.error("Exception:", e);
            }
        }
    }

    public void getTopics(){
        for(int i=0;i<100;i++) {
            MyThread1 temp = new MyThread1(i);
            temp.start();
            try {
                temp.join(10);
            }
            catch (InterruptedException e) {
                log.error("Exception:", e);
            }
        }
    }

    public void createTopicRequests(){
        for(int i=0;i<100;i++) {
            MyThread1 temp = new MyThread1(i);
            temp.start();
            try {
                temp.join(10);
            }
            catch (InterruptedException e) {
                log.error("Exception:", e);
            }
        }
    }

    public void approveTopicRequests(){
        for(int i=1021; i < 1121; i++) {
            MyThread1 temp = new MyThread1(i);
            temp.start();
            try {
                temp.join(10);
            }
            catch (InterruptedException e) {
                log.error("Exception:", e);
            }
        }
    }
}
