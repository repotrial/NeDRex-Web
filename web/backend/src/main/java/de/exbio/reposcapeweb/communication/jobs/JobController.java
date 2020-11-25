package de.exbio.reposcapeweb.communication.jobs;

import de.exbio.reposcapeweb.ReposcapewebApplication;
import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.controller.SocketController;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import de.exbio.reposcapeweb.tools.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
public class JobController {

    private final WebGraphService graphService;
    private final ToolService toolService;
    private final JobQueue jobQueue;
    private final JobRepository jobRepository;
    private final SocketController socketController;

    private HashMap<String, Job> jobs = new HashMap<>();
    private Logger log = LoggerFactory.getLogger(JobController.class);

    @Autowired
    public JobController(WebGraphService graphService, ToolService toolService, JobQueue jobQueue, JobRepository jobRepository, SocketController socketController) {
        this.graphService = graphService;
        this.toolService = toolService;
        this.jobQueue = jobQueue;
        this.jobRepository = jobRepository;
        this.socketController = socketController;
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
        Graph g = graphService.getCachedGraph(req.graphId);
        prepareJob(j, req,g);
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

    private void prepareJob(Job j,JobRequest req, Graph g) {
        String command = createCommand(j,req);
        j.setCommand(command);
        prepareFiles(j, req,g);
    }

    private void prepareFiles(Job j, JobRequest req,Graph g) {
        toolService.prepareJobFiles(j.getJobId(), req, g);
    }

    private String createCommand(Job j, JobRequest req) {
        return toolService.createCommand(j, req);
    }


    public void finishJob(String id) {
        Job j = jobs.get(id);
        try {
            jobQueue.finishJob(j);
            HashMap<Integer, HashMap<String, Object>> results = toolService.getJobResults(j);
            if (results == null) {
                j.setDerivedGraph(j.getBasisGraph());
            } else
                //TODO do something more with the results?
                graphService.applyModuleJob(j, results.keySet());
            save(j);
        }catch (Exception e){
            log.error("Error on finishing job: "+id);
            e.printStackTrace();
            j.setStatus(Job.JobState.ERROR);
        }
        socketController.setJobUpdate(j);
        toolService.clearDirectories(j);
        //TODO send finished message to user frontend
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
