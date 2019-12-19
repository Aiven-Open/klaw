package com.kafkamgt.uiapi.controller;


import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.model.AclInfo;
import com.kafkamgt.uiapi.service.AclControllerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/")
public class AclController {

    private static Logger LOG = LoggerFactory.getLogger(AclController.class);

    @Autowired
    AclControllerService aclControllerService;

    public AclController(AclControllerService aclControllerService){
        this.aclControllerService=aclControllerService;
    }

    @PostMapping(value = "/createAcl")
    public ResponseEntity<String> createAcl(@RequestBody AclRequests addAclRequest) {
        return new ResponseEntity<>(aclControllerService.createAcl(addAclRequest), HttpStatus.OK);
    }

    @PostMapping(value = "/updateSyncAcls")
    public ResponseEntity<String> updateSyncAcls(@RequestParam ("updatedSyncAcls") String updateSyncAcls,
                                                   @RequestParam ("envSelected") String envSelected) {
        return new ResponseEntity<>(aclControllerService.updateSyncAcls(updateSyncAcls, envSelected), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAclRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<AclRequests>> getAclRequests() {
        return new ResponseEntity<>(aclControllerService.getAclRequests(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getCreatedAclRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<List<AclRequests>>> getCreatedAclRequests() {
        return new ResponseEntity<>(aclControllerService.getCreatedAclRequests(), HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteAclRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteAclRequests(@RequestParam("req_no") String req_no) {
        return new ResponseEntity<>(aclControllerService.deleteAclRequests(req_no), HttpStatus.OK);
    }

    @PostMapping(value = "/execAclRequest")
    public ResponseEntity<String> approveAclRequests(@RequestParam("req_no") String req_no) throws KafkawizeException {
        return new ResponseEntity<String>(aclControllerService.approveAclRequests(req_no), HttpStatus.OK);
    }

    @PostMapping(value = "/execAclRequestDecline")
    public ResponseEntity<String> declineAclRequests(@RequestParam("req_no") String req_no) throws KafkawizeException {
        return new ResponseEntity<String>(aclControllerService.declineAclRequests(req_no), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAcls", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<AclInfo>> getAcls(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo,
                                                 @RequestParam(value="topicnamesearch",required=false) String topicNameSearch) throws KafkawizeException {
        return new ResponseEntity<>(aclControllerService.getAcls(env, pageNo, topicNameSearch, false), HttpStatus.OK);
    }

    @RequestMapping(value = "/getSyncAcls", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<AclInfo>> getSyncAcls(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo) throws KafkawizeException {
        return new ResponseEntity<>(aclControllerService.getAcls(env, pageNo, null, true), HttpStatus.OK);
    }
}
