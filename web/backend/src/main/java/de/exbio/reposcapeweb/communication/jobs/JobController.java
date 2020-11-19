package de.exbio.reposcapeweb.communication.jobs;

import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import de.exbio.reposcapeweb.tools.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
public class JobController {

    private final WebGraphService graphService;
    private final ToolService toolService;
    private final JobQueue jobQueue;
    private final JobRepository jobRepository;

    @Autowired
    public JobController(WebGraphService graphService, ToolService toolService, JobQueue jobQueue, JobRepository jobRepository) {
        this.graphService = graphService;
        this.toolService = toolService;
        this.jobQueue = jobQueue;
        this.jobRepository = jobRepository;
    }


    private HashMap<String, Job> jobs = new HashMap<>();


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

        prepareJob(j, g);
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

    private void prepareJob(Job j, Graph g) {
        String command = createCommand(j);
        j.setCommand(command);
        prepareFiles(j, g);
    }

    private void prepareFiles(Job j, Graph g) {
        toolService.prepareJobFiles(j.getJobId(), j.getRequest().algorithm, j.getRequest().params, g);
    }

    private String createCommand(Job j) {
        return toolService.createCommand(j.getJobId(), j.getRequest().algorithm, j.getRequest().params);
    }


    public void finishJob(String id) {
        Job j = jobs.get(id);
        jobQueue.finishJob(j);
        HashMap<Integer, HashMap<String, Object>> results = toolService.getJobResults(j);
        //TODO do something more with the results?
        graphService.applyModuleJob(j, results.keySet());
        save(j);
        toolService.clearDirectories(j);
        //TODO send finished message to user frontend
    }
}
