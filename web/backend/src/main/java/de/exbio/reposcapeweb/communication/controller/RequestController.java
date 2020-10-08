package de.exbio.reposcapeweb.communication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.requests.FilterGroup;
import de.exbio.reposcapeweb.communication.requests.GraphRequest;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for the incoming requests on the RepoScape-WEB application.
 *
 * @author Andreas Maier
 */
@Controller
@RequestMapping(value = "/api")
public class RequestController {

    private final Logger log = LoggerFactory.getLogger(RequestController.class);

    private final DrugService drugService;
    private final ObjectMapper objectMapper;
    private final WebGraphService webGraphService;

    @Autowired
    public RequestController(DrugService drugService, ObjectMapper objectMapper, WebGraphService webGraphService) {
        this.drugService = drugService;
        this.objectMapper = objectMapper;
        this.webGraphService = webGraphService;
    }


//    @RequestMapping(value = "/running", method = RequestMethod.GET)
//    @ResponseBody
//    public String checkRunning(){
//        log.info("Requested Springboot running check!");
//        return "Router is Running!";
//    }

    @RequestMapping(value = "/getExampleGraph1", method = RequestMethod.GET)
    @ResponseBody
    public String getExampleGraph1() {
        log.info("got request on comorbidity graph");
        try {
            String out = objectMapper.writeValueAsString(webGraphService.getCancerComorbidity());
            log.info("Answered request!");
            return out;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getMetagraph", method = RequestMethod.GET)
    @ResponseBody
    public String getMetaGraph() {
        log.info("got request on metagraph");
        try {
            String out = objectMapper.writeValueAsString(webGraphService.getMetaGraph());
            return out;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getExampleGraph2", method = RequestMethod.GET)
    @ResponseBody
    public String getExampleGraph2() {
        log.info("got request on neuro-drug-graph");
        try {
            String out = objectMapper.writeValueAsString(webGraphService.getNeuroDrugs());
            log.info("Answered request!");
            return out;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getGraphList",method = RequestMethod.GET)
    @ResponseBody
    public String getGraphList(@RequestParam("id") String id, @RequestParam("cached") boolean cached){
        System.out.println("got request for "+id+" from cache="+cached);
        try{
            String out = objectMapper.writeValueAsString(webGraphService.getList(id));
            return out;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getFiltered", method = RequestMethod.POST)
    @ResponseBody
    public String getFiltered(@RequestBody FilterGroup filters) {
        return "";
    }

    @RequestMapping(value = "/getGraph", method = RequestMethod.POST)
    @ResponseBody
    public String getGraph(@RequestBody GraphRequest request) {
        try {
            log.info("Requested a graph "+objectMapper.writeValueAsString(request));
            String out = objectMapper.writeValueAsString(webGraphService.getGraph(request));
            log.info("Graph sent");
            return out;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
