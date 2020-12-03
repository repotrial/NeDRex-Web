package de.exbio.reposcapeweb.communication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.jobs.JobController;
import de.exbio.reposcapeweb.communication.jobs.JobRequest;
import de.exbio.reposcapeweb.communication.reponses.SelectionResponse;
import de.exbio.reposcapeweb.communication.reponses.WebGraph;
import de.exbio.reposcapeweb.communication.reponses.WebGraphList;
import de.exbio.reposcapeweb.communication.requests.*;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.history.GraphHistory;
import de.exbio.reposcapeweb.db.history.HistoryController;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;

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
    private final EdgeController edgeController;
    private final NodeController nodeController;
    private final HistoryController historyController;
    private final JobController jobController;
    private final SimpMessagingTemplate socketTemplate;

    @Autowired
    public RequestController(SimpMessagingTemplate simpMessagingTemplate, DrugService drugService, ObjectMapper objectMapper, WebGraphService webGraphService, EdgeController edgeController, NodeController nodeController, HistoryController historyController, JobController jobController) {
        this.drugService = drugService;
        this.objectMapper = objectMapper;
        this.webGraphService = webGraphService;
        this.edgeController = edgeController;
        this.nodeController = nodeController;
        this.historyController = historyController;
        this.jobController = jobController;
        this.socketTemplate = simpMessagingTemplate;
    }


//    @RequestMapping(value = "/running", method = RequestMethod.GET)
//    @ResponseBody
//    public String checkRunning(){
//        log.info("Requested Springboot running check!");
//        return "Router is Running!";
//    }

