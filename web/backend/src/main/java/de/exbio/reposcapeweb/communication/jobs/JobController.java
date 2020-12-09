package de.exbio.reposcapeweb.communication.jobs;

import de.exbio.reposcapeweb.ReposcapewebApplication;
import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.controller.SocketController;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import de.exbio.reposcapeweb.db.services.nodes.GeneService;
import de.exbio.reposcapeweb.db.services.nodes.ProteinService;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.ReaderUtils;
import de.exbio.reposcapeweb.utils.StringUtils;
import de.exbio.reposcapeweb.utils.WriterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class JobController {

    private final WebGraphService graphService;
    private final ToolService toolService;
    private final JobQueue jobQueue;
    private final JobRepository jobRepository;
    private final SocketController socketController;
    private final GeneService geneService;
    private final ProteinService proteinService;
    private final DrugService drugService;

    private HashMap<String, Job> jobs = new HashMap<>();
    private Logger log = LoggerFactory.getLogger(JobController.class);

    @Autowired
    public JobController(DrugService drugService, ProteinService proteinService, GeneService geneService, WebGraphService graphService, ToolService toolService, JobQueue jobQueue, JobRepository jobRepository, SocketController socketController) {
        this.graphService = graphService;
        this.toolService = toolService;
        this.jobQueue = jobQueue;
        this.jobRepository = jobRepository;
        this.socketController = socketController;
        this.geneService = geneService;
        this.proteinService = proteinService;
        this.drugService = drugService;
    }


    private Job createJob(JobRequest req) {
        String id = UUID.randomUUID().toString();
        while (jobs.containsKey(id))
            id = UUID.randomUUID().toString();
        Job j = new Job(id, req);
        jobs.put(id, j);
        return j;
    }

    public void importJobsHistory() {
        jobRepository.findAll().forEach(j -> jobs.put(j.getJobId(), j));
    }


    public Job registerJob(JobRequest req) {
        Job j = createJob(req);
        if (j.getMethod().equals(ToolService.Tool.DIAMOND))
            j.addParam("pcutoff", req.getParams().containsKey("pcutoff") ? Math.pow(10, Double.parseDouble(req.getParams().get("pcutoff"))) : 1);
        if (j.getMethod().equals(ToolService.Tool.DIAMOND) | j.getMethod().equals(ToolService.Tool.BICON)) {
            j.addParam("nodesOnly", req.getParams().get("nodesOnly"));
            j.addParam("addInteractions", req.getParams().get("addInteractions"));
        }
        Graph g = graphService.getCachedGraph(req.graphId);
        try {
            prepareJob(j, req, g);
        } catch (Exception e) {
            log.error("Error on job submission");
            j.setStatus(Job.JobState.ERROR);
            return j;
        }
        queue(j);
        return j;
    }

    private void queue(Job j) {
        save(j);
        jobQueue.addJob(j);
    }

    private void save(Job j) {
        jobRepository.save(j);
    }

    private void prepareJob(Job j, JobRequest req, Graph g) {
        if (j.getMethod().equals(ToolService.Tool.BICON))
            prepareExpressionFile(req);
        String command = createCommand(j, req);
        j.setCommand(command);
        prepareFiles(j, req, g);
    }

    private void prepareExpressionFile(JobRequest req) {
        String data = req.getParams().get("exprData");
        if (data.indexOf(',') > -1)
            data = StringUtils.split(data, ',').get(1);
        final boolean[] header = {true};
        char tab = '\t';
        char n = '\n';
        StringBuilder mapped = new StringBuilder();
        StringUtils.split(new String(Base64.getDecoder().decode(data)), '\n').forEach(line -> {
                    if (line.length() == 0 || line.charAt(0) == '!') {
                        return;
                    }
                    if (header[0]) {
                        mapped.append(line).append(n);
                        header[0] = false;
                        return;
                    }
                    LinkedList<String> split = StringUtils.split(line, "\t");
                    String entrez = split.get(0);
                    if (entrez.charAt(0) == '"')
                        entrez = entrez.substring(1, entrez.length() - 1);

                    if (!entrez.startsWith("entrez."))
                        entrez = "entrez." + entrez;
                    Integer id = geneService.getDomainToIdMap().get(entrez);
                    if (id != null) {
                        mapped.append(id);
                        split.subList(1, split.size()).forEach(e -> mapped.append(tab).append(e));
                        mapped.append(n);
                    }
                }
        );
        req.getParams().put("exprData", mapped.toString());
    }

    private void prepareFiles(Job j, JobRequest req, Graph g) {
        if (j.getMethod().equals(ToolService.Tool.TRUSTRANK)) {
            HashSet<String> domainIds = new HashSet<>();
            HashSet<Integer> ids = new HashSet<>(req.selection ? req.nodes : g.getNodes().get(Graphs.getNode(req.getParams().get("type"))).keySet());
            if (req.getParams().get("type").equals("gene"))
                ids.forEach(n -> domainIds.add(geneService.map(n)));
            else
                ids.forEach(n -> domainIds.add(proteinService.map(n)));
            req.ids= domainIds;
        }
        toolService.prepareJobFiles(j, req, g);
    }

    private String createCommand(Job j, JobRequest req) {
        return toolService.createCommand(j, req);
    }


    public void finishJob(String id) {
        Job j = jobs.get(id);
        try {
            jobQueue.finishJob(j);
            HashSet<Integer> results = toolService.getJobResults(j);
            if ((results == null || results.isEmpty()) & (!j.getParams().containsKey("nodesOnly") || !j.getParams().get("nodesOnly").equals("true"))) {
                j.setDerivedGraph(j.getBasisGraph());
            } else
                graphService.applyModuleJob(j, results);
        } catch (Exception e) {
            log.error("Error on finishing job: " + id);
            e.printStackTrace();
            j.setStatus(Job.JobState.ERROR);
        }
        save(j);
        socketController.setJobUpdate(j);
        toolService.clearDirectories(j);
    }

    private HashSet<Integer> toIds(Job j, HashSet jobResults) {
        if(j.getMethod().equals(ToolService.Tool.DIAMOND)| j.getMethod().equals(ToolService.Tool.BICON))
            return jobResults;
        if(j.getMethod().equals(ToolService.Tool.TRUSTRANK)){
            HashSet<Integer> out = new HashSet<>();
            jobResults.forEach(s->out.add(drugService.map((String)s)));
            return out;
        }
        return null;
    }

    public HashMap<String, Job.JobState> getJobGraphStates(String user) {
        HashMap<String, Job.JobState> stateMap = new HashMap<>();
            jobs.values().stream().filter(j -> j.getUserId().equals(user)).forEach(j -> stateMap.put(j.getDerivedGraph(), j.getState()));
        return stateMap;
    }

    public LinkedList<HashMap<String, Object>> getJobGraphStates(String user, String gid) {
        LinkedList<HashMap<String, Object>> stateMap = new LinkedList<>();
            jobs.values().stream().filter(j -> j.getUserId().equals(user)).forEach(j -> {
                if ((gid == null || j.getBasisGraph().equals(gid)) | !j.getState().equals(Job.JobState.DONE)) {
                    stateMap.add(j.toMap());
                }
            });
        return stateMap;
    }

}
