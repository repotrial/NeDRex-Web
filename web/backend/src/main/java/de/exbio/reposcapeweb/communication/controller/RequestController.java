package de.exbio.reposcapeweb.communication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.reponses.SelectionResponse;
import de.exbio.reposcapeweb.communication.reponses.WebGraph;
import de.exbio.reposcapeweb.communication.reponses.WebGraphList;
import de.exbio.reposcapeweb.communication.requests.*;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

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

    @Autowired
    public RequestController(DrugService drugService, ObjectMapper objectMapper, WebGraphService webGraphService, EdgeController edgeController, NodeController nodeController) {
        this.drugService = drugService;
        this.objectMapper = objectMapper;
        this.webGraphService = webGraphService;
        this.edgeController = edgeController;
        this.nodeController = nodeController;
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
            out = objectMapper.writeValueAsString(nodeController.nodeToAttributeList(Graphs.getNode(name), id));
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
    public String getConnectionGraph(@RequestParam("gid") String gid){
        try{
            return objectMapper.writeValueAsString(webGraphService.getConnectionGraph(gid));
        }catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


    @RequestMapping(value = "/getGraphList", method = RequestMethod.GET)
    @ResponseBody
    public String getGraphList(@RequestParam("id") String id, @RequestParam("cached") boolean cached) {
        log.info("got request for " + id + " from cache=" + cached);
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

    @RequestMapping(value = "/getGraph", method = RequestMethod.POST)
    @ResponseBody
    public String getGraph(@RequestBody GraphRequest request) {
        try {
            log.info("Requested a graph " + objectMapper.writeValueAsString(request));
            String out = objectMapper.writeValueAsString(webGraphService.getWebGraph(request));
            log.info("Graph sent");
            return out;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/updateGraph", method = RequestMethod.POST)
    @ResponseBody
    public String updateGraph(@RequestBody UpdateRequest request) {
        ;
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

}
