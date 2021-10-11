package de.exbio.reposcapeweb.communication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.jobs.JobController;
import de.exbio.reposcapeweb.communication.jobs.JobRequest;
import de.exbio.reposcapeweb.communication.reponses.*;
import de.exbio.reposcapeweb.communication.requests.*;
import de.exbio.reposcapeweb.configs.DBConfig;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.history.HistoryController;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import de.exbio.reposcapeweb.db.updates.UpdateService;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.ProcessUtils;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the incoming requests on the RepoScape-WEB application.
 *
 * @author Andreas Maier
 */
@RestController
@RequestMapping(value = "/api")
public class RequestController {

    private final Logger log = LoggerFactory.getLogger(RequestController.class);

    private final ObjectMapper objectMapper;
    private final WebGraphService webGraphService;
    private final NodeController nodeController;
    private final HistoryController historyController;
    private final JobController jobController;
    private final DbCommunicationService dbCommunicationService;
    private final UpdateService updateService;
    private final NedrexService nedrex;

    @Autowired
    public RequestController(NedrexService nedrex, UpdateService updateService, DbCommunicationService dbCommunicationService, ObjectMapper objectMapper, WebGraphService webGraphService, NodeController nodeController, HistoryController historyController, JobController jobController) {
        this.objectMapper = objectMapper;
        this.webGraphService = webGraphService;
        this.nodeController = nodeController;
        this.historyController = historyController;
        this.jobController = jobController;
        this.dbCommunicationService = dbCommunicationService;
        this.updateService = updateService;
        this.nedrex = nedrex;
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

    @RequestMapping(value = "/getQuickExample", method = RequestMethod.GET)
    @ResponseBody
    public String getQuickExample(@RequestParam("nr") int nr, @RequestParam("nodeType") String nodeName) {
        LinkedList<Object> nodes = new LinkedList<>();
        if (nr == 2) {
            webGraphService.getQuickExample(nodeName, nr).forEach(n -> {
                nodes.add(nodeController.getNode(nodeName, n).getAsMap(new HashSet<>(Arrays.asList("id", "displayName", "primaryDomainId"))));
            });
            return toJson(nodes);
        } else {
            return toJson(webGraphService.getConnectedNodes(nr == 0 ? "disorder" : "drug", nodeName, webGraphService.getQuickExample(nodeName, nr)));
        }

    }

    @RequestMapping(value = "/getSuggestionEntry", method = RequestMethod.GET)
    @ResponseBody
    public String getSuggestionEntry(@RequestParam("gid") String gid, @RequestParam("nodeType") String nodeName, @RequestParam("sid") String sid) {
        try {
            log.debug("Got request for SuggestionId=" + sid);
            return objectMapper.writeValueAsString(webGraphService.getSuggestionEntry(gid, nodeName, sid));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
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

    @RequestMapping(value = "/getTableDownload", method = RequestMethod.POST)
    @ResponseBody
    public String getTableDownload(@RequestBody TableDownloadRequest req) {
        return webGraphService.getTableDownload(req.gid, req.type, req.name, req.attributes);

    }

    @RequestMapping(value = "/getConnectedNodes", method = RequestMethod.POST)
    @ResponseBody
    public String getConnectedNodes(@RequestBody HashMap<String, Object> request) {
        Collection<Integer> ids = request.get("sugId").toString().indexOf('_') > -1 ? webGraphService.getSuggestionEntry(null, request.get("sourceType").toString(), request.get("sugId").toString()) : Collections.singletonList(Integer.parseInt(request.get("sugId").toString()));
        if ((boolean) request.get("noloop")) {
            LinkedList<Object> nodes = webGraphService.getDirectNodes(ids, request);
            return toJson(nodes);
        }
        return toJson(webGraphService.getConnectedNodes(request.get("sourceType").toString(), request.get("targetType").toString(), ids));
    }

    @RequestMapping(value = "/mapToDomainIds", method = RequestMethod.POST)
    @ResponseBody
    public String mapToDomainIds(@RequestBody HashMap<String, Object> request) {
        int type = Graphs.getNode(request.get("type").toString());
        HashMap<Integer, String> map = new HashMap<>();
        ((List<Integer>) request.get("ids")).forEach(id -> map.put(id, nodeController.getDomainId(type, id)));
        return toJson(map);
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

    @RequestMapping(value = "/getGuidedGraph", method = RequestMethod.POST)
    @ResponseBody
    public String getGuidedGraph(@RequestBody GuidedRequest request) {
        return toJson(webGraphService.getGuidedGraph(request));

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
        HashMap<String, Pair<String, Pair<Job.JobState, ToolService.Tool>>> jobs = jobController.getJobGraphStatesAndTypes(userId);
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

    @RequestMapping(value = "/mapListToItems", method = RequestMethod.POST)
    @ResponseBody
    public String getListToItems(@RequestBody MapListRequest request) {
        log.debug("Got mapping request for node type: " + request.type + " and list " + toJson(request.list));
        return toJson(webGraphService.mapIdsToItemList(request.type, new LinkedList(Arrays.asList(request.list))));
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

    @RequestMapping(value = "/getNedrex", method = RequestMethod.POST)
    @ResponseBody
    public String getNedrex(@RequestBody NedrexRequest req) {
        return this.nedrex.get(req.route);
    }

    @RequestMapping(value = "/postNedrex", method = RequestMethod.POST)
    @ResponseBody
    public String postNedrex(@RequestBody NedrexRequest req) {
        return this.nedrex.post(req.route, req.data);
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

    @RequestMapping(value="/getInteractingOnly", method = RequestMethod.POST)
    @ResponseBody
    public String filterInteracting(@RequestBody FilterInteractionRequest req){
        return toJson(webGraphService.filterInteracting(req.type, req.ids));
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


}
