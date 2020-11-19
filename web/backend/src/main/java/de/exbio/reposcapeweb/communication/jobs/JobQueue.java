package de.exbio.reposcapeweb.communication.jobs;

import de.exbio.reposcapeweb.tools.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;

@Service
public class JobQueue {

    private final Environment env;
    private final ToolService toolService;

    private LinkedList<Job> queue = new LinkedList<>();
    private HashMap<String, Job> running = new HashMap<>();
    private final int simultaniousExecutes;

    @Autowired
    public JobQueue(Environment env, ToolService toolService) {
        this.env = env;
        this.toolService=toolService;
        simultaniousExecutes = Integer.parseInt(env.getProperty("jobs.parallel.number"));
    }


    public void addJob(Job j) {
        queue.add(j);
        j.setStatus(Job.JobState.QUEUED);
    }

    @Scheduled(fixedRate = 2000)
    protected void updateQueue() {
        while(running.size()<simultaniousExecutes & queue.size()>0)
            startNext();

    }

    private void startNext(){
        Job j = queue.getFirst();
        queue.removeFirst();
        running.put(j.getJobId(),j);
        executeJob(j);
    }

    @Async
    public void executeJob(Job j){
        j.setStatus(Job.JobState.EXECUTING);
        toolService.executeJob(j.getCommand());

    }



    public void finishJob(Job j) {
        running.remove(j.getJobId());
        j.setStatus(Job.JobState.DONE);
    }
}
