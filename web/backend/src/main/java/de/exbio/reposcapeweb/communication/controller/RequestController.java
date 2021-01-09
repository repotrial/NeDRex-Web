package de.exbio.reposcapeweb.communication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.jobs.JobController;
import de.exbio.reposcapeweb.communication.jobs.JobRequest;
import de.exbio.reposcapeweb.communication.reponses.*;
import de.exbio.reposcapeweb.communication.requests.*;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.history.HistoryController;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import de.exbio.reposcapeweb.db.updates.UpdateService;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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
    private final DbCommunicationService dbCommunicationService;
    private final UpdateService updateService;

    @Autowired
    public RequestController(UpdateService updateService, DbCommunicationService dbCommunicationService, SimpMessagingTemplate simpMessagingTemplate, DrugService drugService, ObjectMapper objectMapper, WebGraphService webGraphService, EdgeController edgeController, NodeController nodeController, HistoryController historyController, JobController jobController) {
        this.drugService = drugService;
        this.objectMapper = objectMapper;
        this.webGraphService = webGraphService;
        this.edgeController = edgeController;
        this.nodeController = nodeController;
        this.historyController = historyController;
        this.jobController = jobController;
        this.socketTemplate = simpMessagingTemplate;
        this.dbCommunicationService = dbCommunicationService;
        this.updateService = updateService;
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
        HashMap<String, Object> details = new HashMap<>();
        nodeController.nodeToAttributeList(Graphs.getNode(name), id).forEach((k, v) -> details.put(nodeController.getAttributeLabelMap(name).get(k), v));
        try {
            details.put("order", nodeController.getAttributeLabels(Graphs.getNode(name)));
            out = objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return out;
    }

    @RequestMapping(value = "/getEdgeDetails", method = RequestMethod.GET)
    @ResponseBody
    public String getDetails(@RequestParam("gid") String gid, @RequestParam("name") String name, @RequestParam("id1") int id1, @RequestParam("id2") int id2) {
        log.info("requested details for edge " + name + " with id (" + id1 + " -> " + id2 + ") [" + gid + "]");
        return toJson(webGraphService.getEdgeDetails(gid, name, new PairId(id1, id2)));
    }


    @RequestMapping(value = "/getSuggestions", method = RequestMethod.POST)
    @ResponseBody
    public String getSuggestions(@RequestBody SuggestionRequest request) {
        try {
            return objectMapper.writeValueAsString(webGraphService.getSuggestions(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getConnectedNodes", method = RequestMethod.POST)
    @ResponseBody
    public String getConnectedNodes(@RequestBody HashMap<String, Object> request) {
        if ((boolean) request.get("noloop")) {
            String type = request.get("sourceType").toString();
            HashSet<Integer> addedIds = new HashSet<>();
            LinkedList<Object> nodes = new LinkedList<>();
            ((List<Integer>) request.get("sourceIds")).forEach(n -> {
                if (addedIds.add(n))
                    nodes.add(nodeController.getNode(type, n).getAsMap(new HashSet<>(Arrays.asList("id", "displayName", "primaryDomainId"))));
            });
            return toJson(nodes);
        }
        return toJson(webGraphService.getConnectedNodes(request.get("sourceType").toString(), request.get("targetType").toString(), (List<Integer>) request.get("sourceIds")));
    }

    @RequestMapping(value = "/mapToDomainIds", method = RequestMethod.POST)
    @ResponseBody
    public String mapToDomainIds(@RequestBody HashMap<String,Object> request){
        int type = Graphs.getNode(request.get("type").toString());
       return toJson(((List<Integer>) request.get("ids")).stream().map(id->nodeController.getDomainId(type,id)).collect(Collectors.toList()));
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
        return toJson(webGraphService.getConnectionGraph(gid));
    }


    @RequestMapping(value = "/getGraphList", method = RequestMethod.GET)
    @ResponseBody
    public String getGraphList(@RequestParam("id") String id) {
        log.info("got request for " + id);
        WebGraphList list = webGraphService.getList(id, null);
        return toJson(list);
    }

    @RequestMapping(value = "/getCustomGraphList", method = RequestMethod.POST)
    @ResponseBody
    public String getCustomGraphList(@RequestBody CustomListRequest req) {
        return toJson(webGraphService.getList(req.id, req));
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

    @RequestMapping(value = "/downloadJobResult", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    ResponseEntity<Resource> downloadJobResult(@RequestParam("jid") String jid, HttpServletRequest request) {
        File f = jobController.getDownload(jid);
        Resource resource = new FileSystemResource(jobController.getDownload(jid));
        String contentType = request.getServletContext().getMimeType(jobController.getDownload(jid).getAbsolutePath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + f.getName())
                .body(resource);
    }

    @RequestMapping(value = "/downloadGraph", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    ResponseEntity<Resource> downloadGraph(@RequestParam("gid") String gid, HttpServletRequest request) {
        File f = webGraphService.getDownload(gid);
        Resource resource = new FileSystemResource(f);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_XML)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + f.getName())
                .body(resource);
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
        if (userId == null)
            return "";
        userId = historyController.validateUser(userId);
        HashMap<String, Pair<Job.JobState, ToolService.Tool>> jobs = jobController.getJobGraphStatesAndTypes(userId);
        HashMap<String, Object> out = historyController.getUserHistory(userId, jobs);

        try {
            return objectMapper.writeValueAsString(out);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/mapFileListToItems", method = RequestMethod.POST)
    @ResponseBody
    public String getFileListToItems(@RequestBody HashMap<String, String> request) {
        return toJson(webGraphService.mapIdsToItemList(request.get("type"), StringUtils.convertBase64(request.get("file"))));


    }


    @RequestMapping(value = "/removeGraph", method = RequestMethod.GET)
    @ResponseBody
    public void removeGraph(@RequestParam("gid") String gid) {
        webGraphService.remove(gid);

    }

    @RequestMapping(value = "/initUser", method = RequestMethod.GET)
    @ResponseBody
    public String initUser() {
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
        log.info("Requested a graph " + toJson(request));
        WebGraphInfo info = webGraphService.getGraph(request).toInfo();
        return toJson(info);
    }

    @RequestMapping(value = "/getGraphHistory", method = RequestMethod.GET)
    @ResponseBody
    public String getGraphHistory(@RequestParam("gid") String gid, @RequestParam("uid") String uid) {
        log.info("GraphHistory detail request: " + gid);
        File thumbnail = webGraphService.getThumbnail(gid);
        webGraphService.createThumbnail(gid, thumbnail);
        return toJson(historyController.getDetailedHistory(uid, webGraphService.getCachedGraph(gid), webGraphService.getConnectionGraph(gid), jobController.getJobGraphStatesAndTypes(uid), thumbnail));
    }

    @RequestMapping(value = "/getThumbnailPath", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> isThumbnailReady(@RequestParam("gid") String gid) throws IOException {
        File thumb = webGraphService.getThumbnail(gid);
        HttpHeaders headers = new HttpHeaders();
        if (!thumb.exists())
            return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);

        InputStream in = new FileSystemResource(thumb.getAbsolutePath()).getInputStream();
        byte[] media = IOUtils.toByteArray(in);
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        return new ResponseEntity<>(media, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/toggleStarred", method = RequestMethod.GET)
    @ResponseBody
    public void toggleStarred(@RequestParam("gid") String gid, @RequestParam("uid") String uid) {
        historyController.toggleStarring(gid, uid);
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
        Job j = jobController.registerJob(request);
        return toJson(j.toMap());
    }

    @RequestMapping(value = "/getJobs", method = RequestMethod.GET)
    @ResponseBody
    public String getJobs(@RequestParam("uid") String uid, @RequestParam("gid") String gid) {
        return toJson(jobController.getJobGraphStatesAndTypes(uid, gid));
    }

    @RequestMapping(value = "/getUserJobs", method = RequestMethod.GET)
    @ResponseBody
    public String getJobs(@RequestParam("uid") String uid) {
        return toJson(jobController.getJobGraphStatesAndTypes(uid, null));
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/isReady", method = RequestMethod.GET)
    @ResponseBody
    public boolean isReady() {
        return !dbCommunicationService.isDbLocked() & !dbCommunicationService.isImportInProgress() & !dbCommunicationService.isUpdateInProgress();
    }

    @RequestMapping(value = "/setGraphName", method = RequestMethod.POST)
    @ResponseBody
    public void setGraphName(@RequestBody HashMap<String, String> data) {
        historyController.saveName(data.get("gid"), data.get("name"));
    }

    @RequestMapping(value = "/saveGraphDescription", method = RequestMethod.POST)
    @ResponseBody
    public void saveGraphDescription(@RequestBody HashMap<String, String> data) {
        historyController.saveDescription(data.get("gid"), data.get("desc"));
    }


    @RequestMapping(value = "/getMetadata", method = RequestMethod.GET)
    @ResponseBody
    public String getMetadata() {
        return toJson(updateService.getMetadata());
    }


//    @RequestMapping(value="/testSocket", method=RequestMethod.GET)
//    @ResponseBody
//    @MessageMapping("/jobs")
//    public void testSocket(@RequestParam("route") String route, @RequestParam("message") String message){
//        System.out.println("Testing socket on '"+route+"' with message "+ message);
//        socketTemplate.convertAndSend(route,message);
//    }

}