//    @RequestMapping(value = "/getExampleGraph1", method = RequestMethod.GET)
//    @ResponseBody
//    public String getExampleGraph1() {
//        log.info("got request on comorbidity graph");
//        try {
//            String out = objectMapper.writeValueAsString(webGraphService.getCancerComorbidity());
//            log.info("Answered request!");
//            return out;
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    @RequestMapping(value = "/getMetagraph", method = RequestMethod.GET)
    @ResponseBody
    public String getMetaGraph() {
        log.info("got request on metagraph");
        try {
            return objectMapper.writeValueAsString(webGraphService.getMetaGraph());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getNodeDetails", method = RequestMethod.GET)
    @ResponseBody
    public String getDetails(@RequestParam("name") String name, @RequestParam("id") int id) {
        log.info("requested details for node " + name + " with id " + id);
        String out = "";
        try {
            HashMap<String, Object> details = nodeController.nodeToAttributeList(Graphs.getNode(name), id);
            details.put("order", nodeController.getAttributes(Graphs.getNode(name)));
            out = objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return out;
    }

    @RequestMapping(value = "/getEdgeDetails", method = RequestMethod.GET)
    @ResponseBody
    public String getDetails(@RequestParam("gid") String gid, @RequestParam("name") String name, @RequestParam("id1") int id1, @RequestParam("id2") int id2) {
        log.info("requested details for edge " + name + " with id (" + id1 + " -> " + id2 + ")");
        return webGraphService.getEdgeDetails(gid, name, new PairId(id1, id2));
    }

//    @RequestMapping(value = "/getExampleGraph2", method = RequestMethod.GET)
//    @ResponseBody
//    public String getExampleGraph2() {
//        log.info("got request on neuro-drug-graph");
//        try {
//            String out = objectMapper.writeValueAsString(webGraphService.getNeuroDrugs());
//            log.info("Answered request!");
//            return out;
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    @RequestMapping(value = "/getSuggestions", method = RequestMethod.POST)
    @ResponseBody
    public String getSuggestions(@RequestBody SuggestionRequest request) {
        log.info("got request for " + request.gid + " getting suggestions for query '" + request.query + "' in " + request.name + " (" + request.type + ")");
        try {
            return objectMapper.writeValueAsString(webGraphService.getSuggestions(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getConnectedSelection", method = RequestMethod.POST)
    @ResponseBody
    public String getConnectedEdges(@RequestBody SelectionRequest request) {
        try {
            return objectMapper.writeValueAsString(webGraphService.getSelection(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/extendGraph", method = RequestMethod.POST)
    @ResponseBody
    public String getGraphExtension(@RequestBody ExtensionRequest request) {
        try {
            return objectMapper.writeValueAsString(webGraphService.getExtension(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/collapseGraph", method = RequestMethod.POST)
    @ResponseBody
    public String getCollapsedGraph(@RequestBody CollapseRequest request) {
        try {
            return objectMapper.writeValueAsString(webGraphService.getCollapsedGraph(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getConnectionGraph", method = RequestMethod.GET)
    @ResponseBody
    public String getConnectionGraph(@RequestParam("gid") String gid) {
        try {
            return objectMapper.writeValueAsString(webGraphService.getConnectionGraph(gid));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


    @RequestMapping(value = "/getGraphList", method = RequestMethod.GET)
    @ResponseBody
    public String getGraphList(@RequestParam("id") String id) {
        log.info("got request for " + id);
        try {
            StringBuilder out = new StringBuilder("{\"edges\":{");
            WebGraphList list = webGraphService.getList(id, null);
            StringBuilder edgeBuilder = new StringBuilder();
            list.getEdges().forEach((type, edges) -> {
                edgeBuilder.append("\"").append(type).append("\":[");
                StringBuilder sb = new StringBuilder("");
                edges.forEach(e -> sb.append(e).append(','));
                edgeBuilder.append(sb.substring(0, edges.size() > 0 ? sb.length() - 1 : sb.length()));
                edgeBuilder.append("],");
            });
            out.append(edgeBuilder.substring(0, list.getEdges().size() > 0 ? edgeBuilder.length() - 1 : edgeBuilder.length())).append("},");
            out.append(objectMapper.writeValueAsString(list).substring(1));
            return out.toString();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getCustomGraphList", method = RequestMethod.POST)
    @ResponseBody
    public String getCustomGraphList(@RequestBody CustomListRequest req) {
        try {
            StringBuilder out = new StringBuilder("{\"edges\":{");
            WebGraphList list = webGraphService.getList(req.id, req);
            StringBuilder edgeBuilder = new StringBuilder();
            list.getEdges().forEach((type, edges) -> {
                edgeBuilder.append("\"").append(type).append("\":[");
                StringBuilder sb = new StringBuilder("");
                edges.forEach(e -> sb.append(e).append(','));
                edgeBuilder.append(sb.substring(0, edges.size() > 0 ? sb.length() - 1 : sb.length()));
                edgeBuilder.append("],");
            });
            out.append(edgeBuilder.substring(0, list.getEdges().size() > 0 ? edgeBuilder.length() - 1 : edgeBuilder.length())).append("},");
            out.append(objectMapper.writeValueAsString(list).substring(1));
            return out.toString();
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

    @RequestMapping(value = "/getGraph", method = RequestMethod.GET)
    @ResponseBody
    public String getGraph(@RequestParam("id") String id) {
        try {
            log.info("Requested a web-graph " + objectMapper.writeValueAsString(id));
            String out = objectMapper.writeValueAsString(webGraphService.getWebGraph(id));
            log.info("Graph sent");
            return out;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/archiveHistory", method = RequestMethod.GET)
    @ResponseBody
    public void archiveHistory(@RequestParam("uid") String uid, @RequestParam("gid") String gid) {
        log.info(uid + " is saving graph " + gid);
        webGraphService.addGraphToHistory(uid, gid);
    }

    @RequestMapping(value = "/getGraph", method = RequestMethod.POST)
    @ResponseBody
    public String getGraph(@RequestBody GraphRequest request) {
        try {
            log.info("Requested a graph " + objectMapper.writeValueAsString(request) + " uid=" + request.uid);
            String out = objectMapper.writeValueAsString(webGraphService.getWebGraph(request));
            log.info("Graph sent");
            return out;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    @ResponseBody
    public String getUser(@RequestParam("user") String userId) {
        if(userId==null)
            return "";
        userId = historyController.validateUser(userId);
        HashMap<String, Job.JobState> jobs = jobController.getJobGraphStates(userId);
        HashMap<String, Object> out = historyController.getUserHistory(userId, jobs);

        try {
            return objectMapper.writeValueAsString(out);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value="/initUser", method=RequestMethod.GET)
    @ResponseBody
    public String initUser(){
        return historyController.validateUser(null);
    }

    @RequestMapping(value = "/updateGraph", method = RequestMethod.POST)
    @ResponseBody
    public String updateGraph(@RequestBody UpdateRequest request) {
        try {
            return objectMapper.writeValueAsString(webGraphService.updateGraph(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getGraphInfo", method = RequestMethod.POST)
    @ResponseBody
    public String getGraphInfo(@RequestBody GraphRequest request) {
        try {
            log.info("Requested a graph " + objectMapper.writeValueAsString(request));
            return objectMapper.writeValueAsString(webGraphService.getGraph(request).toInfo());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/finishedJob", method = RequestMethod.GET)
    @ResponseBody
    public void finishedJob(@RequestParam("id") String id) {
        log.info("Job " + id + " was just finished.");
        jobController.finishJob(id);
    }

    @RequestMapping(value = "/submitJob", method = RequestMethod.POST)
    @ResponseBody
    public String submitJob(@RequestBody JobRequest request) {
        HashMap<String, Object> out = new HashMap<>();
        Job j = jobController.registerJob(request);
        return toJson(j.toMap());
    }

    @RequestMapping(value="/getJobs", method = RequestMethod.GET)
    @ResponseBody
    public String getJobs(@RequestParam("uid") String uid, @RequestParam("gid") String gid){
        return toJson(jobController.getJobGraphStates(uid, gid));
    }

    @RequestMapping(value="/getUserJobs", method = RequestMethod.GET)
    @ResponseBody
    public String getJobs(@RequestParam("uid") String uid){
        return toJson(jobController.getJobGraphStates(uid, null));
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

//    @RequestMapping(value="/testSocket", method=RequestMethod.GET)
//    @ResponseBody
//    @MessageMapping("/jobs")
//    public void testSocket(@RequestParam("route") String route, @RequestParam("message") String message){
//        System.out.println("Testing socket on '"+route+"' with message "+ message);
//        socketTemplate.convertAndSend(route,message);
//    }

}
