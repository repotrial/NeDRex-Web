package de.exbio.reposcapeweb.communication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.jobs.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

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


    @MessageMapping("/jobs")
    public void setThumbnailReady(String gid) {
        HashMap<String,String> params = new HashMap<>();
        params.put("gid",gid);
        socketTemplate.convertAndSend("/graph/status-thumbnail_"+gid, toJson(params));
    }
}
