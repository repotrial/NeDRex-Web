package de.exbio.reposcapeweb.communication.jobs;

import de.exbio.reposcapeweb.communication.controller.SocketController;
import de.exbio.reposcapeweb.tools.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedList;

@Service
public class JobQueue {

    private final Environment env;
    private final ToolService toolService;
    private final SocketController socketController;

    private LinkedList<Job> queue = new LinkedList<>();
    private HashMap<String, Job> running = new HashMap<>();
    private final int simultaniousExecutes;
    private final int maxThreadsPerJob;
    private final long timeoutSeconds;

    @Autowired
    public JobQueue(Environment env, ToolService toolService, SocketController socketController) {
        this.env = env;
        this.toolService = toolService;
        this.socketController = socketController;
        simultaniousExecutes = Integer.parseInt(env.getProperty("jobs.parallel.number"));
        maxThreadsPerJob = Integer.parseInt(env.getProperty("jobs.parallel.task-max"));
        this.timeoutSeconds = Integer.parseInt(env.getProperty("jobs.timeout.mins")) * 60;
    }


    public void addJob(Job j) {
        queue.add(j);
        j.setStatus(Job.JobState.QUEUED);
    }

    @Scheduled(fixedRate = 2000)
    protected void updateQueue() {
        while (running.size() < simultaniousExecutes & queue.size() > 0)
            startNext();
        running.forEach((k, j) -> {
            if ((j.getStarted().toEpochSecond(ZoneOffset.ofTotalSeconds(0)) - LocalDateTime.now().toEpochSecond(ZoneOffset.ofTotalSeconds(0))) > timeoutSeconds) {
                terminate(j);
            }
        });

    }

    private void terminate(Job j) {
        j.getProcess().destroy();
        finishJob(j, Job.JobState.TIMEOUT);
    }

    private void startNext() {
        //TODO try queue.pop()
        Job j = queue.getFirst();
        queue.removeFirst();
        j.setThreads(Math.max(1, Math.min(maxThreadsPerJob, (simultaniousExecutes - running.size()) / 2)));
        running.put(j.getJobId(), j);
        executeJob(j);
        j.setStarted();
    }

    @Async
    public void executeJob(Job j) {
        j.setStatus(Job.JobState.EXECUTING);
        socketController.setJobUpdate(j);
        System.out.println("executing:\n"+j.getCommand());
        Process p = toolService.executeJob(j.getCommand());
        if (p != null)
            j.setProcess(p);
        else {
            finishJob(j);
            j.setStatus(Job.JobState.ERROR);
        }
    }

    public void finishJob(Job j) {
        finishJob(j, Job.JobState.DONE);
    }

    public void finishJob(Job j, Job.JobState state) {
        running.remove(j.getJobId());
        j.setStatus(state);
    }
}
