package de.exbio.reposcapeweb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import de.exbio.reposcapeweb.reponses.WebGraph;
import de.exbio.reposcapeweb.reponses.WebGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for the incoming requests on the RepoScape-WEB application.
 *
 * @author Andreas Maier
 */
@Controller
@RequestMapping(value="/api")
public class RequestController {

    private final Logger log = LoggerFactory.getLogger(RequestController.class);

    private final DrugService drugService;
    private final ObjectMapper objectMapper;
    private final WebGraphService webGraphService;

    @Autowired
    public RequestController(DrugService drugService,ObjectMapper objectMapper, WebGraphService webGraphService) {
        this.drugService = drugService;
        this.objectMapper= objectMapper;
        this.webGraphService = webGraphService;
    }


//    @RequestMapping(value = "/running", method = RequestMethod.GET)
//    @ResponseBody
//    public String checkRunning(){
//        log.info("Requested Springboot running check!");
//        return "Router is Running!";
//    }

    @RequestMapping(value = "/getExampleGraph", method = RequestMethod.GET)
    @ResponseBody
    public String getExampleGraph(){
        log.info("got request on exampleGraph");
        try {
            String out = objectMapper.writeValueAsString(webGraphService.getCancerComorbidity());
            log.info("Answered request!");
            return out;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
