package de.exbio.reposcapeweb.communication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.jobs.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.incrementer.HanaSequenceMaxValueIncrementer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;

@Service
@RequestMapping(value = "/socket")
public class SocketController {

    final SimpMessagingTemplate socketTemplate;
    final ObjectMapper objectMapper;

    @Autowired
    public SocketController(SimpMessagingTemplate simpMessagingTemplate, ObjectMapper objectMapper) {
        this.socketTemplate = simpMessagingTemplate;
        this.objectMapper = objectMapper;
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


    @MessageMapping("/jobs")
    public void setJobUpdate(Job j) {
        socketTemplate.convertAndSend("/graph/status-job" + j.getJobId(), toJson(j.toMap()));
    }


}
