package de.exbio.reposcapeweb.communication.jobs;

import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.controller.SocketController;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import de.exbio.reposcapeweb.db.history.HistoryController;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import de.exbio.reposcapeweb.db.services.nodes.GeneService;
import de.exbio.reposcapeweb.db.services.nodes.ProteinService;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.tools.algorithms.Algorithm;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
    private final HistoryController historyController;
    private final JobCache jobCache;

    private HashMap<String, Job> jobs = new HashMap<>();
    private HashMap<String, LinkedList<String>> jobWaitingForResults = new HashMap<>();
    private Logger log = LoggerFactory.getLogger(JobController.class);
    private EnumMap<ToolService.Tool, Algorithm> algorithms;

    @Autowired
    public JobController(HistoryController historyController, DrugService drugService, ProteinService proteinService, GeneService geneService, WebGraphService graphService, ToolService toolService, JobQueue jobQueue, JobRepository jobRepository, SocketController socketController, JobCache jobCache) {
        this.jobCache = jobCache;
        this.graphService = graphService;
        this.toolService = toolService;
        this.jobQueue = jobQueue;
        this.jobRepository = jobRepository;
        this.socketController = socketController;
        this.geneService = geneService;
        this.proteinService = proteinService;
        this.drugService = drugService;
        this.historyController = historyController;
        this.algorithms = toolService.getAlgorithms();
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

        j.addParam("experimentalOnly", req.experimentalOnly);
        for (String param : req.getParams().keySet()) {
            if (!param.equals("exprData"))
                j.addParam(param, req.getParams().get(param));
        }


        //TODO can be moved to frontend
        j.addParam("pcutoff", req.getParams().containsKey("pcutoff") ? Math.pow(10, Double.parseDouble(req.getParams().get("pcutoff"))) : 1);
        j.addParam("topX", req.getParams().containsKey("topX") ? Integer.parseInt(req.getParams().get("topX")) : Integer.MAX_VALUE);
        j.addParam("elements", req.getParams().containsKey("elements") && req.getParams().get("elements").startsWith("t"));
        Graph g = graphService.getCachedGraph(req.graphId);
        try {
            prepareJob(j, req, g);
        } catch (Exception e) {
            log.error("Error on job submission");
            j.setStatus(Job.JobState.ERROR);
            e.printStackTrace();
            return j;
        }
        if (!j.getState().equals(Job.JobState.DONE) && !j.getState().equals(Job.JobState.WAITING))
            queue(j);
        else
            jobRepository.save(j);
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
        if (g == null & req.selection) {
            g = graphService.createGraphFromIds(req.getParams().get("type"), req.nodes, j.getUserId());
            req.graphId = g.getId();
            j.setBasisGraph(g.getId());
        }

        Algorithm algorithm = algorithms.get(j.getMethod());
        if (algorithm.usesExpressionInput())
            prepareExpressionFile(req);

        String command = createCommand(j, req);
        j.setCommand(command);

        if (algorithm.usesSeedInput() && req.nodes != null && req.nodes.size() > 0) {
            j.setSeeds(req.nodes);
            try {
                Job sameJob = jobs.get(jobCache.getCached(j));
                switch (sameJob.getState()) {
                    case DONE -> {
                        copyResults(j.getJobId(), sameJob, false);
                        copyThumbnailAndLayout(j.getDerivedGraph(), sameJob.getDerivedGraph());
                        return;
                    }
                    case EXECUTING, INITIALIZED, QUEUED, NOCHANGE -> {
                        if (!this.jobWaitingForResults.containsKey(sameJob.getJobId()))
                            this.jobWaitingForResults.put(sameJob.getJobId(), new LinkedList<>());
                        this.jobWaitingForResults.get(sameJob.getJobId()).add(j.getJobId());
                        j.setStatus(Job.JobState.WAITING);
                        return;
                    }
                }
            } catch (NullPointerException ignore) {
            }
        }

        prepareFiles(j, req, g);
    }

    private void prepareExpressionFile(JobRequest req) {
        String data = req.getParams().get("exprData");
        String idType = req.getParams().get("exprIDType");
        if (data.indexOf(',') > -1)
            data = StringUtils.split(data, ',').get(1);
        final boolean[] header = {true};
        char n = '\n';
        StringBuilder mapped = new StringBuilder();

        HashMap<String, String> idMap = geneService.getDomainIdTypes().contains(idType) ? geneService.getSecondaryDomainToIDMap(idType) : proteinService.getSecondaryDomainToIDMap(idType);

        AtomicReference<Character> sep = new AtomicReference<>(null);
        char[] seps = new char[]{'\t',',',';'};
        StringUtils.split(new String(Base64.getDecoder().decode(data)), '\n').forEach(line -> {
                    if (line.length() == 0 || line.charAt(0) == '!' || line.charAt(0) == '#') {
                        return;
                    }
                    if (header[0]) {
                        mapped.append(line).append(n);
                        header[0] = false;
                        return;
                    }
                    if(sep.get() ==null){
                        int maxAmount = 0;
                        char bestChar = '\t';
                        for (char c : seps) {
                            int nr = StringUtils.split(line,c).size();
                            if(nr>maxAmount){
                                maxAmount=nr;
                                bestChar=c;
                            }
                        }
                        sep.set(bestChar);
                    }

                    LinkedList<String> split = StringUtils.splitFirst(line, sep.get());
                    String id = split.get(0);
                    if (id.charAt(0) == '"')
                        id = id.substring(1, id.length() - 1);

                    if (id.startsWith(idType + "."))
                        id = id.substring(idType.length() + 1);
                    id = id.toLowerCase();
                    String strID = idMap.get(id);
                    if (strID != null) {
                        mapped.append(strID).append(sep.get()).append(split.get(1)).append(n);
                    }
                }
        );
        req.getParams().put("exprData", mapped.toString());
    }

    private void prepareFiles(Job j, JobRequest req, Graph g) {
        HashMap<Integer, Pair<String, String>> domainMap;
        if (req.getParams().get("type").equals("gene")) {
            domainMap = geneService.getIdToDomainMap();
        } else {
            domainMap = proteinService.getIdToDomainMap();
        }
        toolService.prepareJobFiles(j, req, g, domainMap);
    }

    private String createCommand(Job j, JobRequest req) {
        return toolService.createCommand(j, req);
    }


    public boolean finishJob(String id) {
        Job j = jobs.get(id);
        boolean state = true;
        try {
            HashMap<Integer, HashMap<String, Integer>> domainIds = new HashMap<>();
            domainIds.put(Graphs.getNode("gene"), geneService.getDomainToIdMap());
            domainIds.put(Graphs.getNode("protein"), proteinService.getDomainToIdMap());
            domainIds.put(Graphs.getNode("drug"), drugService.getDomainToIdMap());
            jobQueue.finishJob(j);
            toolService.getJobResults(j, domainIds);
            if ((j.getResult().getNodes().isEmpty()) & (!j.getParams().containsKey("nodesOnly") || !j.getParams().get("nodesOnly").equals("true"))) {
                j.setDerivedGraph(j.getBasisGraph());
            } else {
                boolean basisIsJob = getJobByGraphId(j.getBasisGraph()) != null;
                graphService.applyJob(j, basisIsJob);
                if (!basisIsJob) {
                    historyController.remove(j.getBasisGraph());
                    j.setBasisGraph(null);
                }

            }
        } catch (Exception e) {
            log.error("Error on finishing job: " + id);
            e.printStackTrace();
            j.setStatus(Job.JobState.ERROR);
            state = false;
        }
        if (state)
            try {
                toolService.clearDirectories(j, historyController.getJobPath(j));
            } catch (Exception e) {
                log.error("Error on finishing job: " + id);
                e.printStackTrace();
                j.setStatus(Job.JobState.ERROR);
                state = false;
            }
        save(j);
        socketController.setJobUpdate(j);
        checkWaitingJobs(j);
        return state;
    }

    private void checkWaitingJobs(Job j) {
        if (jobWaitingForResults.containsKey(j.getJobId()))
            jobWaitingForResults.get(j.getJobId()).forEach(jid -> copyResults(jid, j, true));
    }

    private void copyResults(String cloneJid, Job j, boolean notify) {
        Job dependent = jobs.get(cloneJid);
        boolean keepParent = getJobByGraphId(j.getBasisGraph()) != null;
        if (!keepParent && dependent.getBasisGraph() != null) {
            historyController.remove(dependent.getBasisGraph());
            dependent.setBasisGraph(null);
        }
        dependent.setStatus(Job.JobState.DONE);
        log.info("Finished " + dependent.getMethod().name() + " job " + dependent.getJobId() + "of user " + dependent.getUserId() + "! (Cloned from " + j.getJobId() + " to finish)");
        dependent.setDerivedGraph(graphService.cloneGraph(j.getDerivedGraph(), dependent.getUserId(), dependent.getBasisGraph()));
        dependent.setResultFile(true);
        try {
            Files.copy(historyController.getJobPath(j).toPath(), historyController.getJobPath(dependent).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        dependent.setUpdate(j.getUpdate());
        if (notify) {
            copyThumbnailAndLayout(dependent.getDerivedGraph(), j.getDerivedGraph());
            socketController.setJobUpdate(dependent);
        }
    }

    public void copyThumbnailAndLayout(String derivedId, String originalId) {
        File copy = historyController.getThumbnailPath(derivedId);
        copy.getParentFile().mkdirs();
        File old = historyController.getThumbnailPath(originalId);
        if (old.exists()) {
            try {
                Files.copy(old.toPath(), copy.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        copy = historyController.getLayoutPath(derivedId);
        old = historyController.getLayoutPath(originalId);
        copy.getParentFile().mkdirs();
        if (old.exists()) {
            try {
                Files.copy(old.toPath(), copy.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public File getDownload(String id) {
        return historyController.getJobPath(jobs.get(id));
    }

    public String getJobByGraphId(String gid) {
        Optional<Job> j = jobs.values().stream().filter(job -> job != null && job.getDerivedGraph() != null && job.getDerivedGraph().equals(gid)).findFirst();
        return j.map(Job::getJobId).orElse(null);
    }


    public HashMap<String, Pair<String, Pair<Job.JobState, ToolService.Tool>>> getJobGraphStatesAndTypes(String user) {
        HashMap<String, Pair<String, Pair<Job.JobState, ToolService.Tool>>> stateMap = new HashMap<>();
        jobs.values().stream().filter(j -> j.getUserId() != null && j.getUserId().equals(user)).forEach(j -> stateMap.put(j.getDerivedGraph(), new Pair<>(j.getJobId(), new Pair<>(j.getState(), j.getMethod()))));
        return stateMap;
    }

    public LinkedList<HashMap<String, Object>> getJobGraphStatesAndTypes(String user, String gid) {
        LinkedList<HashMap<String, Object>> stateMap = new LinkedList<>();
        jobs.values().stream().filter(j -> j.getUserId().equals(user)).forEach(j -> {
            if ((gid == null || j.getBasisGraph().equals(gid)) | !j.getState().equals(Job.JobState.DONE)) {
                stateMap.add(j.toMap());
            }
        });
        return stateMap;
    }

    public Job getJobById(String jobid) {
        return this.jobs.get(jobid);
    }

    public HashMap<String, String> getParams(String jobid) {
        Job j = this.getJobById(jobid);
        if (j == null)
            return new HashMap<>();
        return j.getParams();


    }
}
